package com.goncalogoncalves.resilia.core.data.di

import android.content.Context
import androidx.room.Room
import com.goncalogoncalves.resilia.core.data.alerts.AlertDao
import com.goncalogoncalves.resilia.core.data.alerts.MIGRATION_2_3
import com.goncalogoncalves.resilia.core.data.inventory.InventoryDao
import com.goncalogoncalves.resilia.core.data.places.MIGRATION_3_4
import com.goncalogoncalves.resilia.core.data.places.PlaceDao
import com.goncalogoncalves.resilia.core.data.inventory.InventoryDatabase
import com.goncalogoncalves.resilia.core.data.plan.FamilyPlanDao
import com.goncalogoncalves.resilia.core.data.plan.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): InventoryDatabase =
        Room.databaseBuilder(context, InventoryDatabase::class.java, InventoryDatabase.NAME)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()

    @Provides
    fun inventoryDao(db: InventoryDatabase): InventoryDao = db.inventory()

    @Provides
    fun familyPlanDao(db: InventoryDatabase): FamilyPlanDao = db.familyPlan()

    @Provides
    fun alertDao(db: InventoryDatabase): AlertDao = db.alerts()

    @Provides
    fun placeDao(db: InventoryDatabase): PlaceDao = db.places()
}
