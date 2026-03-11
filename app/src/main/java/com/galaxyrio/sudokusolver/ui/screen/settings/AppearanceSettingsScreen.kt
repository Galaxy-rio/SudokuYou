package com.galaxyrio.sudokusolver.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.galaxyrio.sudokusolver.data.ThemeMode
import com.galaxyrio.sudokusolver.ui.viewmodel.SettingsViewModel
import com.materialkolor.PaletteStyle

enum class SettingThemeCategory(val title: String, val subtitle:String) {
    ACCENT_COLOR("Accent Color","Dynamic or Custom"),
    PALETTE_STYLE("Palette Style", "Material You color scheme style"),
    THEME_MODE("Theme Mode", "System, Light or Dark"),
    AMOLED_MODE("AMOLED Mode", "True black dark theme")
}

enum class SettingBoardCategory(val title: String, val subtitle:String) {
    COLORED_BOARD("Colored Board", "Apply theme colors to the board "),
    POSITION_LINES("Position Lines", "Highlight rows, columns of the selected cell"),
    POSITION_BLOCK("Position Block", "Highlight the block of the selected cell"),
    ALTERNATIVE_ERROR_COLOR("Alternative Error Color", "Helpful when using red theme"),
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppearanceSettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val themeMode by viewModel.themeMode.collectAsState()
    val themeColor by viewModel.themeColor.collectAsState()
    val useDynamicColors by viewModel.useDynamicColors.collectAsState()
    val paletteStyle by viewModel.paletteStyle.collectAsState()
    val isAmoled by viewModel.isAmoled.collectAsState()

    val coloredBoard by viewModel.coloredBoard.collectAsState()
    val positionLines by viewModel.positionLines.collectAsState()
    val positionBlock by viewModel.positionBlock.collectAsState()
    val alternativeErrorColor by viewModel.alternativeErrorColor.collectAsState()

    var showPaletteStyleDialog by remember { mutableStateOf(false) }
    var showThemeModeDialog by remember { mutableStateOf(false) }



    if (showPaletteStyleDialog) {
        AlertDialog(
            onDismissRequest = { showPaletteStyleDialog = false },
            title = { Text("Palette Style") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    PaletteStyle.entries.forEach { style ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setPaletteStyle(style)
                                    showPaletteStyleDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (style == paletteStyle),
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = style.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPaletteStyleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showThemeModeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeModeDialog = false },
            title = { Text("Theme Mode") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    val modes = listOf(
                        ThemeMode.SYSTEM to "System Default",
                        ThemeMode.LIGHT to "Light",
                        ThemeMode.DARK to "Dark"
                    )
                    modes.forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(mode)
                                    showThemeModeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (mode == themeMode),
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeModeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Appearance", modifier = Modifier.padding(start = 4.dp)) },
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {


            item {
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, top = 0.dp, bottom = 8.dp)
                )
            }


            item {
                SegmentedListItem(
                    onClick = {  },
                    shapes = ListItemDefaults.segmentedShapes(index = 0, count = SettingThemeCategory.entries.size),
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceBright),

                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Column(modifier = Modifier) {
                        Text(SettingThemeCategory.ACCENT_COLOR.title,)
                        Spacer(modifier = Modifier.height(12.dp))
                        ColorPicker(
                            isDynamic = useDynamicColors,
                            selectedColor = themeColor,
                            onDynamicSelected = { viewModel.setUseDynamicColors(true) },
                            onColorSelected = {
                                viewModel.setUseDynamicColors(false)
                                viewModel.setThemeColor(it)
                            }
                        )
                    }
                }

            }


            item {
                SegmentedListItem(
                    onClick = { showPaletteStyleDialog = true },
                    shapes = ListItemDefaults.segmentedShapes(index = 1, count = SettingThemeCategory.entries.size),
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                    modifier = Modifier.fillMaxSize(),

                    supportingContent = { Text(paletteStyle.name) },

                    ) {
                    Text("Palette Style")
                }

            }


            item {
                SegmentedListItem(
                    onClick = { showThemeModeDialog = true },

                    shapes = ListItemDefaults.segmentedShapes(index = 2, count = SettingThemeCategory.entries.size),
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                    modifier = Modifier.fillMaxSize(),

                    supportingContent = {
                        val text = when (themeMode) {
                            ThemeMode.SYSTEM -> "System Default"
                            ThemeMode.LIGHT -> "Light"
                            ThemeMode.DARK -> "Dark"
                        }
                        Text(text)
                    }
                ) {
                    Text("Theme Mode")
                }
            }

            item {
                SegmentedListItem(
                    onClick = { viewModel.setIsAmoled(!isAmoled) },
                    shapes = ListItemDefaults.segmentedShapes(index = 3, count = SettingThemeCategory.entries.size),
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                    modifier = Modifier.fillMaxSize(),
                    trailingContent = {
                        Switch(
                            checked = isAmoled,
                            onCheckedChange = { viewModel.setIsAmoled(it) }
                        )
                    },
                    supportingContent = { Text(SettingThemeCategory.AMOLED_MODE.subtitle) }
                ) {
                    Text(SettingThemeCategory.AMOLED_MODE.title)
                }
            }

            item {
                Text(
                    text = "Board",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            item {
                SegmentedListItem(
                    onClick = { viewModel.setColoredBoard(!coloredBoard) },
                    shapes = ListItemDefaults.segmentedShapes(index = 0, count = SettingBoardCategory.entries.size),
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                    modifier = Modifier.fillMaxSize(),
                    trailingContent = {
                        Switch(
                            checked = coloredBoard,
                            onCheckedChange = { viewModel.setColoredBoard(it) }
                        )
                    },
                    supportingContent = { Text(SettingBoardCategory.COLORED_BOARD.subtitle) }
                ) {
                    Text(SettingBoardCategory.COLORED_BOARD.title)
                }
            }

            item {
                SegmentedListItem(
                    onClick = { viewModel.setPositionLines(!positionLines) },
                    shapes = ListItemDefaults.segmentedShapes(index = 1, count = SettingBoardCategory.entries.size),
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                    modifier = Modifier.fillMaxSize(),
                    trailingContent = {
                        Switch(
                            checked = positionLines,
                            onCheckedChange = { viewModel.setPositionLines(it) }
                        )
                    },
                    supportingContent = { Text(SettingBoardCategory.POSITION_LINES.subtitle) }
                ) {
                    Text(SettingBoardCategory.POSITION_LINES.title)
                }
            }

            item {
                SegmentedListItem(
                    onClick = { viewModel.setPositionBlock(!positionBlock) },
                    shapes = ListItemDefaults.segmentedShapes(index = 2, count = SettingBoardCategory.entries.size),
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                    modifier = Modifier.fillMaxSize(),
                    trailingContent = {
                        Switch(
                            checked = positionBlock,
                            onCheckedChange = { viewModel.setPositionBlock(it) }
                        )
                    },
                    supportingContent = { Text(SettingBoardCategory.POSITION_BLOCK.subtitle) }
                ) {
                    Text(SettingBoardCategory.POSITION_BLOCK.title)
                }
            }

            item {
                SegmentedListItem(
                    onClick = { viewModel.setAlternativeErrorColor(!alternativeErrorColor) },
                    shapes = ListItemDefaults.segmentedShapes(index = 3, count = SettingBoardCategory.entries.size),
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                    modifier = Modifier.fillMaxSize(),
                    trailingContent = {
                        Switch(
                            checked = alternativeErrorColor,
                            onCheckedChange = { viewModel.setAlternativeErrorColor(it) }
                        )
                    },
                    supportingContent = { Text(SettingBoardCategory.ALTERNATIVE_ERROR_COLOR.subtitle) }
                ) {
                    Text(SettingBoardCategory.ALTERNATIVE_ERROR_COLOR.title)
                }
            }

        }
    }
}



@Composable
fun ColorPicker(
    isDynamic: Boolean,
    selectedColor: Color,
    onDynamicSelected: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    val colors = listOf(
        Color(0xFF6750A4), // Purple
        Color(0xFFB3261E), // Red
        Color(0xFFE27C33), // Orange
        Color(0xFF7D5260), // Pink
        Color(0xFF3F51B5), // Indigo
        Color(0xFF009688), // Teal
        Color(0xFF4CAF50), // Green
        Color(0xFFFFEB3B), // Yellow
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable { onDynamicSelected() }
                    .then(
                        if (isDynamic) {
                            Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BrightnessAuto,
                    contentDescription = "Dynamic",
                    tint = if (isDynamic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(colors) { color ->
            val isSelected = !isDynamic && selectedColor == color
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onColorSelected(color) }
                    .then(
                        if (isSelected) {
                            Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
