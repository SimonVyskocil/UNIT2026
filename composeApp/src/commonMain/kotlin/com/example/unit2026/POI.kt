package com.example.unit2026

import kotlinx.serialization.Serializable

@Serializable
data class POI(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double
)
