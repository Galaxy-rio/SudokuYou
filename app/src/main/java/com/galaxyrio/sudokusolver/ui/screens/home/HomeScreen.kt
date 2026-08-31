package com.galaxyrio.sudokusolver.ui.screens.home

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.galaxyrio.sudokusolver.ui.screens.statistics.StatisticsRoute
import com.galaxyrio.sudokusolver.ui.screens.statistics.StatisticsViewModel
import com.galaxyrio.sudokusolver.ui.motion.materialTopLevelTransition
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    playViewModel: PlayViewModel,
    statisticsViewModel: StatisticsViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onStartGame: (Difficulty) -> Unit,
    onContinueGame: (Long) -> Unit,
    onOpenImportedGame: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentSection by rememberSaveable {
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
                        onOpenSettings = onOpenSettings,
                        onStartGame = onStartGame,
                        onContinueGame = onContinueGame,
                        onOpenImportedGame = onOpenImportedGame,
                        modifier = Modifier.fillMaxSize(),
                    )

                    HomeSection.TUTORIAL -> HomeSectionPlaceholderScreen(
                        titleResource = section.labelResource,
                        onOpenNavigation = openNavigation,
                        onOpenSettings = onOpenSettings,
                        modifier = Modifier.fillMaxSize(),
                    )

                    HomeSection.STATISTICS -> StatisticsRoute(
                        viewModel = statisticsViewModel,
                        onOpenNavigation = openNavigation,
                        onOpenSettings = onOpenSettings,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        ModalWideNavigationRail(
            modifier = Modifier.width(HomeNavigationRailWidth),
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
                    modifier = Modifier.fillMaxWidth(),
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
                    label = {
                        Text(
                            text = stringResource(destination.labelResource),
                            modifier = Modifier.width(HomeNavigationRailLabelWidth),
                        )
                    },
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

// Material 3's expanded rail is 220.dp wide. Reserving a fixed-width label slot makes the
// built-in active indicator fill the 180.dp destination container while keeping its standard
// 20.dp outer margins. The item itself remains the full-width selectable target.
private val HomeNavigationRailWidth = 220.dp
private val HomeNavigationRailLabelWidth = 116.dp

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
