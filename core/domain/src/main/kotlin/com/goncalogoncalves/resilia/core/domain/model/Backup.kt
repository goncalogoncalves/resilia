package com.goncalogoncalves.resilia.core.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.security.SecureRandom
import java.time.LocalDate
import java.util.Base64
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/*
 * Manual export / import (Marco 0 §3, Marco 9a decision 3). A single JSON file the person
 * keeps; nothing leaves the phone unless they move it. With medical sheets the file MUST be
 * encrypted with a password (AES-256-GCM, key from PBKDF2-HMAC-SHA256).
 */

@Serializable
data class BackupInventoryItem(
    val name: String,
    val quantity: String = "",
    val list: KitList = KitList.HOME,
    val kitItemId: String? = null,
    /** YYYY-MM-DD */
    val expiresOn: String? = null,
    val addedOn: String,
)

@Serializable
data class BackupPlace(
    val type: String,
    val name: String,
    val address: String = "",
    val notes: String = "",
    val lat: Double? = null,
    val lng: Double? = null,
)

@Serializable
data class BackupAlerts(val areaId: String? = null, val dico: String? = null, val intervalHours: Int = AlertSettings.DEFAULT_INTERVAL, val notifyYellow: Boolean = false)

/** Everything exported. Ids are not kept: importing creates new rows. */
@Serializable
data class BackupData(
    val createdAt: String,
    val appVersion: String = "",
    val household: Household = Household(),
    val inventory: List<BackupInventoryItem> = emptyList(),
    val plan: FamilyPlan = FamilyPlan(),
    val members: List<Member> = emptyList(),
    val contacts: List<PlanContact> = emptyList(),
    val places: List<BackupPlace> = emptyList(),
    val alerts: BackupAlerts = BackupAlerts(),
) {
    val hasMedical: Boolean get() = members.any { !it.medical.isEmpty }

    fun inventoryItems(): List<InventoryItem> = inventory.map {
        InventoryItem(
            name = it.name,
            quantity = it.quantity,
            list = it.list,
            kitItemId = it.kitItemId,
            expiresOn = it.expiresOn?.let(LocalDate::parse),
            addedOn = LocalDate.parse(it.addedOn),
        )
    }

    fun placeItems(): List<Place> = places.map { p ->
        val la = p.lat
        val lo = p.lng
        Place(
            type = PlaceType.entries.firstOrNull { it.name == p.type } ?: PlaceType.OTHER,
            name = p.name,
            address = p.address,
            notes = p.notes,
            position = if (la != null && lo != null) runCatching { LatLng(la, lo) }.getOrNull() else null,
        )
    }

    companion object {
        fun inventoryOf(items: List<InventoryItem>) = items.map {
            BackupInventoryItem(it.name, it.quantity, it.list, it.kitItemId, it.expiresOn?.toString(), it.addedOn.toString())
        }

        fun placesOf(items: List<Place>) = items.map {
            BackupPlace(it.type.name, it.name, it.address, it.notes, it.position?.lat, it.position?.lng)
        }
    }
}

/** The file on disk. Exactly one of [data] or [ciphertext] is set. */
@Serializable
data class BackupFile(
    val format: String = FORMAT,
    val version: Int = VERSION,
    val encrypted: Boolean,
    val data: BackupData? = null,
    val kdf: String? = null,
    val iterations: Int? = null,
    val salt: String? = null,
    val iv: String? = null,
    val ciphertext: String? = null,
) {
    companion object {
        const val FORMAT = "resilia-backup"
        const val VERSION = 1
    }
}

sealed class BackupException(message: String) : Exception(message) {
    class NotABackup : BackupException("not a Resilia backup")
    class NewerVersion : BackupException("made by a newer version of the app")
    class PasswordRequired : BackupException("this file is encrypted")
    class WrongPassword : BackupException("wrong password or damaged file")
    class MedicalNeedsPassword : BackupException("medical sheets are only exported with a password")
}

object BackupCodec {
    const val KDF = "PBKDF2WithHmacSHA256"
    const val ITERATIONS = 600_000
    const val MIN_PASSWORD = 8
    private const val KEY_BITS = 256
    private const val SALT_BYTES = 16
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; prettyPrint = false }
    private val random = SecureRandom()
    private val b64 = Base64.getEncoder()
    private val unb64 = Base64.getDecoder()

    /** [password] null = plain JSON (refused when there are medical sheets). */
    fun encode(data: BackupData, password: CharArray?, iterations: Int = ITERATIONS): String {
        if (password == null) {
            if (data.hasMedical) throw BackupException.MedicalNeedsPassword()
            return json.encodeToString(BackupFile.serializer(), BackupFile(encrypted = false, data = data))
        }
        require(password.size >= MIN_PASSWORD) { "password too short" }
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val iv = ByteArray(IV_BYTES).also(random::nextBytes)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key(password, salt, iterations), GCMParameterSpec(TAG_BITS, iv))
        cipher.updateAAD(aad(BackupFile.VERSION))
        val plain = json.encodeToString(BackupData.serializer(), data).toByteArray(Charsets.UTF_8)
        val file = BackupFile(
            encrypted = true,
            kdf = KDF,
            iterations = iterations,
            salt = b64.encodeToString(salt),
            iv = b64.encodeToString(iv),
            ciphertext = b64.encodeToString(cipher.doFinal(plain)),
        )
        return json.encodeToString(BackupFile.serializer(), file)
    }

    /** Reads the header only, to know whether to ask for a password. */
    fun inspect(text: String): BackupFile {
        val file = try {
            json.decodeFromString(BackupFile.serializer(), text)
        } catch (_: Exception) {
            throw BackupException.NotABackup()
        }
        if (file.format != BackupFile.FORMAT) throw BackupException.NotABackup()
        if (file.version > BackupFile.VERSION) throw BackupException.NewerVersion()
        return file
    }

    fun decode(text: String, password: CharArray?): BackupData {
        val file = inspect(text)
        if (!file.encrypted) return file.data ?: throw BackupException.NotABackup()
        if (password == null) throw BackupException.PasswordRequired()
        val salt = file.salt?.let(unb64::decode) ?: throw BackupException.NotABackup()
        val iv = file.iv?.let(unb64::decode) ?: throw BackupException.NotABackup()
        val body = file.ciphertext?.let(unb64::decode) ?: throw BackupException.NotABackup()
        if (file.kdf != KDF) throw BackupException.NotABackup()
        val iterations = file.iterations ?: throw BackupException.NotABackup()
        val plain = try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(password, salt, iterations), GCMParameterSpec(TAG_BITS, iv))
            cipher.updateAAD(aad(file.version))
            cipher.doFinal(body)
        } catch (_: AEADBadTagException) {
            throw BackupException.WrongPassword()
        }
        return try {
            json.decodeFromString(BackupData.serializer(), plain.toString(Charsets.UTF_8))
        } catch (_: Exception) {
            throw BackupException.NotABackup()
        }
    }

    /** The header fields are authenticated too, so a file cannot be relabelled. */
    private fun aad(version: Int) = "${BackupFile.FORMAT}:$version".toByteArray(Charsets.UTF_8)

    private fun key(password: CharArray, salt: ByteArray, iterations: Int): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, iterations, KEY_BITS)
        try {
            val bytes = SecretKeyFactory.getInstance(KDF).generateSecret(spec).encoded
            return SecretKeySpec(bytes, "AES")
        } finally {
            spec.clearPassword()
        }
    }
}
