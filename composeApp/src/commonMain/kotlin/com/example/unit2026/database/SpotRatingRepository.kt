package com.example.unit2026.database

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpotRatingUpsertDto(
    @SerialName("spot_id")
    val spotId: Long,
    @SerialName("user_id")
    val userId: String,
    val noise: Int,
    val comfort: Int,
    val snacks: Int,
    @SerialName("power_outlet")
    val powerOutlet: Boolean
)

@Serializable
data class SpotRatingDto(
    @SerialName("spot_id")
    val spotId: Long,
    val noise: Int,
    val comfort: Int,
    val snacks: Int,
    @SerialName("power_outlet")
    val powerOutlet: Boolean
)

data class SpotRatingSummary(
    val noise: Double,
    val comfort: Double,
    val refreshments: Double,
    val powerOutlet: Boolean,
    val ratingsCount: Int
)

class SpotRatingRepository(
    private val supabase: SupabaseClient
) {

    suspend fun getRatingSummaries(): Map<Long, SpotRatingSummary> {
        return try {
            supabase
                .from("spot_ratings")
                .select()
                .decodeList<SpotRatingDto>()
                .groupBy { it.spotId }
                .mapValues { (_, ratings) ->
                    SpotRatingSummary(
                        noise = ratings.map { it.noise }.average().round2(),
                        comfort = ratings.map { it.comfort }.average().round2(),
                        refreshments = ratings.map { it.snacks }.average().round2(),
                        powerOutlet = ratings.count { it.powerOutlet } > ratings.size / 2.0,
                        ratingsCount = ratings.size
                    )
                }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    suspend fun upsertRating(
        spotId: Long,
        userId: String,
        noise: Int,
        comfort: Int,
        snacks: Int,
        powerOutlet: Boolean
    ): String? {
        if (noise !in 1..5) return "Noise musi byt 1 az 5"
        if (comfort !in 1..5) return "Comfort musi byt 1 az 5"
        if (snacks !in 1..5) return "Snacks musi byt 1 az 5"

        return try {
            supabase
                .from("spot_ratings")
                .upsert(
                    SpotRatingUpsertDto(
                        spotId = spotId,
                        userId = userId,
                        noise = noise,
                        comfort = comfort,
                        snacks = snacks,
                        powerOutlet = powerOutlet
                    )
                ) {
                    onConflict = "spot_id,user_id"
                }
            null
        } catch (e: Exception) {
            e.message ?: "Nepodarilo se odeslat hodnoceni"
        }
    }
}

private fun Double.round2(): Double = kotlin.math.round(this * 100) / 100
