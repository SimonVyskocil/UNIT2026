package com.example.unit2026

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

// Pokud ti to na dalším řádku podtrhne POI červeně,
// odkomentuj ten data class pod tímto komentářem:
// data class POI(val id: Int, val name: String, val latitude: Double, val longitude: Double)

@Serializable
data class SpotDto(
    val id: Int? = null,
    val name: String,
    val coordinates: String,
    val description: String? = null
)

class LocationService {

    suspend fun fetchLocations(): List<POI> {
        // V KMP musíme použít Dispatchers.Default, protože IO tady neexistuje
        return withContext(Dispatchers.Default) {
            try {
                listOf(
                    POI(id = 1, name = "Start - Univerzita", latitude = 50.0755, longitude = 14.4378),
                    POI(id = 2, name = "Knihovna", latitude = 50.0760, longitude = 14.4390),
                    POI(id = 3, name = "Menza", latitude = 50.0750, longitude = 14.4370)
                )

                // =========================================================
                // 2. TVŮJ SUPABASE KÓD (Zatím schovaný, aby to neházelo errory)
                // Jakmile přidáš Supabase do Gradle, tohle odkomentuj
                // a smaž ta záchranná data nahoře.
                // =========================================================
                /*
                val client = SupabaseClientProvider.client
                val spots = client.from("spots").select().decodeList<SpotDto>()

                spots.mapNotNull { spot ->
                    val coords = spot.coordinates.split(",")
                    if (coords.size == 2) {
                        val lat = coords[0].trim().toDoubleOrNull()
                        val lng = coords[1].trim().toDoubleOrNull()
                        if (lat != null && lng != null) {
                            POI(
                                id = spot.id ?: 0,
                                name = spot.name,
                                latitude = lat,
                                longitude = lng
                            )
                        } else null
                    } else null
                }
                */

            } catch (e: Exception) {
                // V KMP je lepší použít println místo e.printStackTrace()
                println("Chyba při načítání dat: ${e.message}")
                emptyList()
            }
        }
    }
}