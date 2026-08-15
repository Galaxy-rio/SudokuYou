package com.galaxyrio.sudokusolver.domain.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
enum class Difficulty {
    EASY,
    MEDIUM,
    HARD,
    BRUTAL,
}
