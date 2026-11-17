package com.example.util

import com.example.data.model.AssessmentCooldownEntity

/**
 * Konfigurasi durasi jeda ulang (cooldown) asesmen sesuai aturan PRD:
 * - Kuis Sub Materi: TIDAK ada pemblokiran/jeda (0 menit), pengguna boleh lanjut meski < 75.
 * - Posttest Materi: 2 kali gagal berturut-turut -> blokir 10 menit (persisten di Room & Firestore).
 */
object AssessmentCooldownConfig {
    const val QUIZ_COOLDOWN_MINUTES: Long = 0L
    const val POSTTEST_COOLDOWN_MINUTES: Long = 10L
    const val POSTTEST_MAX_CONSECUTIVE_FAILS: Int = 2

    const val QUIZ_COOLDOWN_MS: Long = QUIZ_COOLDOWN_MINUTES * 60 * 1000L
    const val POSTTEST_COOLDOWN_MS: Long = POSTTEST_COOLDOWN_MINUTES * 60 * 1000L

    fun getCooldownDurationMs(assessmentType: String): Long {
        return if (assessmentType.equals("posttest", ignoreCase = true)) {
            POSTTEST_COOLDOWN_MS
        } else {
            QUIZ_COOLDOWN_MS
        }
    }

    /**
     * Menghitung sisa jeda dari entity cooldown (mendukung hitungan 2x gagal Posttest).
     */
    fun getRemainingBlockedMs(
        cooldown: AssessmentCooldownEntity?,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Long {
        if (cooldown == null || cooldown.blockedUntil <= 0L) return 0L
        return (cooldown.blockedUntil - currentTimeMillis).coerceAtLeast(0L)
    }

    fun getRemainingBlockedSeconds(
        cooldown: AssessmentCooldownEntity?,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Long {
        val remainingMs = getRemainingBlockedMs(cooldown, currentTimeMillis)
        return if (remainingMs <= 0L) 0L else (remainingMs + 999L) / 1000L
    }

    fun isBlocked(
        cooldown: AssessmentCooldownEntity?,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Boolean {
        return getRemainingBlockedMs(cooldown, currentTimeMillis) > 0L
    }

    /**
     * Menghitung sisa jeda dalam milidetik dari timestamp kegagalan terakhir.
     * Kuis mengembalikan 0L (tanpa cooldown).
     */
    fun getRemainingCooldownMs(
        lastFailedAt: Long,
        assessmentType: String,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Long {
        if (!assessmentType.equals("posttest", ignoreCase = true)) return 0L
        if (lastFailedAt <= 0L) return 0L
        val elapsedMs = (currentTimeMillis - lastFailedAt).coerceAtLeast(0L)
        return (POSTTEST_COOLDOWN_MS - elapsedMs).coerceAtLeast(0L)
    }

    fun getRemainingCooldownSeconds(
        lastFailedAt: Long,
        assessmentType: String,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Long {
        val remainingMs = getRemainingCooldownMs(lastFailedAt, assessmentType, currentTimeMillis)
        return if (remainingMs <= 0L) 0L else (remainingMs + 999L) / 1000L
    }

    fun isCooldownActive(
        lastFailedAt: Long,
        assessmentType: String,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Boolean {
        return getRemainingCooldownMs(lastFailedAt, assessmentType, currentTimeMillis) > 0L
    }
}
