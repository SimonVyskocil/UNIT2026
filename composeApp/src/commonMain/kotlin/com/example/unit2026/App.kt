package com.example.unit2026

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.example.unit2026.database.AuthRepository
import com.example.unit2026.database.SpotRepository
import com.example.unit2026.database.SpotRatingRepository
import com.example.unit2026.database.SpotSwipeAction
import com.example.unit2026.database.SpotSwipeRepository
import com.example.unit2026.database.SupabaseClientProvider
import com.example.unit2026.presentation.GalleryScreen
import com.example.unit2026.presentation.LoginScreen
import com.example.unit2026.presentation.Place
import com.example.unit2026.presentation.SignUpScreen
import com.example.unit2026.presentation.SwiperScreen
import com.example.unit2026.presentation.UserAccountUi
import com.example.unit2026.presentation.UserProfileScreen
import com.example.unit2026.presentation.VisitFeedbackDraft
import com.example.unit2026.presentation.VisitFeedbackPrompt
import kotlinx.coroutines.launch

@Composable
@Preview
fun App() {
    val systemDarkMode = isSystemInDarkTheme()
    var isDarkMode by remember(systemDarkMode) { mutableStateOf(systemDarkMode) }
    val authRepository = remember { AuthRepository(SupabaseClientProvider.client) }
    var authVersion by remember { mutableIntStateOf(0) }
    val refreshAuthState = remember {
        { authVersion += 1 }
    }
    val isLoggedIn = remember(authVersion) {
        authRepository.isLoggedIn()
    }

    CompositionLocalProvider(
        LocalIsDarkMode provides isDarkMode,
        LocalSetDarkMode provides { value -> isDarkMode = value },
        LocalRefreshAuth provides refreshAuthState,
    ) {
        MaterialTheme(
            colorScheme = if (isDarkMode) unitNightColors else unitDayColors,
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                if (isLoggedIn) {
                    Navigator(AppRootScreen)
                } else {
                    AuthGate(
                        authRepository = authRepository,
                        onAuthSuccess = refreshAuthState,
                    )
                }
            }
        }
    }
}

private val LocalIsDarkMode = compositionLocalOf { false }
private val LocalSetDarkMode = compositionLocalOf<(Boolean) -> Unit> { {} }
private val LocalRefreshAuth = compositionLocalOf<() -> Unit> { {} }
private val LocalSpotRepository = compositionLocalOf<SpotRepository> {
    error("SpotRepository not provided")
}
private val LocalSpotSwipeRepository = compositionLocalOf<SpotSwipeRepository> {
    error("SpotSwipeRepository not provided")
}
private val LocalCurrentUserId = compositionLocalOf<String?> { null }
private val LocalSavedPlaces = compositionLocalOf<SnapshotStateList<Place>> {
    mutableStateListOf()
}
private val LocalDiscoverPlaces = compositionLocalOf<SnapshotStateList<Place>> {
    mutableStateListOf()
}
private val LocalIsDiscoverLoading = compositionLocalOf<MutableState<Boolean>> {
    mutableStateOf(false)
}
private val LocalHasLoadedDiscover = compositionLocalOf<MutableState<Boolean>> {
    mutableStateOf(false)
}
private val LocalRefreshDiscover = compositionLocalOf<suspend () -> List<Place>> { { emptyList() } }
private val LocalOnDismissPlace = compositionLocalOf<(Place) -> Unit> { {} }
private val LocalOnSavePlace = compositionLocalOf<(Place) -> Unit> { {} }
private val LocalOnDeleteSavedPlace = compositionLocalOf<(Place) -> Unit> { {} }
private val AppRootScreen: Screen = UnitShellScreen

private enum class AuthMode {
    Login,
    SignUp,
}

@Composable
private fun AuthGate(
    authRepository: AuthRepository,
    onAuthSuccess: () -> Unit,
) {
    var authMode by remember { mutableStateOf(AuthMode.Login) }

    when (authMode) {
        AuthMode.Login -> LoginScreen(
            authRepository = authRepository,
            onLoginSuccess = onAuthSuccess,
            onGoToSignUp = { authMode = AuthMode.SignUp },
        )

        AuthMode.SignUp -> SignUpScreen(
            authRepository = authRepository,
            onSignUpSuccess = onAuthSuccess,
            onGoToLogin = { authMode = AuthMode.Login },
        )
    }
}

private object UnitShellScreen : Screen {
    @Composable
    override fun Content() {
        var showVisitFeedback by remember { mutableStateOf(false) }
        val authRepository = remember { AuthRepository(SupabaseClientProvider.client) }
        val spotRepository = remember { SpotRepository(SupabaseClientProvider.client) }
        val spotRatingRepository = remember { SpotRatingRepository(SupabaseClientProvider.client) }
        val spotSwipeRepository = remember { SpotSwipeRepository(SupabaseClientProvider.client) }
        val currentUserId = remember { authRepository.currentUserId() }
        val scope = rememberCoroutineScope()
        val savedPlaces = remember { mutableStateListOf<Place>() }
        val discoverPlaces = remember { mutableStateListOf<Place>() }
        val isDiscoverLoading = remember { mutableStateOf(false) }
        val hasLoadedDiscover = remember { mutableStateOf(false) }
        val allPlaces = remember { mutableStateListOf<Place>() }
        val refreshDiscover: suspend () -> List<Place> = {
            if (currentUserId == null) {
                emptyList()
            } else {
                val swipedIds = spotSwipeRepository.getSwipedSpotIds(currentUserId)
                spotRepository.getSpotsExcludingIds(swipedIds)
            }
        }

        LaunchedEffect(currentUserId) {
            if (currentUserId == null) {
                savedPlaces.clear()
                discoverPlaces.clear()
                isDiscoverLoading.value = false
                hasLoadedDiscover.value = true
            } else {
                isDiscoverLoading.value = true
                allPlaces.clear()
                allPlaces.addAll(spotRepository.getSpots())
                val likedIds = spotSwipeRepository.getLikedSpotIds(currentUserId)
                savedPlaces.clear()
                savedPlaces.addAll(spotRepository.getSpotsByIds(likedIds))
                discoverPlaces.clear()
                discoverPlaces.addAll(refreshDiscover())
                isDiscoverLoading.value = false
                hasLoadedDiscover.value = true
            }
        }

        CompositionLocalProvider(
            LocalCurrentUserId provides currentUserId,
            LocalSpotRepository provides spotRepository,
            LocalSpotSwipeRepository provides spotSwipeRepository,
            LocalSavedPlaces provides savedPlaces,
            LocalDiscoverPlaces provides discoverPlaces,
            LocalIsDiscoverLoading provides isDiscoverLoading,
            LocalHasLoadedDiscover provides hasLoadedDiscover,
            LocalRefreshDiscover provides refreshDiscover,
            LocalOnDismissPlace provides { place ->
                val userId = currentUserId ?: return@provides
                val spotId = place.id.toLongOrNull() ?: return@provides
                scope.launch {
                    spotSwipeRepository.upsertSwipe(
                        spotId = spotId,
                        userId = userId,
                        action = SpotSwipeAction.Disliked,
                    )
                }
            },
            LocalOnSavePlace provides { place ->
                if (savedPlaces.none { it.id == place.id }) {
                    savedPlaces.add(place)
                }
                val userId = currentUserId ?: return@provides
                val spotId = place.id.toLongOrNull() ?: return@provides
                scope.launch {
                    spotSwipeRepository.upsertSwipe(
                        spotId = spotId,
                        userId = userId,
                        action = SpotSwipeAction.Liked,
                    )
                }
            },
            LocalOnDeleteSavedPlace provides { place ->
                savedPlaces.removeAll { it.id == place.id }
                val userId = currentUserId ?: return@provides
                val spotId = place.id.toLongOrNull() ?: return@provides
                scope.launch {
                    spotSwipeRepository.upsertSwipe(
                        spotId = spotId,
                        userId = userId,
                        action = SpotSwipeAction.Disliked,
                    )
                }
            },
        ) {
            TabNavigator(DiscoverTab) {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        UnitBottomBar()
                    },
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .padding(innerPadding),
                    ) {
                        CurrentTab()
                        AddSpotButton(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 16.dp, top = 12.dp),
                            onClick = { showVisitFeedback = true },
                        )
                        if (showVisitFeedback) {
                            VisitFeedbackOverlay(
                                places = allPlaces,
                                currentUserId = currentUserId,
                                spotRepository = spotRepository,
                                spotRatingRepository = spotRatingRepository,
                                spotSwipeRepository = spotSwipeRepository,
                                onDismiss = { showVisitFeedback = false },
                                onSubmitted = {
                                    if (currentUserId != null) {
                                        isDiscoverLoading.value = true
                                        allPlaces.clear()
                                        allPlaces.addAll(spotRepository.getSpots())
                                        val likedIds = spotSwipeRepository.getLikedSpotIds(currentUserId)
                                        savedPlaces.clear()
                                        savedPlaces.addAll(spotRepository.getSpotsByIds(likedIds))
                                        discoverPlaces.clear()
                                        discoverPlaces.addAll(refreshDiscover())
                                        isDiscoverLoading.value = false
                                    }
                                    showVisitFeedback = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

private object DiscoverTab : Tab {
    override val options: TabOptions
        @Composable
        get() = remember {
            TabOptions(
                index = 0u,
                title = "Discover",
            )
        }

    @Composable
    override fun Content() {
        val savePlace = LocalOnSavePlace.current
        val dismissPlace = LocalOnDismissPlace.current
        val discoverPlaces = LocalDiscoverPlaces.current
        val isDiscoverLoading = LocalIsDiscoverLoading.current
        val hasLoadedDiscover = LocalHasLoadedDiscover.current
        val refreshDiscover = LocalRefreshDiscover.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart),
            ) {
                SectionHeader(
                    eyebrow = "Swipe picks",
                    title = "Find your next study sanctuary",
                    subtitle = "Swipe doprava pro uložení, doleva pro skip. Tady už běží reálný tinder feed.",
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 104.dp),
            ) {
                SwiperScreen(
                    places = discoverPlaces,
                    isLoading = isDiscoverLoading.value,
                    hasLoadedInitialData = hasLoadedDiscover.value,
                    onLoadingChange = { isDiscoverLoading.value = it },
                    onInitialLoadComplete = { hasLoadedDiscover.value = true },
                    onDismissPlace = dismissPlace,
                    onPlaceSaved = savePlace,
                    onRefreshPlaces = refreshDiscover,
                )
            }
        }
    }
}

private object MapTab : Tab {
    override val options: TabOptions
        @Composable
        get() = remember {
            TabOptions(
                index = 1u,
                title = "Map",
            )
        }

    @Composable
    override fun Content() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            SectionHeader(
                eyebrow = "Live map",
                title = "Spot energy across the city",
                subtitle = "Dočasný map shell, než napojíme reálnou mapu.",
            )
            Spacer(Modifier.height(18.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.88f),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF163832), Color(0xFF365B73), Color(0xFFF0BC7B)),
                            ),
                        )
                        .padding(18.dp),
                ) {
                    MapLabel(
                        label = "Atrium Lab",
                        modifier = Modifier.align(Alignment.TopStart).padding(top = 36.dp, start = 28.dp),
                    )
                    MapLabel(
                        label = "24/7 Quiet",
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 20.dp),
                    )
                    MapLabel(
                        label = "Loft",
                        modifier = Modifier.align(Alignment.BottomStart).padding(start = 62.dp, bottom = 44.dp),
                    )
                }
            }
        }
    }
}

private object SavedTab : Tab {
    override val options: TabOptions
        @Composable
        get() = remember {
            TabOptions(
                index = 2u,
                title = "Saved",
            )
        }

    @Composable
    override fun Content() {
        val savedPlaces = LocalSavedPlaces.current
        val deletePlace = LocalOnDeleteSavedPlace.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart),
            ) {
                SectionHeader(
                    eyebrow = "Collections",
                    title = "Saved for finals week",
                    subtitle = "Sem se ukládají spoty, které si uživatel swipe-ne doprava.",
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 104.dp),
            ) {
                GalleryScreen(
                    savedPlaces = savedPlaces,
                    onDeletePlace = deletePlace,
                )
            }
        }
    }
}

private object ProfileTab : Tab {
    override val options: TabOptions
        @Composable
        get() = remember {
            TabOptions(
                index = 3u,
                title = "Profile",
            )
        }

    @Composable
    override fun Content() {
        val isDarkMode = LocalIsDarkMode.current
        val setDarkMode = LocalSetDarkMode.current
        val refreshAuth = LocalRefreshAuth.current

        UserProfileScreen(
            account = UserAccountUi(
                displayName = "Guest User",
                email = "Not signed in",
            ),
            isDarkMode = isDarkMode,
            onThemeToggle = setDarkMode,
            onLogoutClick = refreshAuth,
        )
    }
}

@Composable
private fun UnitBottomBar() {
    val tabNavigator = LocalTabNavigator.current
    val tabs = listOf(DiscoverTab, MapTab, SavedTab, ProfileTab)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.72f))
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(38.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            tonalElevation = 12.dp,
            shadowElevation = 22.dp,
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                tabs.forEach { tab ->
                    val selected = tabNavigator.current == tab
                    val containerColor by animateColorAsState(
                        targetValue = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
                        } else {
                            Color.Transparent
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    )
                    val labelColor by animateColorAsState(
                        targetValue = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    )

                    Column(
                        modifier = Modifier
                            .width(78.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                tabNavigator.current = tab
                            }
                            .background(containerColor)
                            .padding(horizontal = 8.dp, vertical = 11.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        UnitTabIcon(
                            title = tab.options.title,
                            tint = labelColor,
                            selected = selected,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = tab.options.title,
                            color = labelColor,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Spacer(Modifier.height(4.dp))
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .size(width = 18.dp, height = 3.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary,
                                            ),
                                        ),
                                    ),
                            )
                        } else {
                            Spacer(Modifier.height(3.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddSpotButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .size(42.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 10.dp,
        shadowElevation = 18.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "+",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineSmall,
            )
        }
    }
}

@Composable
private fun VisitFeedbackOverlay(
    places: List<Place>,
    currentUserId: String?,
    spotRepository: SpotRepository,
    spotRatingRepository: SpotRatingRepository,
    spotSwipeRepository: SpotSwipeRepository,
    onDismiss: () -> Unit,
    onSubmitted: suspend () -> Unit,
) {
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.28f)),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 18.dp),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 8.dp,
            shadowElevation = 24.dp,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                VisitFeedbackPrompt(
                    places = places,
                    modifier = Modifier.fillMaxSize(),
                    onDismiss = onDismiss,
                    onConfirmVisit = { draft: VisitFeedbackDraft ->
                        val userId = currentUserId ?: return@VisitFeedbackPrompt
                        val spotId = draft.selectedPlaceId?.toLongOrNull() ?: return@VisitFeedbackPrompt
                        scope.launch {
                            spotRatingRepository.upsertRating(
                                spotId = spotId,
                                userId = userId,
                                noise = draft.noise,
                                comfort = draft.comfort,
                                snacks = draft.snacks,
                                powerOutlet = draft.hasPowerOutlet,
                            )
                            onSubmitted()
                        }
                    },
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(34.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismiss,
                        ),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "x",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UnitTabIcon(
    title: String,
    tint: Color,
    selected: Boolean,
) {
    Box(
        modifier = Modifier.size(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (title) {
            "Discover" -> DiscoverIcon(tint = tint, selected = selected)
            "Map" -> MapIcon(tint = tint, selected = selected)
            "Saved" -> SavedIcon(tint = tint, selected = selected)
            else -> ProfileIcon(tint = tint, selected = selected)
        }
    }
}

@Composable
private fun DiscoverIcon(
    tint: Color,
    selected: Boolean,
) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .drawBehind {
                val stroke = 1.8.dp.toPx()
                val inset = 3.dp.toPx()
                drawCircle(
                    color = tint,
                    radius = size.minDimension / 2.7f,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
                )
                drawLine(
                    color = tint.copy(alpha = if (selected) 1f else 0.75f),
                    start = androidx.compose.ui.geometry.Offset(size.width / 2f, inset),
                    end = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height - inset),
                    strokeWidth = stroke * 0.85f,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = tint.copy(alpha = if (selected) 1f else 0.75f),
                    start = androidx.compose.ui.geometry.Offset(inset, size.height / 2f),
                    end = androidx.compose.ui.geometry.Offset(size.width - inset, size.height / 2f),
                    strokeWidth = stroke * 0.85f,
                    cap = StrokeCap.Round,
                )
            },
    )
}

@Composable
private fun MapIcon(
    tint: Color,
    selected: Boolean,
) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .drawBehind {
                val stroke = 1.8.dp.toPx()
                val w = size.width
                val h = size.height
                val foldTop = h * 0.18f
                val foldBottom = h * 0.82f

                drawLine(
                    color = tint,
                    start = androidx.compose.ui.geometry.Offset(w * 0.24f, foldTop),
                    end = androidx.compose.ui.geometry.Offset(w * 0.24f, foldBottom),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = tint,
                    start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.12f),
                    end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.88f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = tint,
                    start = androidx.compose.ui.geometry.Offset(w * 0.76f, foldTop),
                    end = androidx.compose.ui.geometry.Offset(w * 0.76f, foldBottom),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = tint.copy(alpha = if (selected) 1f else 0.78f),
                    start = androidx.compose.ui.geometry.Offset(w * 0.24f, foldTop),
                    end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.12f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = tint.copy(alpha = if (selected) 1f else 0.78f),
                    start = androidx.compose.ui.geometry.Offset(w * 0.24f, foldBottom),
                    end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.88f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = tint.copy(alpha = if (selected) 1f else 0.78f),
                    start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.12f),
                    end = androidx.compose.ui.geometry.Offset(w * 0.76f, foldTop),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = tint.copy(alpha = if (selected) 1f else 0.78f),
                    start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.88f),
                    end = androidx.compose.ui.geometry.Offset(w * 0.76f, foldBottom),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
            },
    )
}

@Composable
private fun SavedIcon(
    tint: Color,
    selected: Boolean,
) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .drawBehind {
                val stroke = 1.8.dp.toPx()
                val pad = 3.dp.toPx()

                drawRoundRect(
                    color = tint,
                    topLeft = androidx.compose.ui.geometry.Offset(pad, pad),
                    size = androidx.compose.ui.geometry.Size(size.width - pad * 2, size.height - pad * 2),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
                )

                val bottomY = size.height - pad
                drawLine(
                    color = tint,
                    start = androidx.compose.ui.geometry.Offset(size.width * 0.32f, bottomY),
                    end = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.64f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = tint,
                    start = androidx.compose.ui.geometry.Offset(size.width * 0.68f, bottomY),
                    end = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.64f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                if (selected) {
                    drawCircle(
                        color = tint.copy(alpha = 0.12f),
                        radius = 5.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.34f),
                    )
                }
            },
    )
}

@Composable
private fun ProfileIcon(
    tint: Color,
    selected: Boolean,
) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .drawBehind {
                val stroke = 1.8.dp.toPx()
                val headCenterY = size.height * 0.31f
                val bodyTop = size.height * 0.52f

                drawCircle(
                    color = tint,
                    radius = 3.4.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(size.width / 2f, headCenterY),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
                )
                drawArc(
                    color = tint,
                    startAngle = 198f,
                    sweepAngle = 144f,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.18f, bodyTop),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.64f, size.height * 0.28f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = StrokeCap.Round),
                )
                if (selected) {
                    drawCircle(
                        color = tint.copy(alpha = 0.12f),
                        radius = 4.2.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(size.width / 2f, headCenterY),
                    )
                }
            },
    )
}

@Composable
private fun SectionHeader(
    eyebrow: String,
    title: String,
    subtitle: String,
) {
    Text(
        text = eyebrow.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.secondary,
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = subtitle,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun MapLabel(
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.92f),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = Color(0xFF132320),
        )
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
