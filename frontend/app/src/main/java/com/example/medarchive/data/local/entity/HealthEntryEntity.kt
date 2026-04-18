package com.example.medarchive.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "health_entries")
data class HealthEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val localId: String,
    val userId: Int,
    val entryType: String,
    val value1: Double? = null,
    val value2: Double? = null,
    val value3: Double? = null,
    val unit: String? = null,
    val notes: String? = null,
    val entryDate: Date,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val deletedAt: Date? = null,
    val isSynced: Boolean = false,
    val lastSyncAt: Date? = null
)