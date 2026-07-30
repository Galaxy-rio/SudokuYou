package com.galaxyrio.sudokusolver.ui.screens.settings.details

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import com.galaxyrio.sudokusolver.R

enum class AppLanguage(
    val languageTags: String,
    @param:StringRes val labelResource: Int,
    @param:StringRes val summaryResource: Int,
) {
    SYSTEM(
        languageTags = "",
        labelResource = R.string.language_system,
        summaryResource = R.string.language_system_summary,
    ),
    ENGLISH(
        languageTags = "en",
        labelResource = R.string.language_english,
        summaryResource = R.string.language_english_summary,
    ),
    CHINESE_SIMPLIFIED(
        languageTags = "zh-CN",
        labelResource = R.string.language_chinese_simplified,
        summaryResource = R.string.language_chinese_simplified_summary,
    );

    fun apply() {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(languageTags)
        )
    }

    @Composable
    fun label(): String = stringResource(labelResource)

    companion object {
        fun current(): AppLanguage {
            val tags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
            if (tags.isBlank()) return SYSTEM

            val primaryTag = tags.substringBefore(',').lowercase()
            return when {
                primaryTag.startsWith("zh") -> CHINESE_SIMPLIFIED
                primaryTag.startsWith("en") -> ENGLISH
                else -> SYSTEM
            }
        }
    }
}
