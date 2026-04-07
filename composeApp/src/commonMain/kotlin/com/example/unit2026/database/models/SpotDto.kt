package com.example.unit2026.database.models
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpotDto(
    val id: Long? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    val name: String,
    val description: String? = null,

    val certified: Boolean = false,

    val coordinates: String,

    @SerialName("opening_hours")
    val openingHours: String? = null,

    @SerialName("power_outlet")
    val powerOutlet: Boolean = false,

    val noise: Int,
    val comfort: Int,
    val refreshments: Int
)
