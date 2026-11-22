package com.goncalogoncalves.resilia.core.data.content

import android.content.Context
import com.goncalogoncalves.resilia.core.content.ContentLoader
import com.goncalogoncalves.resilia.core.domain.model.ContentBundle
import com.goncalogoncalves.resilia.core.domain.repository.ContentRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Loads bundled content from APK assets once and keeps it in memory (it is small). */
@Singleton
class AssetContentRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : ContentRepository {

    private val mutex = Mutex()
    private var cached: ContentBundle? = null

    override suspend fun load(): ContentBundle = mutex.withLock {
        cached ?: withContext(Dispatchers.IO) {
            ContentLoader { path ->
                context.assets.open("$ASSET_ROOT/$path").bufferedReader().use { it.readText() }
            }.load()
        }.also { cached = it }
    }

    private companion object {
        const val ASSET_ROOT = "content"
    }
}
