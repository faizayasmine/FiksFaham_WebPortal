package com.example.util

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.FiksScoreGreen
import com.example.ui.theme.FiksScoreRed
import com.example.ui.theme.FiksScoreYellow

/**
 * Helper terpusat untuk pewarnaan nilai sesuai aturan PRD:
 * - 80..100 -> Hijau (FiksScoreGreen)
 * - 75..79  -> Kuning (FiksScoreYellow)
 * - < 75    -> Merah (FiksScoreRed)
 */
object ScoreColorUtils {
    const val GREEN_THRESHOLD = 80
    const val PASS_THRESHOLD = 75

    fun getScoreColor(score: Int): Color {
        return when {
            score >= GREEN_THRESHOLD -> FiksScoreGreen
            score >= PASS_THRESHOLD -> FiksScoreYellow
            else -> FiksScoreRed
        }
    }

    fun getScoreBadgeLabel(score: Int): String {
        return when {
            score >= GREEN_THRESHOLD -> "Sangat Baik"
            score >= PASS_THRESHOLD -> "Cukup / Lulus"
            else -> "Perlu Penguatan"
        }
    }
}
