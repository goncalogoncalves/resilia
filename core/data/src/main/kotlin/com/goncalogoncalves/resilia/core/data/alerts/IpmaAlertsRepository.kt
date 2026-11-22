package com.goncalogoncalves.resilia.core.data.alerts

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.goncalogoncalves.resilia.core.content.IpmaParser
import com.goncalogoncalves.resilia.core.data.state.appStateStore
import com.goncalogoncalves.resilia.core.domain.AppInfo
import com.goncalogoncalves.resilia.core.domain.model.AlertSettings
import com.goncalogoncalves.resilia.core.domain.model.AlertSnapshot
import com.goncalogoncalves.resilia.core.domain.model.AlertZone
import com.goncalogoncalves.resilia.core.domain.repository.AlertsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** One GET. An interface so tests can answer with the responses saved on 2026-11-21. */
fun interface HttpGetter {
    /** Returns the HTTP status and body; throws [IOException] when there is no answer at all. */
    fun get(url: String): Pair<Int, String>
}

/**
 * Plain HttpURLConnection (decision §4.2: no network library). HTTPS only, no cookies,
 * no identifiers: the only header we set is a generic User-Agent.
 */
class UrlConnectionGetter(private val userAgent: String) : HttpGetter {
    override fun get(url: String): Pair<Int, String> {
        require(url.startsWith("https://")) { "HTTPS only" }
        val connection = URL(url).openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.instanceFollowRedirects = false
            connection.useCaches = false
            connection.setRequestProperty("User-Agent", userAgent)
            connection.setRequestProperty("Accept", "application/json")
            val status = connection.responseCode
            val body = if (status == HttpURLConnection.HTTP_OK) {
                connection.inputStream.use { input ->
                    val bytes = input.readNBytesCompat(MAX_BYTES + 1)
                    if (bytes.size > MAX_BYTES) throw IOException("response too large")
                    bytes.toString(Charsets.UTF_8)
                }
            } else {
                ""
            }
            status to body
        } finally {
            connection.disconnect()
        }
    }

    private fun java.io.InputStream.readNBytesCompat(limit: Int): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8 * 1024)
        while (out.size() < limit) {
            val n = read(buffer, 0, minOf(buffer.size, limit - out.size()))
            if (n < 0) break
            out.write(buffer, 0, n)
        }
        return out.toByteArray()
    }

    private companion object {
        const val TIMEOUT_MS = 20_000
        /** The warnings file was 35 KB on 2026-11-21; anything near this limit is not what we expect. */
        const val MAX_BYTES = 2 * 1024 * 1024
    }
}

@Singleton
class IpmaAlertsRepository internal constructor(
    context: Context,
    private val dao: AlertDao,
    private val http: HttpGetter,
    private val clock: () -> Instant,
) : AlertsRepository {

    @Inject
    constructor(@ApplicationContext context: Context, dao: AlertDao) : this(
        context,
        dao,
        UrlConnectionGetter("${AppInfo.NAME}/${context.versionName()}"),
        Instant::now,
    )

    private val store = context.appStateStore

    private val prefs = store.data.catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }

    override val settings: Flow<AlertSettings> = prefs.map { p ->
        val area = p[AREA]
        AlertSettings(
            zone = area?.let { AlertZone(it, p[DICO]) },
            intervalHours = p[INTERVAL] ?: AlertSettings.DEFAULT_INTERVAL,
            notifyYellow = p[NOTIFY_YELLOW] ?: false,
        )
    }

    override val snapshot: Flow<AlertSnapshot> = combine(dao.observeCache(), prefs) { rows, p ->
        val warningsRow = rows.firstOrNull { it.source == WARNINGS }
        AlertSnapshot(
            warnings = warningsRow?.let { runCatching { IpmaParser.warnings(it.payload) }.getOrNull() }.orEmpty(),
            fireRisk = rows.filter { it.source.startsWith("rcm-") }
                .flatMap { row -> runCatching { IpmaParser.fireRisk(row.payload) }.getOrNull().orEmpty() },
            fetchedAt = warningsRow?.let { Instant.ofEpochMilli(it.fetchedAt) },
            lastAttemptFailed = p[LAST_FAILED] ?: false,
            lastAttemptAt = p[LAST_ATTEMPT]?.let(Instant::ofEpochMilli),
        )
    }.flowOn(Dispatchers.Default)

    override suspend fun setZone(zone: AlertZone?) {
        store.edit {
            if (zone == null) {
                it.remove(AREA)
                it.remove(DICO)
            } else {
                it[AREA] = zone.areaId
                val dico = zone.dico
                if (dico == null) it.remove(DICO) else it[DICO] = dico
            }
        }
        if (zone?.dico == null) dao.clearFireRisk()
    }

    override suspend fun setInterval(hours: Int) {
        require(hours in AlertSettings.INTERVAL_OPTIONS) { "unsupported interval $hours" }
        store.edit { it[INTERVAL] = hours }
    }

    override suspend fun setNotifyYellow(enabled: Boolean) {
        store.edit { it[NOTIFY_YELLOW] = enabled }
    }

    override suspend fun refresh(): Boolean = withContext(Dispatchers.IO) {
        val zone = settings.first().zone
        val now = clock()
        // The national file, always (decision §4.3): the request says nothing about the zone.
        val warningsOk = fetch(WARNINGS, IpmaParser.WARNINGS_URL, now) { IpmaParser.warnings(it) }
        if (zone?.dico != null) {
            (0..2).forEach { day -> fetch("rcm-d$day", IpmaParser.fireRiskUrl(day), now) { IpmaParser.fireRisk(it) } }
        }
        store.edit {
            it[LAST_ATTEMPT] = now.toEpochMilli()
            it[LAST_FAILED] = !warningsOk
        }
        dao.pruneNotified(now.minus(Duration.ofDays(7)).toEpochMilli())
        warningsOk
    }

    /** Stores the body only if it is a 200 that parses; otherwise the previous copy stays. */
    private suspend fun fetch(source: String, url: String, now: Instant, check: (String) -> Unit): Boolean {
        val (status, body) = try {
            http.get(url)
        } catch (_: IOException) {
            return false
        }
        if (status != HttpURLConnection.HTTP_OK) return false
        try {
            check(body)
        } catch (_: Exception) {
            return false
        }
        dao.upsert(AlertCacheEntity(source, now.toEpochMilli(), body, status))
        return true
    }

    override suspend fun notifiedKeys(): Set<String> = dao.notifiedKeys().toSet()

    override suspend fun markNotified(keys: Collection<String>) {
        val at = clock().toEpochMilli()
        dao.markNotified(keys.map { AlertNotifiedEntity(it, at) })
    }

    private companion object {
        const val WARNINGS = "warnings"
        val AREA = stringPreferencesKey("alerts_area")
        val DICO = stringPreferencesKey("alerts_dico")
        val INTERVAL = intPreferencesKey("alerts_interval_hours")
        val NOTIFY_YELLOW = booleanPreferencesKey("alerts_notify_yellow")
        val LAST_ATTEMPT = longPreferencesKey("alerts_last_attempt")
        val LAST_FAILED = booleanPreferencesKey("alerts_last_failed")
    }
}

private fun Context.versionName(): String =
    runCatching { packageManager.getPackageInfo(packageName, 0).versionName }.getOrNull() ?: "0"
