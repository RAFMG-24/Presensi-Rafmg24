package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        UnitKerjaEntity::class,
        LokasiKantorEntity::class,
        PegawaiLokasiEntity::class,
        AbsensiEntity::class,
        PengajuanEntity::class,
        ActivityLogEntity::class,
        PengaturanEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DamkarDatabase : RoomDatabase() {
    abstract fun damkarDao(): DamkarDao

    companion object {
        @Volatile
        private var INSTANCE: DamkarDatabase? = null

        fun getDatabase(context: Context): DamkarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DamkarDatabase::class.java,
                    "damkar_subang.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
