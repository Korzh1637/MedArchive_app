package com.example.medarchive.domain.models

data class HealthStats(
    val average: Double,
    val min: Double,
    val max: Double,
    val trend: String // "↑", "↓", "→"
)

fun calculateTrend(values: List<Double>): String {
    if (values.size < 2) return "→"
    val first = values.first()
    val last = values.last()
    return when {
        last > first -> "↑"
        last < first -> "↓"
        else -> "→"
    }
}