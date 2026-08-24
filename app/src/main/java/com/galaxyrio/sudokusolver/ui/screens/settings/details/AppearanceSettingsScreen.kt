package com.galaxyrio.sudokusolver.ui.screens.settings.details

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.data.settings.ThemeMode
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsUiState
import com.galaxyrio.sudokusolver.ui.util.label
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import com.materialkolor.dynamiccolor.ColorSpec

@Composable
fun AppearanceSettingsScreen(
    uiState: SettingsUiState,
    onThemeModeChange: (ThemeMode) -> Unit,
    onThemeColorChange: (Color) -> Unit,
    onPaletteStyleChange: (PaletteStyle) -> Unit,
    onDynamicColorsChange: (Boolean) -> Unit,
    onAmoledChange: (Boolean) -> Unit,
    onColoredBoardChange: (Boolean) -> Unit,
    onPositionLinesChange: (Boolean) -> Unit,
    onPositionBlockChange: (Boolean) -> Unit,
    onAlternativeErrorColorChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var dialog by remember { mutableStateOf<AppearanceDialog?>(null) }

    when (dialog) {
        AppearanceDialog.PALETTE_STYLE -> SelectionDialog(
            titleResource = R.string.appearance_palette_style,
            items = supportedPaletteStyles,
            selectedItem = uiState.paletteStyle,
            itemLabel = { it.label() },
            onSelect = {
                onPaletteStyleChange(it)
                dialog = null
            },
            onDismiss = { dialog = null },
        )

        AppearanceDialog.THEME_MODE -> SelectionDialog(
            titleResource = R.string.appearance_theme_mode,
            items = ThemeMode.entries,
            selectedItem = uiState.themeMode,
            itemLabel = { it.label() },
            onSelect = {
                onThemeModeChange(it)
                dialog = null
            },
            onDismiss = { dialog = null },
        )

        null -> Unit
    }

    SettingsDetailScaffold(
        titleResource = R.string.appearance_title,
        onBack = onBack,
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            item(key = "theme_heading") {
                SettingsSectionHeading(R.string.appearance_theme_section)
            }

            item(key = "accent_color") {
                SeedColorPicker(
                    isDynamic = uiState.useDynamicColors,
                    selectedColor = uiState.themeColor,
                    paletteStyle = uiState.paletteStyle,
                    onColorSelected = onThemeColorChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                )
            }

            item(key = "dynamic_colors") {
                SettingsSwitchItem(
                    titleResource = R.string.appearance_dynamic_color,
                    summaryResource = R.string.appearance_dynamic_color_summary,
                    checked = uiState.useDynamicColors,
                    index = 0,
                    count = 4,
                    onCheckedChange = onDynamicColorsChange,
                )
            }

            item(key = "palette_style") {
                SettingsActionItem(
                    titleResource = R.string.appearance_palette_style,
                    summary = uiState.paletteStyle.label(),
                    index = 1,
                    count = 4,
                    onClick = { dialog = AppearanceDialog.PALETTE_STYLE },
                )
            }

            item(key = "theme_mode") {
                SettingsActionItem(
                    titleResource = R.string.appearance_theme_mode,
                    summary = uiState.themeMode.label(),
                    index = 2,
                    count = 4,
                    onClick = { dialog = AppearanceDialog.THEME_MODE },
                )
            }

            item(key = "amoled") {
                SettingsSwitchItem(
                    titleResource = R.string.appearance_amoled_mode,
                    summaryResource = R.string.appearance_amoled_mode_summary,
                    checked = uiState.isAmoled,
                    index = 3,
                    count = 4,
                    onCheckedChange = onAmoledChange,
                )
            }

            item(key = "board_heading") {
                SettingsSectionHeading(R.string.appearance_board_section)
            }

            item(key = "colored_board") {
                SettingsSwitchItem(
                    titleResource = R.string.appearance_colored_board,
                    summaryResource = R.string.appearance_colored_board_summary,
                    checked = uiState.coloredBoard,
                    index = 0,
                    count = 4,
                    onCheckedChange = onColoredBoardChange,
                )
            }

            item(key = "position_lines") {
                SettingsSwitchItem(
                    titleResource = R.string.appearance_position_lines,
                    summaryResource = R.string.appearance_position_lines_summary,
                    checked = uiState.positionLines,
                    index = 1,
                    count = 4,
                    onCheckedChange = onPositionLinesChange,
                )
            }

            item(key = "position_block") {
                SettingsSwitchItem(
                    titleResource = R.string.appearance_position_block,
                    summaryResource = R.string.appearance_position_block_summary,
                    checked = uiState.positionBlock,
                    index = 2,
                    count = 4,
                    onCheckedChange = onPositionBlockChange,
                )
            }

            item(key = "alternative_error_color") {
                SettingsSwitchItem(
                    titleResource = R.string.appearance_alternative_error_color,
                    summaryResource = R.string.appearance_alternative_error_color_summary,
                    checked = uiState.alternativeErrorColor,
                    index = 3,
                    count = 4,
                    onCheckedChange = onAlternativeErrorColorChange,
                )
            }

            item(key = "bottom_spacing") {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SettingsSectionHeading(@StringRes textResource: Int) {
    Text(
        text = stringResource(textResource),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 24.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsActionItem(
    @StringRes titleResource: Int,
    summary: String,
    index: Int,
    count: Int,
    onClick: () -> Unit,
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        colors = settingsItemColors(),
        content = { Text(stringResource(titleResource)) },
        supportingContent = { Text(summary) },
    )
}

@Composable
private fun SettingsSwitchItem(
    @StringRes titleResource: Int,
    @StringRes summaryResource: Int,
    checked: Boolean,
    index: Int,
    count: Int,
    onCheckedChange: (Boolean) -> Unit,
) {
    SegmentedListItem(
        onClick = { onCheckedChange(!checked) },
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        colors = settingsItemColors(),
        content = { Text(stringResource(titleResource)) },
        supportingContent = { Text(stringResource(summaryResource)) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        },
    )
}

@Composable
private fun settingsItemColors() = ListItemDefaults.segmentedColors(
    containerColor = MaterialTheme.colorScheme.surfaceBright,
)

@Composable
private fun <T> SelectionDialog(
    @StringRes titleResource: Int,
    items: List<T>,
    selectedItem: T,
    itemLabel: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(titleResource)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(item) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = item == selectedItem,
                            onClick = { onSelect(item) },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = itemLabel(item),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
    )
}

@Composable
private fun SeedColorPicker(
    isDynamic: Boolean,
    selectedColor: Color,
    paletteStyle: PaletteStyle,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.appearance_accent_color),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        Text(
            text = stringResource(R.string.appearance_dynamic_or_custom),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            val itemsPerPage = ((maxWidth + PaletteGap) / (PaletteSize + PaletteGap))
                .toInt()
                .coerceIn(1, accentColors.size)
            val pages = remember(itemsPerPage) { accentColors.chunked(itemsPerPage) }
            val pagerState = rememberPagerState(pageCount = { pages.size })

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                ) { page ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            PaletteGap,
                            Alignment.CenterHorizontally,
                        ),
                    ) {
                        pages[page].forEach { color ->
                            PalettePreviewOption(
                                seedColor = color,
                                paletteStyle = paletteStyle,
                                selected = !isDynamic && selectedColor == color,
                                contentDescription = stringResource(
                                    R.string.appearance_color_option,
                                    color.toHexRgb(),
                                ),
                                onClick = { onColorSelected(color) },
                            )
                        }
                    }
                }

                if (pages.size > 1) {
                    Row(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(pages.size) { index ->
                            val indicatorWidth by animateDpAsState(
                                targetValue = if (pagerState.currentPage == index) 20.dp else 8.dp,
                                label = "palette_page_indicator",
                            )
                            Box(
                                modifier = Modifier
                                    .width(indicatorWidth)
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (pagerState.currentPage == index) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surfaceContainerHighest
                                        }
                                    ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PalettePreviewOption(
    seedColor: Color,
    paletteStyle: PaletteStyle,
    selected: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val previewScheme = remember(seedColor, paletteStyle) {
        dynamicColorScheme(
            seedColor = seedColor,
            isDark = false,
            style = paletteStyle,
            specVersion = ColorSpec.SpecVersion.SPEC_2025,
        )
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(PaletteSize)
            .semantics {
                this.contentDescription = contentDescription
                this.selected = selected
            },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
    ) {
        Box(
            modifier = Modifier.padding(10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(previewScheme.primary),
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(previewScheme.secondary),
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(previewScheme.tertiary),
                    )
                }
            }

            if (selected) {
                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.common_selected),
                        modifier = Modifier.padding(4.dp),
                    )
                }
            }
        }
    }
}

private fun Color.toHexRgb(): String = "#%06X".format(toArgb() and 0xFFFFFF)

private enum class AppearanceDialog {
    PALETTE_STYLE,
    THEME_MODE,
}

private val supportedPaletteStyles = listOf(
    PaletteStyle.TonalSpot,
    PaletteStyle.Neutral,
    PaletteStyle.Vibrant,
    PaletteStyle.Expressive,
    PaletteStyle.Rainbow,
    PaletteStyle.FruitSalad,
    PaletteStyle.Monochrome,
    PaletteStyle.Fidelity,
    PaletteStyle.Content,
)

private val accentColors = listOf(
    Color(0xFF6750A4),
    Color(0xFFB3261E),
    Color(0xFFE27C33),
    Color(0xFF7D5260),
    Color(0xFF3F51B5),
    Color(0xFF009688),
    Color(0xFF4CAF50),
    Color(0xFFF9A825),
)

private val PaletteSize = 70.dp
private val PaletteGap = 10.dp
