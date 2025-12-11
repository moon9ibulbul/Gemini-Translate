package com.astral.translate.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val SETTINGS_NAME = "astraltl_settings"

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = SETTINGS_NAME)

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val apiKey = stringPreferencesKey("api_key")
        val model = stringPreferencesKey("model")
        val leftPrompt = stringPreferencesKey("left_prompt")
        val rightPrompt = stringPreferencesKey("right_prompt")
        val theme = intPreferencesKey("theme")
    }

    val settings: Flow<UserSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            val themeOrdinal = prefs[Keys.theme]
            val themeOption = ThemeOption.entries.getOrElse(themeOrdinal ?: ThemeOption.SYSTEM.ordinal) { ThemeOption.SYSTEM }
            UserSettings(
                apiKey = prefs[Keys.apiKey] ?: "",
                model = prefs[Keys.model] ?: "gemini-3-pro-preview",
                leftPrompt = prefs[Keys.leftPrompt] ?: DEFAULT_LEFT_PROMPT,
                rightPrompt = prefs[Keys.rightPrompt] ?: DEFAULT_RIGHT_PROMPT,
                theme = themeOption
            )
        }

    suspend fun updateApiKey(apiKey: String) {
        dataStore.edit { prefs -> prefs[Keys.apiKey] = apiKey }
    }

    suspend fun updateModel(model: String) {
        dataStore.edit { prefs -> prefs[Keys.model] = model }
    }

    suspend fun updateLeftPrompt(prompt: String) {
        dataStore.edit { prefs -> prefs[Keys.leftPrompt] = prompt }
    }

    suspend fun updateRightPrompt(prompt: String) {
        dataStore.edit { prefs -> prefs[Keys.rightPrompt] = prompt }
    }

    suspend fun updateTheme(themeOption: ThemeOption) {
        dataStore.edit { prefs -> prefs[Keys.theme] = themeOption.ordinal }
    }
}
