package com.example.unit2026.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.unit2026.components.PlaceCard
import com.example.unit2026.components.SwipeableCard

@Composable
fun SwiperScreen(onPlaceSaved: (Place) -> Unit) {
    val places = remember {
        mutableStateListOf(
            Place(
                id = "1",
                name = "Cozy Cafe",
                description = "A very quiet and comfortable place to study or work with great coffee.",
                rating = 4.8,
                noise = "Low",
                comfort = "Excellent",
                snacksAvailability = "Available",
                openingHours = "08:00 - 20:00",
                images = listOf("cafe1", "cafe2")
            ),
            Place(
                id = "2",
                name = "Modern Library",
                description = "Spacious library with high-speed internet and many power outlets.",
                rating = 4.5,
                noise = "Minimal",
                comfort = "Good",
                snacksAvailability = "None",
                openingHours = "09:00 - 22:00",
                images = listOf("lib1", "lib2")
            ),
            Place(
                id = "3",
                name = "Urban Hub",
                description = "Trendy coworking space in the city center with a vibrant atmosphere.",
                rating = 4.2,
                noise = "Medium",
                comfort = "Superior",
                snacksAvailability = "Full Menu",
                openingHours = "24/7",
                images = listOf("hub1")
            )
        )
    }

    var currentOffsetX by remember { mutableStateOf(0f) }

    val backgroundColor = when {
        currentOffsetX > 50 -> MaterialTheme.colorScheme.primary.copy(alpha = (currentOffsetX / 600f).coerceIn(0f, 0.5f))
        currentOffsetX < -50 -> Color.Red.copy(alpha = (-currentOffsetX / 600f).coerceIn(0f, 0.5f))
        else -> MaterialTheme.colorScheme.background
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (places.isNotEmpty()) {
            // Show cards in reverse so the first one is on top
            places.asReversed().forEachIndexed { index, place ->
                val isTopCard = index == places.size - 1
                SwipeableCard(
                    onSwipeLeft = {
                        places.remove(place)
                        currentOffsetX = 0f
                    },
                    onSwipeRight = {
                        onPlaceSaved(place)
                        places.remove(place)
                        currentOffsetX = 0f
                        println("Saved: ${place.name}")
                    },
                    onOffsetChanged = { offset ->
                        if (isTopCard) {
                            currentOffsetX = offset
                        }
                    }
                ) {
                    PlaceCard(place = place)
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No more places!",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        // Reset for demo
                        places.addAll(
                            listOf(
                                Place("1", "Cozy Cafe", "A very quiet and comfortable place to study or work with great coffee.", 4.8, "Low", "Excellent", "Available", "08:00 - 20:00", listOf("cafe1", "cafe2")),
                                Place("2", "Modern Library", "Spacious library with high-speed internet and many power outlets.", 4.5, "Minimal", "Good", "None", "09:00 - 22:00", listOf("lib1", "lib2")),
                                Place("3", "Urban Hub", "Trendy coworking space in the city center with a vibrant atmosphere.", 4.2, "Medium", "Superior", "Full Menu", "24/7", listOf("hub1"))
                            )
                        )
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Refresh Places")
                }
            }
        }
    }
}