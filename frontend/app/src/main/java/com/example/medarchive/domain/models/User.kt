package com.example.medarchive.domain.models

import java.util.Date

data class User(
    val id: Int,
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val createdAt: Date,
    val updatedAt: Date,
    val isActive: Boolean
)