package com.example.unit2026

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
// Importy tvých vlastních tříd z commonMain
import com.example.unit2026.POI
import com.example.unit2026.DirectionsService
import com.example.unit2026.LocationService
import com.example.unit2026.decodePolyline
// Import Android Mapy
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@Composable
fun LocationsMapScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Inicializace služeb
    val locationService = remember { LocationService() }
    val apiKey = "AIzaSyB7so83JXHEDecCxYQT_5UOWumqc-lhJi8" // Pozor, API klíč by neměl být v kódu, ale pro hackathon OK
    val directionsService = remember { DirectionsService(apiKey) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var pois by remember { mutableStateOf<List<POI>>(emptyList()) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var selectedPoi by remember { mutableStateOf<POI?>(null) }
    var isNavigating by remember { mutableStateOf(false) }
    var customMarkerIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions.values.all { it }
    }

    LaunchedEffect(Unit) {
        MapsInitializer.initialize(context, MapsInitializer.Renderer.LATEST, object : OnMapsSdkInitializedCallback {
            override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
                Log.d("MapsSDK", "Renderer initialized: $renderer")
            }
        })

        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        // Načtení dat ze služby
        pois = locationService.fetchLocations()

        // Načtení custom ikony (ošetřeno proti pádu)
        try {
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.custom_pin)
            if (bitmap != null) {
                val density = context.resources.displayMetrics.density
                val height = (42 * density).toInt()
                val width = (height * bitmap.width) / bitmap.height
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
                customMarkerIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap)
            }
        } catch (e: Exception) {
            Log.e("LocationsMapScreen", "Error loading custom pin", e)
        }
    }

    val mapProperties by remember(hasLocationPermission) {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = hasLocationPermission,
                mapStyleOptions = try {
                    MapStyleOptions.loadRawResourceStyle(context, R.raw.goon_maps_style)
                } catch (e: Exception) {
                    null
                }
            )
        )
    }

    val uiSettings by remember {
        mutableStateOf(MapUiSettings(myLocationButtonEnabled = false, mapToolbarEnabled = false))
    }

    val prague = LatLng(50.0755, 14.4378)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(prague, 10f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = uiSettings,
            onMapClick = {
                selectedPoi = null
                isNavigating = false
                routePoints = emptyList()
            }
        ) {
            pois.forEach { poi ->
                Marker(
                    state = MarkerState(position = LatLng(poi.latitude, poi.longitude)),
                    title = poi.name,
                    icon = customMarkerIcon,
                    onClick = {
                        selectedPoi = poi
                        isNavigating = false
                        routePoints = emptyList()
                        false
                    }
                )
            }

            if (isNavigating && routePoints.isNotEmpty()) {
                // Vrstva stínu pod čarou
                Polyline(
                    points = routePoints,
                    color = Color(0x1A000000),
                    width = 45f,
                    startCap = RoundCap(),
                    endCap = RoundCap(),
                    jointType = JointType.ROUND,
                    zIndex = 1f
                )

                // Hlavní bílá čára ("White Sauce")
                Polyline(
                    points = routePoints,
                    color = Color(0xFFFDFDFD),
                    width = 30f,
                    startCap = RoundCap(),
                    endCap = RoundCap(),
                    jointType = JointType.ROUND,
                    zIndex = 2f
                )
            }
        }

        // Tlačítko Moje poloha
        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 32.dp, start = 16.dp),
            onClick = {
                if (hasLocationPermission) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f)
                                )
                            }
                        }
                    }
                }
            }
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "My Location")
        }

        // Navigační panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(visible = selectedPoi != null || isNavigating) {
                if (!isNavigating) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            selectedPoi?.let { poi ->
                                if (hasLocationPermission) {
                                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                        location?.let {
                                            scope.launch {
                                                val origin = "${it.latitude},${it.longitude}"
                                                val destination = "${poi.latitude},${poi.longitude}"
                                                val pointsString = directionsService.getRoutePoints(origin, destination)
                                                if (pointsString != null) {
                                                    routePoints = decodePolyline(pointsString).map { p -> LatLng(p.latitude, p.longitude) }
                                                    isNavigating = true
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.Navigation, contentDescription = null) },
                        text = { Text("Navigovat") }
                    )
                } else {
                    Button(
                        onClick = {
                            isNavigating = false
                            routePoints = emptyList()
                            selectedPoi = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Zrušit trasu")
                    }
                }
            }
        }
    }
}