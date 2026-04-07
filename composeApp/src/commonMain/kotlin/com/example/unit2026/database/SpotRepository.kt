package com.example.unit2026.database
import com.example.unit2026.database.models.SpotDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

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
}