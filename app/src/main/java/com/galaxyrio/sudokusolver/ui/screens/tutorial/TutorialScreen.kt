package com.galaxyrio.sudokusolver.ui.screens.tutorial

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.PlaceholderSize.Companion.AnimatedSize
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.RemeasureToBounds
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import com.galaxyrio.sudokusolver.domain.tutorial.TutorialLessons
import com.galaxyrio.sudokusolver.ui.screens.play.SudokuThumbnail
import com.galaxyrio.sudokusolver.ui.util.label

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun TutorialScreen(
    onOpenNavigation: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTechnique: (String) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.nav_tutorial),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                navigationIcon = {
                    IconButton(onClick = onOpenNavigation) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(R.string.nav_open_menu),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.nav_open_settings),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = innerPadding.calculateTopPadding(),
                end = 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        ) {
            tutorialCategories.forEachIndexed { categoryIndex, category ->
                item(key = "heading_${category.id}", contentType = "heading") {
                    Text(
                        text = stringResource(category.titleResource),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(
                                start = 4.dp,
                                top = if (categoryIndex == 0) 16.dp else 28.dp,
                                bottom = 8.dp,
                            )
                            .semantics { heading() },
                    )
                }
                itemsIndexed(
                    items = category.techniques,
                    key = { _, technique -> technique.id },
                    contentType = { _, _ -> "technique" },
                ) { index, technique ->
                    TutorialTechniqueItem(
                        technique = technique,
                        index = index,
                        count = category.techniques.size,
                        onClick = { onOpenTechnique(technique.id) },
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun TutorialTechniqueItem(
    technique: TutorialTechnique,
    index: Int,
    count: Int,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val lesson = TutorialLessons.find(technique.id)
    val content: @Composable () -> Unit = {
        Text(stringResource(technique.titleResource), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
    val supportingContent: @Composable () -> Unit = {
        Text(technique.level?.label().orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
    val colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceBright)
    val shapes = ListItemDefaults.segmentedShapes(index = index, count = count)
    with(sharedTransitionScope) {
        if (lesson == null) {
            SegmentedListItem(
                verticalAlignment = Alignment.CenterVertically,
                shapes = shapes, colors = colors, content = content, supportingContent = supportingContent,
                leadingContent = { SudokuThumbnail(EmptyTutorialBoard, modifier = Modifier.size(64.dp)) },
            )
        } else {
            SegmentedListItem(
                selected = false,
                onClick = onClick,
                verticalAlignment = Alignment.CenterVertically,
                shapes = shapes, colors = colors, content = content, supportingContent = supportingContent,
                leadingContent = {
                    TutorialThumbnail(lesson.examples.first(), Modifier.size(64.dp).sharedBounds(
                        rememberSharedContentState(tutorialBoardKey(technique.id)), animatedVisibilityScope,
                        resizeMode = RemeasureToBounds,
                    ))
                },
                modifier = Modifier.sharedBounds(
                    rememberSharedContentState(tutorialContainerKey(technique.id)), animatedVisibilityScope,
                    resizeMode = RemeasureToBounds, placeholderSize = AnimatedSize,
                ),
            )
        }
    }
}

private val EmptyTutorialBoard = List(Sudoku.CELL_COUNT) { 0 }
