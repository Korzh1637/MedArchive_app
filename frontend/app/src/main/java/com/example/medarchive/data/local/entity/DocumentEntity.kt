package com.example.medarchive.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val localId: String,           // UUID как строка
    val userId: Int,
    val title: String? = null,
    val documentType: String? = null,
    val content: String? = null,
    val imagePath: String? = null,      // ПЕРЕВЕСТИ В БИНАРНЫЙ ФОРМАТ 
    val addImagePath: Boolean = false,  // УДАЛИТЬ
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val deletedAt: Date? = null,
    val isSynced: Boolean = false,
    val lastSyncAt: Date? = null
)
