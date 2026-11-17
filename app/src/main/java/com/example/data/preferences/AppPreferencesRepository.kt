package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fiksfaham_preferences")

enum class ThemeMode(val code: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        val DEFAULT = SYSTEM

        fun fromCode(code: String?): ThemeMode {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: DEFAULT
        }
    }
}

class AppPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val KEY_THEME_MODE = stringPreferencesKey("app_theme_mode")
        val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
    }

    constructor(context: Context) : this(context.dataStore)

    val themeMode: Flow<ThemeMode> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val code = preferences[KEY_THEME_MODE]
            ThemeMode.fromCode(code)
        }

    val appLanguage: Flow<AppLanguage> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            when (preferences[KEY_APP_LANGUAGE]) {
                "en" -> AppLanguage.ENGLISH
                else -> AppLanguage.INDONESIAN
            }
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode.code
        }
    }

    suspend fun setAppLanguage(language: AppLanguage) {
        dataStore.edit { preferences ->
            preferences[KEY_APP_LANGUAGE] = language.code
        }
    }
}
