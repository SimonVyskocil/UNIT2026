package com.example.unit2026

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
actual fun MapWidget(
    modifier: Modifier,
    lat: Double,
    lng: Double,
    pois: List<POI>,
    onPoiClick: (POI) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(lat, lng), 10f)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState
    ) {
        pois.forEach { poi ->
            Marker(
                state = MarkerState(position = LatLng(poi.latitude, poi.longitude)),
                title = poi.name,
                onClick = {
                    onPoiClick(poi)
                    true
                }
            )
        }
    }
}
