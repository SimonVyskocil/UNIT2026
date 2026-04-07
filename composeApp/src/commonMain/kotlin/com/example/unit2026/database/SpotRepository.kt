package com.example.unit2026.database
import com.example.unit2026.database.models.SpotDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.JsonObject

class SpotRepository(
    private val supabase: SupabaseClient
) {

    suspend fun getSpots(): List<SpotDto> {
        return try {
            supabase
                .from("spots")
                .select()
                .decodeList<SpotDto>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
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

    suspend fun getSpotsByIds(spotIds: Set<Long>): List<SpotDto> {
        if (spotIds.isEmpty()) return emptyList()
        return getSpots()
            .filter { spot -> spot.id in spotIds }
    }

    suspend fun getSpotsExcludingIds(spotIds: Set<Long>): List<SpotDto> {
        if (spotIds.isEmpty()) return getSpots()
        return getSpots()
            .filter { spot -> spot.id !in spotIds }
    }
}
