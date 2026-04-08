package com.example.unit2026
import androidx.compose.runtime.Composable

@Composable
expect fun LocationsMapScreen(
    selectedPlaceId: String? = null,
    onSelectedPlaceHandled: () -> Unit = {},
)
