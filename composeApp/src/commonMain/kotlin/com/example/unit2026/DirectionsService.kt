package com.example.unit2026
import com.example.unit2026.POI

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class DirectionsResponse(
    val routes: List<Route>
)

@Serializable
data class Route(
    val overview_polyline: Polyline
)

@Serializable
data class Polyline(
    val points: String
)

class DirectionsService(private val apiKey: String) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun getRoutePoints(origin: String, destination: String): String? {
        return try {
            val response: DirectionsResponse = client.get("https://maps.googleapis.com/maps/api/directions/json") {
                parameter("origin", origin)
                parameter("destination", destination)
                parameter("key", apiKey)
            }.body()
            
            response.routes.firstOrNull()?.overview_polyline?.points
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

data class MapLatLng(val latitude: Double, val longitude: Double)

fun decodePolyline(encoded: String): List<MapLatLng> {
    val poly = ArrayList<MapLatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng

        val p = MapLatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
        poly.add(p)
    }
    return poly
}
