package com.example.unit2026

import com.example.unit2026.database.SupabaseClientProvider
import com.example.unit2026.database.models.SpotDto
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationService {

    suspend fun fetchLocations(): List<POI> {
        // V KMP musíme použít Dispatchers.Default, protože IO tady neexistuje
        return withContext(Dispatchers.Default) {
            try {
                val client = SupabaseClientProvider.client
                val spots = client.from("spots").select().decodeList<SpotDto>()

                spots.mapNotNull { spot ->
                    val coords = spot.coordinates.split(",")
                    if (coords.size == 2) {
                        val lat = coords[0].trim().toDoubleOrNull()
                        val lng = coords[1].trim().toDoubleOrNull()
                        if (lat != null && lng != null) {
                            POI(
                                id = spot.id?.toInt() ?: 0,
                                name = spot.name,
                                latitude = lat,
                                longitude = lng,
                                description = spot.description,
                                photos = spot.imageUrls
                            )
                        } else null
                    } else null
                }
            } catch (e: Exception) {
                // V KMP je lepší použít println místo e.printStackTrace()
                println("Chyba při načítání dat: ${e.message}")
                emptyList()
            }
        }
    }
}
