package com.example.unit2026.database

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class SpotSwipeAction(val value: String) {
    Liked("liked"),
    Disliked("disliked");

    companion object {
        fun fromValue(value: String?): SpotSwipeAction? = entries.firstOrNull { it.value == value }
    }
}

@Serializable
data class SpotSwipeUpsertDto(
    @SerialName("spot_id")
    val spotId: Long,
    @SerialName("user_id")
    val userId: String,
    val action: String
)

@Serializable
data class SpotSwipeDto(
    @SerialName("spot_id")
    val spotId: Long,
    @SerialName("user_id")
    val userId: String,
    val action: String
)

class SpotSwipeRepository(
    private val supabase: SupabaseClient
) {

    suspend fun upsertSwipe(
        spotId: Long,
        userId: String,
        action: SpotSwipeAction
    ): String? {
        return try {
            supabase
                .from("spot_swipes")
                .upsert(
                    SpotSwipeUpsertDto(
                        spotId = spotId,
                        userId = userId,
                        action = action.value
                    )
                ) {
                    onConflict = "user_id,spot_id"
                }
            null
        } catch (e: Exception) {
            e.message ?: "Nepodarilo se ulozit swipe"
        }
    }

    suspend fun getSwipesForUser(userId: String): List<SpotSwipeDto> {
        return try {
            supabase
                .from("spot_swipes")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<SpotSwipeDto>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getSwipedSpotIds(userId: String): Set<Long> {
        return getSwipesForUser(userId)
            .map { it.spotId }
            .toSet()
    }

    suspend fun getLikedSpotIds(userId: String): Set<Long> {
        return getSwipesForUser(userId)
            .filter { SpotSwipeAction.fromValue(it.action) == SpotSwipeAction.Liked }
            .map { it.spotId }
            .toSet()
    }
}
