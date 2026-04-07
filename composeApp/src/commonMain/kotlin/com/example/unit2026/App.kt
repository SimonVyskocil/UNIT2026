package com.example.unit2026

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.unit2026.database.AuthRepository
import com.example.unit2026.database.SpotRepository
import com.example.unit2026.database.SupabaseClientProvider
import com.example.unit2026.presentation.GalleryScreen
import com.example.unit2026.presentation.LoginScreen
import com.example.unit2026.presentation.Place
import com.example.unit2026.presentation.SignUpScreen
import com.example.unit2026.presentation.SwiperScreen

val LightColorPalette = lightColorScheme(
    primary = Color(0xFF6F4BF2), // fialová
    onPrimary = Color.White,
    background = Color(0xFFFFFBF6), // světlá
    onBackground = Color(0xFF141418), // tmavá
    surface = Color(0xFFFFFBF6),
    onSurface = Color(0xFF141418),
)

val DarkColorPalette = darkColorScheme(
    primary = Color(0xFF6F4BF2), // fialová
    onPrimary = Color.White,
    background = Color(0xFF141418), // tmavá
    onBackground = Color(0xFFFFFBF6), // světlá
    surface = Color(0xFF141418),
    onSurface = Color(0xFFFFFBF6),
)

@Composable
@Preview
fun App() {
    val authRepository = remember { AuthRepository(SupabaseClientProvider.client) }
    val spotRepository = remember { SpotRepository(SupabaseClientProvider.client) }
    var currentScreen by remember { mutableStateOf(if (authRepository.isLoggedIn()) "swiper" else "login") }
    val savedPlaces = remember { mutableStateListOf<Place>() }

    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColorPalette else LightColorPalette
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            when (currentScreen) {
                "login" -> LoginScreen(
                    authRepository = authRepository,
                    onLoginSuccess = { currentScreen = "swiper" },
                    onGoToSignUp = { currentScreen = "signup" }
                )
                "signup" -> SignUpScreen(
                    authRepository = authRepository,
                    onSignUpSuccess = {
                        if (authRepository.isLoggedIn()) {
                            currentScreen = "swiper"
                        } else {
                            currentScreen = "login"
                        }
                    },
                    onGoToLogin = { currentScreen = "login" }
                )
                else -> {
                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentScreen == "swiper",
                                    onClick = { currentScreen = "swiper" },
                                    icon = { Icon(Icons.Default.ThumbUp, contentDescription = "Swipe") },
                                    label = { Text("Swipe") }
                                )
                                NavigationBarItem(
                                    selected = currentScreen == "gallery",
                                    onClick = { currentScreen = "gallery" },
                                    icon = { Icon(Icons.Default.List, contentDescription = "Gallery") },
                                    label = { Text("Gallery") }
                                )
                            }
                        }
                    ) { paddingValues ->
                        Box(modifier = Modifier.padding(paddingValues)) {
                            when (currentScreen) {
                                "swiper" -> SwiperScreen(
                                    spotRepository = spotRepository,
                                    onPlaceSaved = { place ->
                                        if (!savedPlaces.contains(place)) {
                                            savedPlaces.add(place)
                                        }
                                    }
                                )
                                "gallery" -> GalleryScreen(
                                    savedPlaces = savedPlaces,
                                    onDeletePlace = { savedPlaces.remove(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}