package com.example.unit2026.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
                    }

                    Text(
                        text = place.description,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        maxLines = 2,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MetricRow(label = "Noise", value = formatMetric(place.noise))
                        MetricRow(label = "Comfort", value = formatMetric(place.comfort))
                        MetricRow(label = "Snacks", value = formatMetric(place.refreshments))
                        MetricRow(
                            label = "Power outlet",
                            value = if (place.powerOutlet) "Yes" else "No",
                        )
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
private fun MetricRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.82f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun formatMetric(value: Double): String {
    val rounded = (value * 10).toInt() / 10.0
    return "$rounded/5"
}
