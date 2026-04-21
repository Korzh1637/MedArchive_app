package com.example.medarchive.domain.models

import androidx.compose.ui.graphics.Color

data class HealthCategory(
    val id: String,
    val name: String,
    val unit: String,
    val iconRes: Int,
    val color: Color,
    val lastValue: Double? = null
)