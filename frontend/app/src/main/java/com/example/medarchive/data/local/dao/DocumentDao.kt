package com.example.medarchive.data.local.dao

import androidx.room.*
import com.example.medarchive.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDocument(document: DocumentEntity): Long

    @Query("SELECT * FROM documents WHERE userId = :userId AND localId = :localId AND deletedAt IS NULL")
    suspend fun getDocument(userId: Int, localId: String): DocumentEntity?

    @Query("SELECT * FROM documents WHERE addImagePath = 1 AND deletedAt IS NULL")
    suspend fun getRowDocuments(): List<DocumentEntity>

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Query("UPDATE documents SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE userId = :userId AND localId = :localId")
    suspend fun softDeleteDocument(userId: Int, localId: String, deletedAt: java.util.Date, updatedAt: java.util.Date)

    @Query("SELECT * FROM documents WHERE userId = :userId AND deletedAt IS NULL ORDER BY createdAt DESC")
    fun getAllDocumentsFlow(userId: Int): Flow<List<DocumentEntity>>
}