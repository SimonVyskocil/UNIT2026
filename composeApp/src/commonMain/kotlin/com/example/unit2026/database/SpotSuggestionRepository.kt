package com.example.unit2026.database

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class SpotSuggestionInsertDto(
    val name: String,
    val comment: String,
    @SerialName("user_id")
    val userId: String,
)

class SpotSuggestionRepository(
    private val supabase: SupabaseClient
) {
    suspend fun submitSuggestion(
        name: String,
        comment: String,
        userId: String,
    ): String? {
        if (name.isBlank()) return "Vypln nazev mista"

        return try {
            supabase
                .from("spot_suggestions")
                .insert(
                    SpotSuggestionInsertDto(
                        name = name.trim(),
                        comment = comment.trim(),
                        userId = userId,
                    )
                )
            null
        } catch (e: Exception) {
            e.message ?: "Nepodarilo se odeslat navrh mista"
        }
    }
}
