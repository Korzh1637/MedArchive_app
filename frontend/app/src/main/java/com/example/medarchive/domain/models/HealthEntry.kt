package com.example.medarchive.domain.models

import java.util.Date

data class HealthEntry(
    val localId: String,
    val userId: Int,
    val entryType: String,
    val value1: Double?,
    val value2: Double?,
    val value3: Double?,
    val unit: String?,
    val notes: String?,
    val entryDate: Date,
    val createdAt: Date,
    val updatedAt: Date,
    val isSynced: Boolean
)