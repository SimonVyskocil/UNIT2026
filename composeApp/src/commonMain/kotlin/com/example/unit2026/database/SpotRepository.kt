package com.example.unit2026.database

import com.example.unit2026.database.models.SpotDto
import com.example.unit2026.presentation.Place
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.JsonObject

class SpotRepository(
    private val supabase: SupabaseClient
) {

    suspend fun getSpots(): List<Place> {
        return try {
            val ratingSummaries = SpotRatingRepository(supabase).getRatingSummaries()
            supabase
                .from("spots")
                .select()
                .decodeList<SpotDto>()
                .map { spot ->
                    spot.toPlace(
                        ratingSummary = spot.id?.let { ratingSummaries[it] },
                    )
                }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun SpotDto.toPlace(
        ratingSummary: SpotRatingSummary?,
    ): Place {
        val resolvedNoise = ratingSummary?.noise ?: noise
        val resolvedComfort = ratingSummary?.comfort ?: comfort
        val resolvedRefreshments = ratingSummary?.refreshments ?: refreshments
        val resolvedRating = ratingSummary?.let {
            ((it.noise + it.comfort + it.refreshments) / 3.0).round2()
        } ?: 4.5

        return Place(
            id = id?.toString() ?: "",
            name = name,
            description = description ?: "",
            rating = resolvedRating,
            noise = resolvedNoise,
            comfort = resolvedComfort,
            refreshments = resolvedRefreshments,
            openingHours = openingHours ?: "N/A",
            images = imageUrls,
            certified = certified,
            powerOutlet = ratingSummary?.powerOutlet ?: powerOutlet,
            coordinates = coordinates
        )
    }

    suspend fun getSpotsImageUrlsRaw(): Map<Long?, String> {
        return try {
            supabase
                .from("spots")
                .select()
                .decodeList<JsonObject>()
                .associate { json ->
                    val id = json["id"]?.toString()?.removeSurrounding("\"")?.toLongOrNull()
                    val rawImageUrls = json["photos"]?.toString() ?: "null"
                    id to rawImageUrls
                }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    suspend fun getSpotsByIds(spotIds: Set<Long>): List<Place> {
        if (spotIds.isEmpty()) return emptyList<Place>()
        val spots: List<Place> = getSpots()
        return spots
            .filter { spot -> spot.id.toLongOrNull() in spotIds }
    }

    suspend fun getSpotsExcludingIds(spotIds: Set<Long>): List<Place> {
        val spots: List<Place> = getSpots()
        if (spotIds.isEmpty()) return spots
        return spots
            .filter { spot -> spot.id.toLongOrNull() !in spotIds }
    }
}

private fun Double.round2(): Double = kotlin.math.round(this * 100) / 100
