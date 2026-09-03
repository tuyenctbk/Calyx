package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.CalyxDao
import com.example.data.local.entities.CycleLogEntity
import com.example.data.local.entities.CyclePeriodEntity
import com.example.data.local.entities.SecurityEntity

@Database(
    entities = [CycleLogEntity::class, CyclePeriodEntity::class, SecurityEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CalyxDatabase : RoomDatabase() {
    abstract fun calyxDao(): CalyxDao

    companion object {
        @Volatile
        private var INSTANCE: CalyxDatabase? = null

        fun getDatabase(context: Context): CalyxDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CalyxDatabase::class.java,
                    "calyx_zero_knowledge_db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
