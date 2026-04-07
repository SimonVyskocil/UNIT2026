package com.example.unit2026.presentation

data class Place(
    val id: String,
    val name: String,
    val description: String,
    val rating: Double,
    val noise: Int,
    val comfort: Int,
    val refreshments: Int,
    val openingHours: String,
    val images: List<String>,
    val certified: Boolean,
    val powerOutlet: Boolean,
    val coordinates: String? = null,
)
