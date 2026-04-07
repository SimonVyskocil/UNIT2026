package com.example.unit2026.presentation

data class Place(
    val id: String,
    val name: String,
    val description: String,
    val rating: Double,
    val noise: String, // e.g. "Low", "Medium", "High"
    val comfort: String, // e.g. "Excellent", "Good"
    val snacksAvailability: String, // e.g. "Available", "None"
    val openingHours: String,
    val images: List<String>, // Placeholder URLs or just names for now
)
