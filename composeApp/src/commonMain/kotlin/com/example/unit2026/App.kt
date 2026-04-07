package com.example.unit2026

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.unit2026.database.AuthRepository
import com.example.unit2026.database.SpotRepository
import com.example.unit2026.database.SupabaseClientProvider
import com.example.unit2026.database.models.SpotDto
import com.example.unit2026.presentation.LoginScreen
import com.example.unit2026.presentation.SignUpScreen
import kotlinx.coroutines.launch

private enum class AppScreen {
    Login,
    SignUp,
    Home
}

@Composable
@Preview
fun App() {
    val authRepository = remember { AuthRepository(SupabaseClientProvider.client) }
    val spotRepository = remember { SpotRepository(SupabaseClientProvider.client) }

    var screen by remember {
        mutableStateOf(if (authRepository.isLoggedIn()) AppScreen.Home else AppScreen.Login)
    }
    var authMessage by remember { mutableStateOf<String?>(null) }
    var currentUserEmail by remember { mutableStateOf(authRepository.currentUserEmail()) }

    LaunchedEffect(screen) {
        currentUserEmail = authRepository.currentUserEmail()
    }

    MaterialTheme {
        when (screen) {
            AppScreen.Login -> LoginScreen(
                authRepository = authRepository,
                onLoginSuccess = {
                    authMessage = "Prihlaseni probehlo v poradku."
                    currentUserEmail = authRepository.currentUserEmail()
                    screen = AppScreen.Home
                },
                onGoToSignUp = {
                    authMessage = null
                    screen = AppScreen.SignUp
                }
            )

            AppScreen.SignUp -> SignUpScreen(
                authRepository = authRepository,
                onSignUpSuccess = {
                    currentUserEmail = authRepository.currentUserEmail()
                    if (authRepository.isLoggedIn()) {
                        authMessage = "Registrace probehla a session je aktivni."
                        screen = AppScreen.Home
                    } else {
                        authMessage = "Registrace probehla. Pokud mas potvrzeni emailu, prihlas se potom rucne."
                        screen = AppScreen.Login
                    }
                },
                onGoToLogin = {
                    authMessage = null
                    screen = AppScreen.Login
                }
            )

            AppScreen.Home -> HomeScreen(
                authRepository = authRepository,
                spotRepository = spotRepository,
                authMessage = authMessage,
                currentUserEmail = currentUserEmail,
                onLoggedOut = {
                    currentUserEmail = null
                    authMessage = "Byl jsi odhlasen."
                    screen = AppScreen.Login
                }
            )
        }
    }
}

@Composable
private fun HomeScreen(
    authRepository: AuthRepository,
    spotRepository: SpotRepository,
    authMessage: String?,
    currentUserEmail: String?,
    onLoggedOut: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var spots by remember { mutableStateOf<List<SpotDto>>(emptyList()) }
    var statusText by remember { mutableStateOf("Stiskni tlacitko pro nacteni spots.") }
    var isLoadingSpots by remember { mutableStateOf(false) }
    var isLoggingOut by remember { mutableStateOf(false) }
    var localMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Auth test screen",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = if (authRepository.isLoggedIn()) {
                "Session aktivni: ano"
            } else {
                "Session aktivni: ne"
            }
        )

        Text(
            text = "Uzivatel: ${currentUserEmail ?: "neznamy"}",
            modifier = Modifier.fillMaxWidth()
        )

        if (authMessage != null) {
            Text(authMessage)
        }

        if (localMessage != null) {
            Text(localMessage!!)
        }

        Button(
            onClick = {
                scope.launch {
                    isLoadingSpots = true
                    localMessage = null

                    val loadedSpots = spotRepository.getSpots()
                    spots = loadedSpots

                    statusText = if (loadedSpots.isEmpty()) {
                        "getSpots vratilo prazdny seznam."
                    } else {
                        "Nacteno spotu: ${loadedSpots.size}"
                    }

                    isLoadingSpots = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoadingSpots
        ) {
            Text(if (isLoadingSpots) "Nacitam spots..." else "Vyzkouset getSpots()")
        }

        Button(
            onClick = {
                scope.launch {
                    isLoggingOut = true
                    val error = authRepository.logout()
                    isLoggingOut = false

                    if (error == null) {
                        onLoggedOut()
                    } else {
                        localMessage = error
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoggingOut
        ) {
            Text(if (isLoggingOut) "Odhlasuju..." else "Odhlasit se")
        }

        Text(
            text = statusText,
            modifier = Modifier.fillMaxWidth()
        )

        spots.forEach { spot ->
            SpotCard(spot = spot)
        }
    }
}

@Composable
private fun SpotCard(spot: SpotDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = spot.name,
                style = MaterialTheme.typography.titleMedium
            )

            Text("id: ${spot.id ?: "-"}")
            Text("certified: ${spot.certified}")
            Text("noise: ${spot.noise}")
            Text("comfort: ${spot.comfort}")
            Text("refreshments: ${spot.refreshments}")

            if (!spot.description.isNullOrBlank()) {
                Text("description: ${spot.description}")
            }

            if (spot.imageUrls.isEmpty()) {
                Text("Bez obrazku")
            } else {
                spot.imageUrls.forEach { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Spot image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
