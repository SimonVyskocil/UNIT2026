package com.example.unit2026.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.unit2026.components.PlaceCard
import com.example.unit2026.components.SwipeableCard
import kotlinx.coroutines.launch

@Composable
fun SwiperScreen(
    places: SnapshotStateList<Place>,
    isLoading: Boolean,
    hasLoadedInitialData: Boolean,
    onLoadingChange: (Boolean) -> Unit,
    onInitialLoadComplete: () -> Unit,
    onDismissPlace: (Place) -> Unit,
    onPlaceSaved: (Place) -> Unit,
    onRefreshPlaces: suspend () -> List<Place>,
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(hasLoadedInitialData) {
        if (!hasLoadedInitialData) {
            onLoadingChange(true)
            places.clear()
            places.addAll(onRefreshPlaces())
            onLoadingChange(false)
            onInitialLoadComplete()
        }
    }

    var currentOffsetX by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Left side overlay
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                .align(Alignment.CenterStart)
                .background(
                    Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0f to Color.Red.copy(
                                alpha = if (currentOffsetX < -50) (-currentOffsetX / 600f).coerceIn(0f, 0.5f) else 0f
                            ),
                            0.45f to Color.Red.copy(
                                alpha = if (currentOffsetX < -50) (-currentOffsetX / 600f).coerceIn(0f, 0.18f) else 0f
                            ),
                            1f to Color.Transparent,
                        )
                    )
                )
        )

        // Right side overlay
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                .align(Alignment.CenterEnd)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.primary.copy(
                                alpha = if (currentOffsetX > 50) (currentOffsetX / 600f).coerceIn(0f, 0.5f) else 0f
                            ),
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (places.isNotEmpty()) {
                // Show cards in reverse so the first one is on top
                places.asReversed().forEachIndexed { index, place ->
                    val isTopCard = index == places.size - 1
                    SwipeableCard(
                        onSwipeLeft = {
                            onDismissPlace(place)
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
                            scope.launch {
                                onLoadingChange(true)
                                places.clear()
                                places.addAll(onRefreshPlaces())
                                onLoadingChange(false)
                                onInitialLoadComplete()
                            }
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
}
