package com.example

import com.example.ui.AppLanguage
import com.example.data.preferences.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit test untuk preferensi aksesibilitas (Tahap 8A):
 * - Mode gelap (SYSTEM, LIGHT, DARK)
 * - Bahasa aplikasi (INDONESIAN, ENGLISH)
 */
class PreferencesPhase8ATest {

    @Test
    fun themeMode_fromCode_resolvesCorrectly() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromCode("system"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromCode("light"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromCode("dark"))
        // Case-insensitivity & fallback
        assertEquals(ThemeMode.DARK, ThemeMode.fromCode("DARK"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromCode("unknown_code"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromCode(null))
    }

    @Test
    fun appLanguage_fromCode_resolvesCorrectly() {
        assertEquals(AppLanguage.INDONESIAN, AppLanguage.fromCode("in"))
        assertEquals(AppLanguage.INDONESIAN, AppLanguage.fromCode("id"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode("en"))
        // Fallback
        assertEquals(AppLanguage.INDONESIAN, AppLanguage.fromCode("fr"))
        assertEquals(AppLanguage.INDONESIAN, AppLanguage.fromCode(null))
    }
}
