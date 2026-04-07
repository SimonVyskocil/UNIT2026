package com.example.unit2026

import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory // (Nebo ktor2 podle toho, co jsi zjistil)
import platform.UIKit.UIUserInterfaceStyle
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.*

import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.useContents
import kotlinx.coroutines.launch
import platform.CoreLocation.*
import platform.MapKit.*
import platform.UIKit.UIImage
import platform.darwin.NSObject
import kotlin.math.roundToInt

// Pomocná funkce pro výpočet vzdálenosti na iOS
fun calculateDistanceIos(userLat: Double, userLng: Double, poiLat: Double, poiLng: Double): String {
    val userLoc = CLLocation(latitude = userLat, longitude = userLng)
    val poiLoc = CLLocation(latitude = poiLat, longitude = poiLng)
    val distanceInMeters = userLoc.distanceFromLocation(poiLoc)

    return if (distanceInMeters < 1000) {
        "${distanceInMeters.toInt()} m"
    } else {
        // Zástupce za String.format (který na iOS nefunguje)
        val km = (distanceInMeters / 100.0).roundToInt() / 10.0
        "$km km"
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalForeignApi::class)
@Composable
actual fun LocationsMapScreen() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
            }
            .build()
    }
    val scope = rememberCoroutineScope()
    val locationService = remember { LocationService() }

    var pois by remember { mutableStateOf<List<POI>>(emptyList()) }
    var selectedPoi by remember { mutableStateOf<POI?>(null) }
    var currentUserLocation by remember { mutableStateOf<CValue<CLLocationCoordinate2D>?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    // Reference na samotnou mapu, abychom k ní mohli skákat tlačítkem
    var mapViewRef by remember { mutableStateOf<MKMapView?>(null) }

    // iOS Manažer polohy
    val locationManager = remember { CLLocationManager() }

    // 1. Delegát pro Polohu (musí být v remember, jinak ho iOS smaže)
    val locationDelegate = remember {
        object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val location = didUpdateLocations.lastOrNull() as? CLLocation
                if (location != null) {
                    currentUserLocation = location.coordinate // Tady už by to mělo projít, protože .coordinate vrací CValue
                }
            }

            override fun locationManager(manager: CLLocationManager, didChangeAuthorizationStatus: Int) {
                if (didChangeAuthorizationStatus == kCLAuthorizationStatusAuthorizedWhenInUse ||
                    didChangeAuthorizationStatus == kCLAuthorizationStatusAuthorizedAlways) {
                    manager.startUpdatingLocation()
                }
            }
        }
    }

    // 2. Delegát pro Mapu (Klikání na piny)
    val mapDelegate = remember {
        object : NSObject(), MKMapViewDelegateProtocol {

            // 1. VYKRESLENÍ VLASTNÍCH BODŮ (A ZMENŠENÍ PINU)
            @Suppress("CONFLICTING_OVERLOADS")
            @ObjCSignatureOverride
            override fun mapView(mapView: MKMapView, viewForAnnotation: MKAnnotationProtocol): MKAnnotationView? {
                // Pokud jde o modrou tečku polohy uživatele, necháme výchozí systémový vzhled
                if (viewForAnnotation is MKUserLocation) return null

                val identifier = "CustomPin"
                var view = mapView.dequeueReusableAnnotationViewWithIdentifier(identifier)

                if (view == null) {
                    view = MKAnnotationView(viewForAnnotation, identifier)
                    view.canShowCallout = false // Zabráníme iOS bublině, máme vlastní BottomSheet
                } else {
                    view.annotation = viewForAnnotation
                }

                val originalImage = UIImage.imageNamed("custom_pin")
                if (originalImage != null) {

                    // 1. Nastavíme MAXIMÁLNÍ velikost (výšku nebo šířku), kterou pin smí mít
                    // Zkusil jsem 48.0, což je standardní profi velikost. Pokud chceš ještě větší, dej třeba 55.0.
                    val maxSize = 48.0

                    var finalWidth = maxSize
                    var finalHeight = maxSize

                    // 2. MAGIE: Vypočítáme poměr stran, aby se pin NESPLÁCNUL
                    // V Kotlin/Native musíme použít useContents, abychom se dostali k rozměrům C-struktury
                    originalImage.size.useContents {
                        val originalWidth = width
                        val originalHeight = height

                        if (originalWidth > originalHeight) {
                            // Obrázek je na šířku
                            finalHeight = (maxSize * originalHeight) / originalWidth
                        } else if (originalHeight > originalWidth) {
                            // Obrázek je na výšku
                            finalWidth = (maxSize * originalWidth) / originalHeight
                        }
                        // Pokud jsou stejné, zůstane maxSize x maxSize
                    }

                    // 3. Vykreslíme zmenšený obrázek na plátno s SPRÁVNÝM POMĚREM STRAN
                    val targetSize = CGSizeMake(finalWidth, finalHeight)
                    UIGraphicsBeginImageContextWithOptions(targetSize, false, 0.0) // 0.0 používá správné rozlišení displeje (Retina)
                    originalImage.drawInRect(CGRectMake(0.0, 0.0, finalWidth, finalHeight))
                    val resizedImage = UIGraphicsGetImageFromCurrentImageContext()
                    UIGraphicsEndImageContext()

                    view.image = resizedImage

                    // 4. POSUNUTÍ PINU (Kritické pro piny se špičkou dole!)
                    // Apple dává střed obrázku na souřadnici. Pokud má pin špičku dole,
                    // musíme ho posunout o polovinu výšky nahoru.
                    // CGPointMake(x_posun, y_posun). Záporné Y = posun nahoru.
                    view.centerOffset = platform.CoreGraphics.CGPointMake(0.0, -finalHeight / 2.0)
                }

                return view
            }

            // 2. KLIKNUTÍ NA PIN (OTEVŘENÍ PANELU)
            @Suppress("CONFLICTING_OVERLOADS")
            @ObjCSignatureOverride
            override fun mapView(mapView: MKMapView, didSelectAnnotationView: MKAnnotationView) {
                val annotation = didSelectAnnotationView.annotation
                if (annotation is MKPointAnnotation) {
                    // V subtitle máme schované ID z databáze
                    val idString = annotation.subtitle
                    val poiId = idString?.toIntOrNull()
                    if (poiId != null) {
                        selectedPoi = pois.find { it.id == poiId }
                        showSheet = true
                    }
                }
            }

            // 3. ODZNAČENÍ PINU (ZAVŘENÍ PANELU)
            @Suppress("CONFLICTING_OVERLOADS")
            @ObjCSignatureOverride
            override fun mapView(mapView: MKMapView, didDeselectAnnotationView: MKAnnotationView) {
                showSheet = false
                selectedPoi = null
            }
        }
    }

    // Nastartování služeb při otevření obrazovky
    LaunchedEffect(Unit) {
        locationManager.delegate = locationDelegate
        locationManager.requestWhenInUseAuthorization() // Požádá o povolení
        locationManager.startUpdatingLocation() // Začne sledovat polohu

        // Načte data ze Supabase
        pois = locationService.fetchLocations()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Nativní Apple Mapa vložená do Compose
        UIKitView(
            factory = {
                val map = MKMapView()
                map.showsUserLocation = true
                map.delegate = mapDelegate
                mapViewRef = map

                // 🌑 Vynucení Dark Mode
                map.overrideUserInterfaceStyle = UIUserInterfaceStyle.UIUserInterfaceStyleDark

                // 🧹 Skrytí všech výchozích bodů (památky, obchody, metro atd.)
                map.pointOfInterestFilter = MKPointOfInterestFilter.filterExcludingAllCategories()

                val pragueCenter = CLLocationCoordinate2DMake(50.0755, 14.4378)
                // Hodnoty 15000.0 znamenají šířku a výšku záběru v metrech (cca 15x15 km).
                // Pokud chceš mapu víc přiblížit, dej třeba 5000.0.
                val region = MKCoordinateRegionMakeWithDistance(pragueCenter, 15000.0, 15000.0)
                map.setRegion(region, animated = false)

                map
            },
            update = { map ->
                // 1. Najdeme všechny aktuální vlastní špendlíky na mapě
                val currentPins = map.annotations.filterIsInstance<MKPointAnnotation>()

                // 2. Překreslíme mapu JEN pokud se změnil počet bodů z databáze
                if (currentPins.size != pois.size) {
                    map.removeAnnotations(currentPins)

                    pois.forEach { poi ->
                        val annotation = MKPointAnnotation()
                        annotation.setCoordinate(CLLocationCoordinate2DMake(poi.latitude, poi.longitude))
                        annotation.setTitle(poi.name)
                        annotation.setSubtitle(poi.id.toString())
                        map.addAnnotation(annotation)
                    }
                    println("iOS Map: Vykresluji ${pois.size} bodů") // <-- Pomocný výpis
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Vlož toto někam do Boxu nad tlačítko FloatingActionButton:
        Text(
            text = "Počet bodů v databázi: ${pois.size}",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp)
                .background(Color.White)
                .padding(8.dp)
        )

        // Tlačítko Moje poloha
        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 32.dp, start = 16.dp),
            onClick = {
                currentUserLocation?.let { loc ->
                    // Zoomne mapu na uživatele (1500 metrů záběr)
                    val region = MKCoordinateRegionMakeWithDistance(loc, 1500.0, 1500.0)
                    mapViewRef?.setRegion(region, animated = true)
                } ?: run {
                    locationManager.requestWhenInUseAuthorization()
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
                    Text(
                        text = selectedPoi!!.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // --- ZOBRAZENÍ VZDÁLENOSTI PRO iOS ---
                    val distanceText = currentUserLocation?.useContents {
                        calculateDistanceIos(
                            userLat = latitude,
                            userLng = longitude,
                            poiLat = selectedPoi!!.latitude,
                            poiLng = selectedPoi!!.longitude
                        )
                    } ?: "Neznámá"

                    Text(
                        text = "📍 Vzdálenost: $distanceText",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    selectedPoi?.description?.let { desc ->
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

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