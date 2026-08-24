package com.galaxyrio.sudokusolver.ui.screens.settings

import androidx.annotation.StringRes
import androidx.annotation.Keep
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SportsEsports
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.ui.screens.settings.details.AppLanguage
import kotlinx.serialization.Serializable

@Keep
@Serializable
enum class SettingsCategory {
    APPEARANCE,
    GAME,
    ASSISTANCE,
    FILES,
    LANGUAGE,
    ABOUT,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateTo: (SettingsCategory) -> Unit,
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
                        text = stringResource(R.string.settings_title),
                        modifier = Modifier.padding(start = 4.dp),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 24.dp),
        ) {
            itemsIndexed(
                items = SettingsCategory.entries,
                key = { _, category -> category.name },
            ) { index, category ->
                val title = stringResource(category.titleResource)
                SegmentedListItem(
                    onClick = { onNavigateTo(category) },
                    shapes = ListItemDefaults.segmentedShapes(
                        index = index,
                        count = SettingsCategory.entries.size,
                    ),
                    colors = ListItemDefaults.segmentedColors(
                        containerColor = MaterialTheme.colorScheme.surfaceBright,
                    ),
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    },
                    content = { Text(title) },
                    supportingContent = {
                        Text(
                            text = if (category == SettingsCategory.LANGUAGE) {
                                AppLanguage.current().label()
                            } else {
                                stringResource(category.summaryResource)
                            },
                        )
                    },
                )
            }
        }
    }
}

private val SettingsCategory.icon: ImageVector
    get() = when (this) {
        SettingsCategory.APPEARANCE -> Icons.Default.Palette
        SettingsCategory.GAME -> Icons.Default.SportsEsports
        SettingsCategory.ASSISTANCE -> Icons.AutoMirrored.Filled.Help
        SettingsCategory.FILES -> Icons.Default.Folder
        SettingsCategory.LANGUAGE -> Icons.Default.Language
        SettingsCategory.ABOUT -> Icons.Default.Info
    }

@get:StringRes
private val SettingsCategory.titleResource: Int
    get() = when (this) {
        SettingsCategory.APPEARANCE -> R.string.settings_appearance
        SettingsCategory.GAME -> R.string.settings_game
        SettingsCategory.ASSISTANCE -> R.string.settings_assistance
        SettingsCategory.FILES -> R.string.settings_files
        SettingsCategory.LANGUAGE -> R.string.settings_language
        SettingsCategory.ABOUT -> R.string.settings_about
    }

@get:StringRes
private val SettingsCategory.summaryResource: Int
    get() = when (this) {
        SettingsCategory.APPEARANCE -> R.string.settings_appearance_summary
        SettingsCategory.GAME -> R.string.settings_game_summary
        SettingsCategory.ASSISTANCE -> R.string.settings_assistance_summary
        SettingsCategory.FILES -> R.string.settings_files_summary
        SettingsCategory.LANGUAGE -> R.string.settings_language
        SettingsCategory.ABOUT -> R.string.settings_about_summary
    }
