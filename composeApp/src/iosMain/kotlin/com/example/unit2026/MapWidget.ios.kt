package com.example.unit2026

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun MapWidget(
    modifier: Modifier,
    lat: Double,
    lng: Double,
    pois: List<POI>,
    onPoiClick: (POI) -> Unit
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("iOS Map Placeholder")
    }
}
