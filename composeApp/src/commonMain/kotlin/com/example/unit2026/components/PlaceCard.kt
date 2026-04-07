package com.example.unit2026.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unit2026.presentation.Place
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import unit2026.composeapp.generated.resources.Res
import unit2026.composeapp.generated.resources.compose_multiplatform

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlaceCard(
    place: Place,
    modifier: Modifier = Modifier,
) {
    var currentImageIndex by remember { mutableIntStateOf(0) }
    val isDescriptionVisible = place.images.isEmpty() || currentImageIndex == 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(550.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Image with clickable to toggle
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        if (place.images.isNotEmpty()) {
                            currentImageIndex = (currentImageIndex + 1) % place.images.size
                        }
                    }
            ) {
                if (place.images.isNotEmpty()) {
                    AsyncImage(
                        model = place.images[currentImageIndex],
                        contentDescription = place.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Image(
                        painter = painterResource(Res.drawable.compose_multiplatform),
                        contentDescription = place.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
            }

            // Gradient Overlay
            if (isDescriptionVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                startY = 300f
                            )
                        )
                )
            }

            // Content
            if (isDescriptionVisible) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = place.name,
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (place.certified) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Certified",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        if (place.powerOutlet) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Power Outlets",
                                    tint = Color.Yellow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Power Outlets Available",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⭐",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = place.rating.toString(),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = place.description,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        maxLines = 2,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tags/Attributes
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoTag(label = "Noise: ${getNoiseLabel(place.noise)}")
                        InfoTag(label = "Comfort: ${getComfortLabel(place.comfort)}")
                        InfoTag(label = "Refreshments: ${getRefreshmentsLabel(place.refreshments)}")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "🕒 Open: ${place.openingHours}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }

            // Image Indicators
            if (place.images.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    repeat(place.images.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .clickable {
                                    currentImageIndex = index
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                modifier = Modifier.size(8.dp),
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = if (index == currentImageIndex) Color.White else Color.White.copy(alpha = 0.5f)
                            ) {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoTag(label: String) {
    Surface(
        color = Color.White.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun getNoiseLabel(noise: Double): String = when {
    noise <= 1.5 -> "Quiet"
    noise <= 2.5 -> "Moderate"
    noise <= 3.5 -> "Lively"
    noise <= 4.5 -> "Busy"
    noise <= 5.0 -> "Loud"
    else -> "Unknown"
}

private fun getComfortLabel(comfort: Double): String = when {
    comfort <= 1.5 -> "Basic"
    comfort <= 2.5 -> "Fine"
    comfort <= 3.5 -> "Good"
    comfort <= 4.5 -> "Great"
    comfort <= 5.0 -> "Excellent"
    else -> "Unknown"
}

private fun getRefreshmentsLabel(refreshments: Double): String = when {
    refreshments <= 1.5 -> "None"
    refreshments <= 2.5 -> "Snacks"
    refreshments <= 3.5 -> "Drinks"
    refreshments <= 4.5 -> "Cafe"
    refreshments <= 5.0 -> "Full Menu"
    else -> "Unknown"
}
