package com.galaxyrio.sudokusolver.ui.screens.home

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.ui.screens.play.PlayMenuRoute
import com.galaxyrio.sudokusolver.ui.screens.play.PlayViewModel
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsCategory
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsScreen
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
    var currentDestination by rememberSaveable {
        mutableStateOf(HomeSection.PLAY)
    }
    val navigationRailState = rememberWideNavigationRailState()
    val navigationScope = rememberCoroutineScope()
    val openNavigation: () -> Unit = {
        navigationScope.launch { navigationRailState.expand() }
    }
    val closeNavigation: () -> Unit = {
        navigationScope.launch { navigationRailState.collapse() }
    }

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = { materialTopLevelTransition() },
                label = "home_section_transition",
            ) { destination ->
                when (destination) {
                    HomeSection.PLAY -> PlayMenuRoute(
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        viewModel = playViewModel,
                        onOpenNavigation = openNavigation,
                        onStartGame = onStartGame,
                        onContinueGame = onContinueGame,
                        onOpenImportedGame = onOpenImportedGame,
                        modifier = Modifier.fillMaxSize(),
                    )

                    HomeSection.TUTORIAL,
                    HomeSection.STATISTICS,
                    -> HomeSectionPlaceholderScreen(
                        titleResource = destination.labelResource,
                        onOpenNavigation = openNavigation,
                        modifier = Modifier.fillMaxSize(),
                    )

                    HomeSection.SETTINGS -> SettingsScreen(
                        onOpenNavigation = openNavigation,
                        onNavigateTo = onNavigateToSettings,
                        modifier = Modifier.fillMaxSize(),
                    )
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
                val isSelected = destination == currentDestination
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
                        currentDestination = destination
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
    SETTINGS(
        R.string.nav_settings,
        Icons.Filled.Settings,
        Icons.Outlined.Settings,
    ),
}
