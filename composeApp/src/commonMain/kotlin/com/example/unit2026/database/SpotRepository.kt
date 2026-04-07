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
            supabase
                .from("spots")
                .select()
                .decodeList<SpotDto>()
                .map { it.toPlace() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun SpotDto.toPlace(): Place {
        return Place(
            id = id?.toString() ?: "",
            name = name,
            description = description ?: "",
            rating = 4.5, // Default rating as it's missing in DTO
            noise = noise,
            comfort = comfort,
            refreshments = refreshments,
            openingHours = openingHours ?: "N/A",
            images = imageUrls,
            certified = certified,
            powerOutlet = powerOutlet,
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
}
