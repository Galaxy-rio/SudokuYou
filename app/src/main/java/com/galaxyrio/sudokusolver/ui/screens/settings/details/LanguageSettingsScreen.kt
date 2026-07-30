package com.galaxyrio.sudokusolver.ui.screens.settings.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.galaxyrio.sudokusolver.R

@Composable
fun LanguageSettingsScreen(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsDetailScaffold(
        titleResource = R.string.language_title,
        onBack = onBack,
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        ) {
            item(key = "description") {
                Text(
                    text = stringResource(R.string.language_description),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        start = 4.dp,
                        top = 24.dp,
                        end = 4.dp,
                        bottom = 16.dp,
                    ),
                )
            }

            itemsIndexed(
                items = AppLanguage.entries,
                key = { _, language -> language.name },
            ) { index, language ->
                SegmentedListItem(
                    selected = language == selectedLanguage,
                    onClick = { onLanguageSelected(language) },
                    shapes = ListItemDefaults.segmentedShapes(
                        index = index,
                        count = AppLanguage.entries.size,
                    ),
                    colors = ListItemDefaults.segmentedColors(
                        containerColor = MaterialTheme.colorScheme.surfaceBright,
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    content = {
                        Text(stringResource(language.labelResource))
                    },
                    supportingContent = {
                        Text(stringResource(language.summaryResource))
                    },
                    trailingContent = {
                        RadioButton(
                            selected = language == selectedLanguage,
                            onClick = { onLanguageSelected(language) },
                        )
                    },
                )
            }
        }
    }
}
