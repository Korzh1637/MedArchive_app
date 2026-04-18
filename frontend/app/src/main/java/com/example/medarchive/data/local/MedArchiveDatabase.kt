package com.example.medarchive.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.medarchive.data.local.converters.Converters
import com.example.medarchive.data.local.dao.UserDao
import com.example.medarchive.data.local.dao.DocumentDao
import com.example.medarchive.data.local.dao.HealthEntryDao
import com.example.medarchive.data.local.entity.UserEntity
import com.example.medarchive.data.local.entity.DocumentEntity
import com.example.medarchive.data.local.entity.HealthEntryEntity

@Database(
    entities = [UserEntity::class, DocumentEntity::class, HealthEntryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MedArchiveDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun documentDao(): DocumentDao
    abstract fun healthEntryDao(): HealthEntryDao

    companion object {
        @Volatile
        private var INSTANCE: MedArchiveDatabase? = null

        fun getDatabase(context: Context): MedArchiveDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedArchiveDatabase::class.java,
                    "medarchive.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}