package com.astral.translate.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astral.translate.data.GeminiTranslator
import com.astral.translate.data.SettingsRepository
import com.astral.translate.data.ThemeOption
import com.astral.translate.data.UserSettings
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TranslationUiState(
    val input: String = "",
    val leftTranslation: String = "",
    val rightTranslation: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class MainViewModel(
    private val settingsRepository: SettingsRepository,
    private val translator: GeminiTranslator = GeminiTranslator(),
) : ViewModel() {

    private val inputText = MutableStateFlow("")
    private val translations = MutableStateFlow(TranslationUiState())

    val uiState: StateFlow<Pair<UserSettings, TranslationUiState>> =
        combine(settingsRepository.settings, translations) { settings, state ->
            settings to state.copy(input = inputText.value)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserSettings() to TranslationUiState()
        )

    fun updateInput(text: String) {
        inputText.value = text
        translations.value = translations.value.copy(input = text)
    }

    fun updateApiKey(value: String) = viewModelScope.launch {
        settingsRepository.updateApiKey(value)
    }

    fun updateModel(value: String) = viewModelScope.launch {
        settingsRepository.updateModel(value)
    }

    fun updateLeftPrompt(value: String) = viewModelScope.launch {
        settingsRepository.updateLeftPrompt(value)
    }

    fun updateRightPrompt(value: String) = viewModelScope.launch {
        settingsRepository.updateRightPrompt(value)
    }

    fun updateTheme(option: ThemeOption) = viewModelScope.launch {
        settingsRepository.updateTheme(option)
    }

    fun translate(settings: UserSettings) {
        val currentInput = inputText.value
        viewModelScope.launch {
            translations.value = translations.value.copy(isLoading = true, errorMessage = null)
            try {
                val (left, right) = translateDual(settings, currentInput)
                translations.value = translations.value.copy(
                    leftTranslation = left,
                    rightTranslation = right,
                    isLoading = false,
                )
            } catch (e: Exception) {
                translations.value = translations.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Terjadi kesalahan tidak dikenal",
                )
            }
        }
    }

    private suspend fun translateDual(settings: UserSettings, text: String): Pair<String, String> {
        return kotlinx.coroutines.coroutineScope {
            val left = async {
                translator.translate(
                    apiKey = settings.apiKey,
                    model = settings.model,
                    sourceText = text,
                    stylePrompt = settings.leftPrompt,
                )
            }
            val right = async {
                translator.translate(
                    apiKey = settings.apiKey,
                    model = settings.model,
                    sourceText = text,
                    stylePrompt = settings.rightPrompt,
                )
            }
            left.await() to right.await()
        }
    }
}
