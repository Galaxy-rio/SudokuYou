package com.galaxyrio.sudokusolver.ui.screens.home

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalWideNavigationRail
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.ui.screens.play.PlayMenuRoute
import com.galaxyrio.sudokusolver.ui.screens.play.PlayViewModel
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsCategory
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsScreen
import com.galaxyrio.sudokusolver.ui.motion.materialBackwardEnter
import com.galaxyrio.sudokusolver.ui.motion.materialBackwardExit
import com.galaxyrio.sudokusolver.ui.motion.materialForwardEnter
import com.galaxyrio.sudokusolver.ui.motion.materialForwardExit
import com.galaxyrio.sudokusolver.ui.motion.materialTopLevelTransition
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    playViewModel: PlayViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onStartGame: (Difficulty) -> Unit,
    onContinueGame: (Long) -> Unit,
    onOpenImportedGame: (Long) -> Unit,
    onNavigateToSettings: (SettingsCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentSection by rememberSaveable {
        mutableStateOf(HomeSection.PLAY)
    }
    var isSettingsVisible by rememberSaveable { mutableStateOf(false) }
    val navigationRailState = rememberWideNavigationRailState()
    val navigationScope = rememberCoroutineScope()
    val layoutDirection = LocalLayoutDirection.current
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val settingsHierarchyTravelPx = with(LocalDensity.current) {
        SettingsHierarchyTravel.roundToPx()
    }
    val openNavigation: () -> Unit = {
        navigationScope.launch { navigationRailState.expand() }
    }
    val closeNavigation: () -> Unit = {
        navigationScope.launch { navigationRailState.collapse() }
    }
    val edgeSwipeEnabled by rememberUpdatedState(
        !isSettingsVisible &&
            navigationRailState.targetValue == WideNavigationRailValue.Collapsed
    )
    val currentOpenNavigation by rememberUpdatedState(openNavigation)

    BackHandler(
        enabled = isSettingsVisible && lifecycleState == Lifecycle.State.RESUMED,
        onBack = { isSettingsVisible = false },
    )

    Box(
        modifier = modifier.pointerInput(layoutDirection) {
            val edgeWidthPx = NavigationRailEdgeSwipeWidth.toPx()
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                if (!edgeSwipeEnabled) return@awaitEachGesture

                val startedAtLeadingEdge = when (layoutDirection) {
                    LayoutDirection.Ltr -> down.position.x <= edgeWidthPx
                    LayoutDirection.Rtl -> down.position.x >= size.width - edgeWidthPx
                }
                if (!startedAtLeadingEdge) return@awaitEachGesture

                var openingDragAccepted = false
                val drag = awaitHorizontalTouchSlopOrCancellation(down.id) { change, overSlop ->
                    val movesTowardContent = when (layoutDirection) {
                        LayoutDirection.Ltr -> overSlop > 0f
                        LayoutDirection.Rtl -> overSlop < 0f
                    }
                    if (movesTowardContent) {
                        openingDragAccepted = true
                        change.consume()
                    }
                }
                if (drag != null && openingDragAccepted) {
                    currentOpenNavigation()
                    horizontalDrag(drag.id) { change -> change.consume() }
                }
            }
        },
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            AnimatedContent(
                targetState = isSettingsVisible,
                transitionSpec = {
                    if (targetState) {
                        materialForwardEnter(settingsHierarchyTravelPx).togetherWith(
                            materialForwardExit(settingsHierarchyTravelPx)
                        )
                    } else {
                        materialBackwardEnter(settingsHierarchyTravelPx).togetherWith(
                            materialBackwardExit(settingsHierarchyTravelPx)
                        )
                    }
                },
                modifier = Modifier.fillMaxSize(),
                label = "home_settings_transition",
            ) { showSettings ->
                if (showSettings) {
                    SettingsScreen(
                        onBack = { isSettingsVisible = false },
                        onNavigateTo = onNavigateToSettings,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    AnimatedContent(
                        targetState = currentSection,
                        transitionSpec = { materialTopLevelTransition() },
                        modifier = Modifier.fillMaxSize(),
                        label = "home_section_transition",
                    ) { section ->
                        when (section) {
                            HomeSection.PLAY -> PlayMenuRoute(
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                                viewModel = playViewModel,
                                onOpenNavigation = openNavigation,
                                onOpenSettings = { isSettingsVisible = true },
                                onStartGame = onStartGame,
                                onContinueGame = onContinueGame,
                                onOpenImportedGame = onOpenImportedGame,
                                modifier = Modifier.fillMaxSize(),
                            )

                            HomeSection.TUTORIAL,
                            HomeSection.STATISTICS,
                            -> HomeSectionPlaceholderScreen(
                                titleResource = section.labelResource,
                                onOpenNavigation = openNavigation,
                                onOpenSettings = { isSettingsVisible = true },
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }
        }

        ModalWideNavigationRail(
            state = navigationRailState,
            hideOnCollapse = true,
            contentPadding = PaddingValues(top = 8.dp),
            header = {
                IconButton(
                    onClick = closeNavigation,
                    modifier = Modifier.padding(start = 4.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuOpen,
                        contentDescription = stringResource(R.string.nav_close_menu),
                    )
                }
            },
        ) {
            HomeSection.entries.forEach { destination ->
                val isSelected = destination == currentSection
                WideNavigationRailItem(
                    railExpanded = navigationRailState.targetValue ==
                        WideNavigationRailValue.Expanded,
                    icon = {
                        Icon(
                            imageVector = if (isSelected) {
                                destination.selectedIcon
                            } else {
                                destination.unselectedIcon
                            },
                            contentDescription = null,
                        )
                    },
                    label = { Text(stringResource(destination.labelResource)) },
                    selected = isSelected,
                    onClick = {
                        currentSection = destination
                        closeNavigation()
                    },
                )
            }
        }
    }
}

private enum class HomeSection(
    @param:StringRes val labelResource: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    PLAY(
        R.string.nav_play,
        Icons.Filled.SportsEsports,
        Icons.Outlined.SportsEsports,
    ),
    TUTORIAL(
        R.string.nav_tutorial,
        Icons.Filled.School,
        Icons.Outlined.School,
    ),
    STATISTICS(
        R.string.nav_statistics,
        Icons.Filled.Analytics,
        Icons.Outlined.Analytics,
    ),
}

private val SettingsHierarchyTravel = 30.dp
private val NavigationRailEdgeSwipeWidth = 48.dp
