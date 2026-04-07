package com.example.unit2026

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun MapWidget(
    modifier: Modifier = Modifier,
    lat: Double,
    lng: Double,
    pois: List<POI> = emptyList(),
    onPoiClick: (POI) -> Unit = {}
)
