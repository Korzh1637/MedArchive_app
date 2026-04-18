package com.example.medarchive.domain.models

import java.util.Date

data class Document(
    val localId: String,
    val userId: Int,
    val title: String?,
    val documentType: String?,
    val content: String?,
    val imagePath: String?,
    val createdAt: Date,
    val updatedAt: Date,
    val isSynced: Boolean
)