package com.example.unit2026

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.unit2026.presentation.AddStudySpotScreen

@Composable
@Preview
fun App() {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) unitNightColors else unitDayColors,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            AddStudySpotScreen(
                onSubmit = {},
            )
        }
    }
}

private val unitDayColors = lightColorScheme(
    primary = Color(0xFF6F4BF2),
    onPrimary = Color(0xFFFFFBF6),
    primaryContainer = Color(0xFFE6DDFF),
    onPrimaryContainer = Color(0xFF2A146E),
    secondary = Color(0xFF8D71FF),
    onSecondary = Color(0xFFFFFBF6),
    secondaryContainer = Color(0xFFEDE7FF),
    onSecondaryContainer = Color(0xFF342066),
    tertiary = Color(0xFFA483FF),
    onTertiary = Color(0xFFFFFBF6),
    surface = Color(0xFFFFFBF6),
    surfaceVariant = Color(0xFFF1E8DC),
    onSurface = Color(0xFF211D28),
    onSurfaceVariant = Color(0xFF6A6472),
    background = Color(0xFFF7F1E7),
    error = Color(0xFFC65B72),
)

private val unitNightColors = darkColorScheme(
    primary = Color(0xFFB79CFF),
    onPrimary = Color(0xFF25135D),
    primaryContainer = Color(0xFF3B286B),
    onPrimaryContainer = Color(0xFFE9E0FF),
    secondary = Color(0xFF9E83FF),
    onSecondary = Color(0xFF21124F),
    secondaryContainer = Color(0xFF46317E),
    onSecondaryContainer = Color(0xFFF0EAFF),
    tertiary = Color(0xFFC4AEFF),
    onTertiary = Color(0xFF2B174E),
    surface = Color(0xFF1A1A1F),
    surfaceVariant = Color(0xFF2C2C34),
    onSurface = Color(0xFFF4F0F8),
    onSurfaceVariant = Color(0xFFB9B2C4),
    background = Color(0xFF141418),
    error = Color(0xFFFFB4C0),
)
