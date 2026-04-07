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
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import com.example.unit2026.database.SpotRatingRepository
import com.example.unit2026.database.SpotRatingSummary
import com.example.unit2026.database.SpotSwipeAction
import com.example.unit2026.database.SpotSwipeRepository
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
    val spotRatingRepository = remember { SpotRatingRepository(SupabaseClientProvider.client) }
    val spotSwipeRepository = remember { SpotSwipeRepository(SupabaseClientProvider.client) }

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
                spotRatingRepository = spotRatingRepository,
                spotSwipeRepository = spotSwipeRepository,
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
    spotRatingRepository: SpotRatingRepository,
    spotSwipeRepository: SpotSwipeRepository,
    authMessage: String?,
    currentUserEmail: String?,
    onLoggedOut: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var spots by remember { mutableStateOf<List<SpotDto>>(emptyList()) }
    var ratingSummaries by remember { mutableStateOf<Map<Long, SpotRatingSummary>>(emptyMap()) }
    var statusText by remember { mutableStateOf("Stiskni tlacitko pro nacteni spots.") }
    var isLoadingSpots by remember { mutableStateOf(false) }
    var isLoggingOut by remember { mutableStateOf(false) }
    var localMessage by remember { mutableStateOf<String?>(null) }

    fun requireUserId(): String? {
        val userId = authRepository.currentUserId()
        if (userId == null) {
            localMessage = "Chybi user id"
        }
        return userId
    }

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
                    val loadedSummaries = spotRatingRepository.getRatingSummaries()
                    spots = loadedSpots
                    ratingSummaries = loadedSummaries

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
                val userId = requireUserId() ?: return@Button
                scope.launch {
                    isLoadingSpots = true
                    localMessage = null

                    val swipedSpotIds = spotSwipeRepository.getSwipedSpotIds(userId)
                    val loadedSpots = spotRepository.getSpotsExcludingIds(swipedSpotIds)
                    val loadedSummaries = spotRatingRepository.getRatingSummaries()
                    spots = loadedSpots
                    ratingSummaries = loadedSummaries
                    statusText = "Nacteno neswipnutych spotu: ${loadedSpots.size}"
                    isLoadingSpots = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoadingSpots
        ) {
            Text(if (isLoadingSpots) "Nacitam..." else "Nacist neswipnute spoty")
        }

        Button(
            onClick = {
                val userId = requireUserId() ?: return@Button
                scope.launch {
                    isLoadingSpots = true
                    localMessage = null

                    val likedSpotIds = spotSwipeRepository.getLikedSpotIds(userId)
                    val loadedSpots = spotRepository.getSpotsByIds(likedSpotIds)
                    val loadedSummaries = spotRatingRepository.getRatingSummaries()
                    spots = loadedSpots
                    ratingSummaries = loadedSummaries
                    statusText = "Nacteno liked spotu: ${loadedSpots.size}"
                    isLoadingSpots = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoadingSpots
        ) {
            Text(if (isLoadingSpots) "Nacitam..." else "Nacist liked spoty")
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
            SpotCard(
                spot = spot,
                ratingSummary = spot.id?.let { ratingSummaries[it] },
                onSwipe = { action ->
                    val userId = authRepository.currentUserId()
                    val spotId = spot.id

                    if (userId == null || spotId == null) {
                        localMessage = "Chybi user nebo spot id"
                        return@SpotCard
                    }

                    scope.launch {
                        val error = spotSwipeRepository.upsertSwipe(
                            spotId = spotId,
                            userId = userId,
                            action = action
                        )

                        if (error == null) {
                            localMessage = when (action) {
                                SpotSwipeAction.Liked -> "Spot ulozen do liked."
                                SpotSwipeAction.Disliked -> "Spot oznacen jako swipnuty."
                            }
                            val swipedSpotIds = spotSwipeRepository.getSwipedSpotIds(userId)
                            spots = spotRepository.getSpotsExcludingIds(swipedSpotIds)
                        } else {
                            localMessage = error
                        }
                    }
                },
                onRateSpot = { noise, comfort, snacks, powerOutlet ->
                    val userId = authRepository.currentUserId()
                    val spotId = spot.id

                    if (userId == null || spotId == null) {
                        localMessage = "Chybi user nebo spot id"
                        return@SpotCard
                    }

                    scope.launch {
                        val error = spotRatingRepository.upsertRating(
                            spotId = spotId,
                            userId = userId,
                            noise = noise,
                            comfort = comfort,
                            snacks = snacks,
                            powerOutlet = powerOutlet
                        )

                        if (error == null) {
                            localMessage = "Hodnoceni ulozeno."
                            spots = spotRepository.getSpots()
                            ratingSummaries = spotRatingRepository.getRatingSummaries()
                        } else {
                            localMessage = error
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun SpotCard(
    spot: SpotDto,
    ratingSummary: SpotRatingSummary?,
    onSwipe: (SpotSwipeAction) -> Unit,
    onRateSpot: (noise: Int, comfort: Int, snacks: Int, powerOutlet: Boolean) -> Unit
) {
    val displayedNoise = ratingSummary?.noise ?: spot.noise
    val displayedComfort = ratingSummary?.comfort ?: spot.comfort
    val displayedRefreshments = ratingSummary?.refreshments ?: spot.refreshments
    val displayedPowerOutlet = ratingSummary?.powerOutlet ?: spot.powerOutlet

    var noiseRating by remember(spot.id) { mutableStateOf(displayedNoise.coerceIn(1.0, 5.0).toFloat()) }
    var comfortRating by remember(spot.id) { mutableStateOf(displayedComfort.coerceIn(1.0, 5.0).toFloat()) }
    var snacksRating by remember(spot.id) { mutableStateOf(displayedRefreshments.coerceIn(1.0, 5.0).toFloat()) }
    var powerOutletRating by remember(spot.id) { mutableStateOf(displayedPowerOutlet) }

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
            Text("noise: $displayedNoise")
            Text("comfort: $displayedComfort")
            Text("refreshments: $displayedRefreshments")
            Text("power outlet: ${if (displayedPowerOutlet) "ano" else "ne"}")
            Text("ratings count: ${ratingSummary?.ratingsCount ?: 0}")

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

            Text("Tvoje hodnoceni")
            Text("Noise: ${noiseRating.toInt()}")
            Slider(
                value = noiseRating,
                onValueChange = { noiseRating = it },
                valueRange = 1f..5f,
                steps = 3
            )

            Text("Comfort: ${comfortRating.toInt()}")
            Slider(
                value = comfortRating,
                onValueChange = { comfortRating = it },
                valueRange = 1f..5f,
                steps = 3
            )

            Text("Snacks: ${snacksRating.toInt()}")
            Slider(
                value = snacksRating,
                onValueChange = { snacksRating = it },
                valueRange = 1f..5f,
                steps = 3
            )

            Text("Power outlet: ${if (powerOutletRating) "ano" else "ne"}")
            Switch(
                checked = powerOutletRating,
                onCheckedChange = { powerOutletRating = it }
            )

            Button(
                onClick = { onSwipe(SpotSwipeAction.Liked) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Like")
            }

            Button(
                onClick = { onSwipe(SpotSwipeAction.Disliked) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dislike")
            }

            Button(
                onClick = {
                    onRateSpot(
                        noiseRating.toInt(),
                        comfortRating.toInt(),
                        snacksRating.toInt(),
                        powerOutletRating
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Odeslat hodnoceni")
            }
        }
    }
}
