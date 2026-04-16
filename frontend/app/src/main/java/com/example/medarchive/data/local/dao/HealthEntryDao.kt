package com.example.medarchive.data.local.dao

import androidx.room.*
import com.example.medarchive.data.local.entity.HealthEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthEntryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEntry(entry: HealthEntryEntity): Long

    @Query("SELECT * FROM health_entries WHERE userId = :userId AND localId = :localId AND deletedAt IS NULL")
    suspend fun getEntry(userId: Int, localId: String): HealthEntryEntity?

    @Update
    suspend fun updateEntry(entry: HealthEntryEntity)

    @Query("UPDATE health_entries SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE userId = :userId AND localId = :localId")
    suspend fun softDeleteEntry(userId: Int, localId: String, deletedAt: java.util.Date, updatedAt: java.util.Date)

    @Query("SELECT * FROM health_entries WHERE userId = :userId AND deletedAt IS NULL ORDER BY entryDate DESC")
    fun getAllEntriesFlow(userId: Int): Flow<List<HealthEntryEntity>>
}