package com.galaxyrio.sudokusolver.domain.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
enum class Difficulty(val clueCount: Int) {
    EASY(clueCount = 40),
    MEDIUM(clueCount = 32),
    HARD(clueCount = 26),
}
