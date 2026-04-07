package com.example.unit2026.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.unit2026.components.PlaceCard
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import unit2026.composeapp.generated.resources.Res
import unit2026.composeapp.generated.resources.compose_multiplatform

@Composable
fun GalleryScreen(
    savedPlaces: List<Place>,
    onDeletePlace: (Place) -> Unit
) {
    var selectedPlace by remember { mutableStateOf<Place?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (savedPlaces.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No saved places yet!", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(savedPlaces) { place ->
                    GalleryItem(
                        place = place,
                        onClick = { selectedPlace = place },
                        onDelete = { onDeletePlace(place) }
                    )
                }
            }
        }

        // Expanded view
        selectedPlace?.let { place ->
            Dialog(
                onDismissRequest = { selectedPlace = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f))
                        .clickable { selectedPlace = null },
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.padding(16.dp).clickable(enabled = false) {}) {
                        PlaceCard(place = place)
                    }
                }
            }
        }
    }
}

@Composable
fun GalleryItem(
    place: Place,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (place.images.isNotEmpty()) {
                AsyncImage(
                    model = place.images.first(),
                    contentDescription = place.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(Res.drawable.compose_multiplatform),
                    contentDescription = place.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }

            // Name overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(8.dp)
            ) {
                Text(
                    text = place.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

