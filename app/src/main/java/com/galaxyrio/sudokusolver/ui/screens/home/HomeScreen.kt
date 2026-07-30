package com.galaxyrio.sudokusolver.ui.screens.home

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.ui.screens.info.InfoScreen
import com.galaxyrio.sudokusolver.ui.screens.play.PlayMenuRoute
import com.galaxyrio.sudokusolver.ui.screens.play.PlayViewModel
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsCategory
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    playViewModel: PlayViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onStartGame: (Difficulty) -> Unit,
    onContinueGame: (Long) -> Unit,
    onNavigateToSettings: (SettingsCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentDestination by rememberSaveable {
        mutableStateOf(HomeSection.PLAY)
    }
    val motionScheme = MaterialTheme.motionScheme

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            HomeSection.entries.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = null,
                        )
                    },
                    label = {
                        Text(stringResource(destination.labelResource))
                    },
                    selected = destination == currentDestination,
                    onClick = { currentDestination = destination },
                )
            }
        },
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        (
                            slideInHorizontally(
                                animationSpec = motionScheme.defaultSpatialSpec(),
                                initialOffsetX = { it },
                            ) + fadeIn(
                                animationSpec = motionScheme.defaultEffectsSpec(),
                            )
                        ).togetherWith(
                            slideOutHorizontally(
                                animationSpec = motionScheme.fastSpatialSpec(),
                                targetOffsetX = { -it },
                            ) + fadeOut(
                                animationSpec = motionScheme.fastEffectsSpec(),
                            )
                        )
                    } else {
                        (
                            slideInHorizontally(
                                animationSpec = motionScheme.defaultSpatialSpec(),
                                initialOffsetX = { -it },
                            ) + fadeIn(
                                animationSpec = motionScheme.defaultEffectsSpec(),
                            )
                        ).togetherWith(
                            slideOutHorizontally(
                                animationSpec = motionScheme.fastSpatialSpec(),
                                targetOffsetX = { it },
                            ) + fadeOut(
                                animationSpec = motionScheme.fastEffectsSpec(),
                            )
                        )
                    }
                },
                label = "home_section_transition",
            ) { destination ->
                when (destination) {
                    HomeSection.INFO -> InfoScreen(modifier = Modifier.fillMaxSize())
                    HomeSection.PLAY -> PlayMenuRoute(
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        viewModel = playViewModel,
                        initialDifficulty = Difficulty.MEDIUM,
                        onStartGame = onStartGame,
                        onContinueGame = onContinueGame,
                        modifier = Modifier.fillMaxSize(),
                    )

                    HomeSection.SETTINGS -> SettingsScreen(
                        onNavigateTo = onNavigateToSettings,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

private enum class HomeSection(
    @param:StringRes val labelResource: Int,
    val icon: ImageVector,
) {
    INFO(R.string.nav_info, Icons.Default.Info),
    PLAY(R.string.nav_play, Icons.Default.SportsEsports),
    SETTINGS(R.string.nav_settings, Icons.Default.Settings),
}
