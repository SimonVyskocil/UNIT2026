package com.example.unit2026.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
    var isDescriptionVisible by remember { mutableStateOf(true) }

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
                        isDescriptionVisible = !isDescriptionVisible
                        currentImageIndex = (currentImageIndex + 1) % place.images.size
                    }
            ) {
                Image(
                    painter = painterResource(Res.drawable.compose_multiplatform), // Improvising with default
                    contentDescription = place.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    colorFilter = when (currentImageIndex % 3) {
                        0 -> null
                        1 -> androidx.compose.ui.graphics.ColorFilter.tint(Color.Cyan, blendMode = androidx.compose.ui.graphics.BlendMode.Color)
                        else -> androidx.compose.ui.graphics.ColorFilter.tint(Color.Magenta, blendMode = androidx.compose.ui.graphics.BlendMode.Color)
                    }
                )
                
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = place.name,
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⭐", // Using emoji as fallback if icons are being stubborn
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
                        InfoTag(label = "Noise: ${place.noise}")
                        InfoTag(label = "Comfort: ${place.comfort}")
                        InfoTag(label = "Snacks: ${place.snacksAvailability}")
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
                                    println("Switched to image index: $index")
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
