package com.goncalogoncalves.resilia.core.data.di

import com.goncalogoncalves.resilia.core.data.content.AssetContentRepository
import com.goncalogoncalves.resilia.core.data.device.CameraTorch
import com.goncalogoncalves.resilia.core.domain.device.Torch
import com.goncalogoncalves.resilia.core.data.state.DataStoreAppStateRepository
import com.goncalogoncalves.resilia.core.data.state.DataStoreFavoritesRepository
import com.goncalogoncalves.resilia.core.domain.repository.FavoritesRepository
import com.goncalogoncalves.resilia.core.domain.repository.AppStateRepository
import com.goncalogoncalves.resilia.core.domain.repository.ContentRepository
import com.goncalogoncalves.resilia.core.data.inventory.RoomInventoryRepository
import com.goncalogoncalves.resilia.core.data.state.DataStorePreparednessRepository
import com.goncalogoncalves.resilia.core.domain.repository.InventoryRepository
import com.goncalogoncalves.resilia.core.domain.repository.PreparednessRepository
import com.goncalogoncalves.resilia.core.data.plan.KeystoreCipher
import com.goncalogoncalves.resilia.core.data.plan.RoomFamilyPlanRepository
import com.goncalogoncalves.resilia.core.domain.repository.FamilyPlanRepository
import com.goncalogoncalves.resilia.core.domain.repository.SecretCipher
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun content(impl: AssetContentRepository): ContentRepository

    @Binds
    abstract fun appState(impl: DataStoreAppStateRepository): AppStateRepository

    @Binds
    abstract fun favorites(impl: DataStoreFavoritesRepository): FavoritesRepository

    @Binds
    abstract fun inventory(impl: RoomInventoryRepository): InventoryRepository

    @Binds
    abstract fun preparedness(impl: DataStorePreparednessRepository): PreparednessRepository

    @Binds
    abstract fun familyPlan(impl: RoomFamilyPlanRepository): FamilyPlanRepository

    @Binds
    abstract fun cipher(impl: KeystoreCipher): SecretCipher

    @Binds
    abstract fun torch(impl: CameraTorch): Torch

    @Binds
    abstract fun activityFilter(impl: com.goncalogoncalves.resilia.core.data.state.DataStoreActivityFilterRepository): com.goncalogoncalves.resilia.core.domain.repository.ActivityFilterRepository

    @Binds
    abstract fun alerts(impl: com.goncalogoncalves.resilia.core.data.alerts.IpmaAlertsRepository): com.goncalogoncalves.resilia.core.domain.repository.AlertsRepository

    @Binds
    abstract fun places(impl: com.goncalogoncalves.resilia.core.data.places.RoomPlacesRepository): com.goncalogoncalves.resilia.core.domain.repository.PlacesRepository

    @Binds
    abstract fun backup(impl: com.goncalogoncalves.resilia.core.data.backup.FileBackupRepository): com.goncalogoncalves.resilia.core.domain.repository.BackupRepository
}
