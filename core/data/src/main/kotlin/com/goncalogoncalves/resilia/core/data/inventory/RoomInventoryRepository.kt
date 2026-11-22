package com.goncalogoncalves.resilia.core.data.inventory

import com.goncalogoncalves.resilia.core.domain.model.InventoryItem
import com.goncalogoncalves.resilia.core.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomInventoryRepository @Inject constructor(
    private val dao: InventoryDao,
) : InventoryRepository {

    override val items: Flow<List<InventoryItem>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun all(): List<InventoryItem> = dao.all().map { it.toDomain() }

    override suspend fun save(item: InventoryItem): Long {
        val rowId = dao.upsert(item.toEntity())
        // @Upsert returns -1 when it updated an existing row.
        return if (item.id != 0L) item.id else rowId
    }

    override suspend fun delete(id: Long) = dao.delete(id)

    override suspend fun markNotified(soon: List<InventoryItem>, expired: List<InventoryItem>) {
        if (soon.isNotEmpty()) dao.markSoonNotified(soon.map { it.id })
        if (expired.isNotEmpty()) dao.markExpiredNotified(expired.map { it.id })
    }
}
