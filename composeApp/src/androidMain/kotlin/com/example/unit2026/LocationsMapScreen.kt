package com.example.unit2026

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

// Importy vlastních tříd z commonMain
import com.example.unit2026.POI
import com.example.unit2026.LocationService

// Importy pro Google Maps
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*

// Pomocná funkce pro výpočet vzdálenosti (vrací hezky naformátovaný text)
fun calculateDistance(userLat: Double, userLng: Double, poiLat: Double, poiLng: Double): String {
    val results = FloatArray(1)
    android.location.Location.distanceBetween(userLat, userLng, poiLat, poiLng, results)
    val distanceInMeters = results[0]

    return if (distanceInMeters < 1000) {
        "${distanceInMeters.toInt()} m"
    } else {
        "${String.format("%.1f", distanceInMeters / 1000f)} km"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
actual fun LocationsMapScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Inicializace služeb
    val locationService = remember { LocationService() }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Stavy pro Mapu a POI
    var pois by remember { mutableStateOf<List<POI>>(emptyList()) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var selectedPoi by remember { mutableStateOf<POI?>(null) }
    var customMarkerIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }

    // Zde ukládáme aktuální polohu uživatele pro výpočet vzdálenosti
    var currentUserLocation by remember { mutableStateOf<LatLng?>(null) }

    // Stavy pro Bottom Sheet (Vysouvací panel)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showSheet by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions.values.all { it }
    }

    // Načítání polohy (zavolá se při startu a vždy, když se změní povolení)
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentUserLocation = LatLng(it.latitude, it.longitude)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        MapsInitializer.initialize(context, MapsInitializer.Renderer.LATEST, object : OnMapsSdkInitializedCallback {
            override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
                Log.d("MapsSDK", "Renderer initialized: $renderer")
            }
        })

        // Kontrola oprávnění
        val fineLocation = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        hasLocationPermission = fineLocation == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        // Načtení bodů
        pois = locationService.fetchLocations()

        // Načtení custom pinu
        try {
            val resId = context.resources.getIdentifier("custom_pin", "drawable", context.packageName)
            if (resId != 0) {
                val bitmap = BitmapFactory.decodeResource(context.resources, resId)
                if (bitmap != null) {
                    val density = context.resources.displayMetrics.density
                    val height = (42 * density).toInt()
                    val width = (height * bitmap.width) / bitmap.height
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
                    customMarkerIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap)
                }
            }
        } catch (e: Exception) {
            Log.e("LocationsMapScreen", "Nenalezen custom_pin, pouzije se defaultni ikona.")
        }
    }

    val mapProperties by remember(hasLocationPermission) {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = hasLocationPermission,
                mapStyleOptions = try {
                    val resId = context.resources.getIdentifier("goon_maps_style", "raw", context.packageName)
                    if (resId != 0) MapStyleOptions.loadRawResourceStyle(context, resId) else null
                } catch (e: Exception) { null }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties.copy(isMyLocationEnabled = hasLocationPermission),
            uiSettings = uiSettings,
            onMapClick = {
                showSheet = false
                selectedPoi = null
            }
        ) {
            pois.forEach { poi ->
                Marker(
                    state = MarkerState(position = LatLng(poi.latitude, poi.longitude)),
                    title = poi.name,
                    icon = customMarkerIcon,
                    onClick = {
                        selectedPoi = poi
                        showSheet = true
                        true
                    }
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
                            val userLatLng = LatLng(it.latitude, it.longitude)
                            currentUserLocation = userLatLng // Uložení pozice pro výpočet
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(userLatLng, 15f)
                                )
                            }
                        }
                    }
                } else {
                    permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                }
            }
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Moje poloha")
        }

        // ==========================================
        // VYSOUVACÍ PANEL S INFORMACEMI (BottomSheet)
        // ==========================================
        if (showSheet && selectedPoi != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    showSheet = false
                    selectedPoi = null
                },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, bottom = 48.dp)
                ) {
                    // Jméno místa
                    Text(
                        text = selectedPoi!!.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // --- ZOBRAZENÍ VZDÁLENOSTI ---
                    val distanceText = if (currentUserLocation != null) {
                        calculateDistance(
                            userLat = currentUserLocation!!.latitude,
                            userLng = currentUserLocation!!.longitude,
                            poiLat = selectedPoi!!.latitude,
                            poiLng = selectedPoi!!.longitude
                        )
                    } else {
                        "Neznámá"
                    }

                    Text(
                        text = "📍 Vzdálenost: $distanceText",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Popis
                    selectedPoi?.description?.let { desc ->
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Zobrazení fotky (pokud existuje)
                    if (selectedPoi!!.photos.isNotEmpty()) {
                        AsyncImage(
                            model = selectedPoi!!.photos.first(),
                            contentDescription = "Foto místa",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Placeholder pro fotku
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Žádná fotka k dispozici",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}