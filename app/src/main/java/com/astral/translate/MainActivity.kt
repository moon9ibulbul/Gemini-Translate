package com.astral.translate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.astral.translate.data.ThemeOption
import com.astral.translate.data.UserSettings
import com.astral.translate.data.DEFAULT_LEFT_PROMPT
import com.astral.translate.data.DEFAULT_RIGHT_PROMPT
import com.astral.translate.ui.MainViewModel
import com.astral.translate.ui.TranslationUiState
import com.astral.translate.ui.theme.AstralTLTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel> {
        val app = application as AstralTLApp
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app.settingsRepository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            AstralTLTheme(themeOption = uiState.first.theme) {
                MainScreen(
                    settings = uiState.first,
                    state = uiState.second,
                    onInputChange = viewModel::updateInput,
                    onTranslate = { viewModel.translate(uiState.first) },
                    onApiKeyChange = viewModel::updateApiKey,
                    onModelChange = viewModel::updateModel,
                    onLeftPromptChange = viewModel::updateLeftPrompt,
                    onRightPromptChange = viewModel::updateRightPrompt,
                    onThemeChange = viewModel::updateTheme,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    settings: UserSettings,
    state: TranslationUiState,
    onInputChange: (String) -> Unit,
    onTranslate: () -> Unit,
    onApiKeyChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onLeftPromptChange: (String) -> Unit,
    onRightPromptChange: (String) -> Unit,
    onThemeChange: (ThemeOption) -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSettings by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { showError = it }
    }

    showError?.let { message ->
        AlertDialog(
            onDismissRequest = { showError = null },
            confirmButton = {
                TextButton(onClick = { showError = null }) { Text(text = "Tutup") }
            },
            title = { Text(text = "Terjadi Kesalahan") },
            text = { Text(message) },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Pengaturan")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextFieldWithLabel(
                value = state.input,
                onValueChange = onInputChange,
                label = "Masukkan teks sumber",
                placeholder = "Tempel teks di sini",
                singleLine = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = {
                    keyboardController?.hide()
                    onTranslate()
                })
            )

            Button(
                onClick = {
                    keyboardController?.hide()
                    onTranslate()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                Text(text = if (state.isLoading) "Menerjemahkan..." else "Terjemahkan")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TranslationCard(
                    title = "Natural",
                    description = "Gaya percakapan lembut",
                    text = state.leftTranslation,
                    onCopy = {
                        clipboard.setText(androidx.compose.ui.text.AnnotatedString(state.leftTranslation))
                        scope.launch { snackbarHostState.showSnackbar("Hasil natural tersalin") }
                    },
                    modifier = Modifier.weight(1f)
                )
                TranslationCard(
                    title = "Semi Formal",
                    description = "Literal dan terstruktur",
                    text = state.rightTranslation,
                    onCopy = {
                        clipboard.setText(androidx.compose.ui.text.AnnotatedString(state.rightTranslation))
                        scope.launch { snackbarHostState.showSnackbar("Hasil semi formal tersalin") }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            LineByLineComparison(
                leftText = state.leftTranslation,
                rightText = state.rightTranslation,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showSettings) {
        ModalBottomSheet(onDismissRequest = { showSettings = false }) {
            SettingsSheet(
                settings = settings,
                onApiKeyChange = onApiKeyChange,
                onModelChange = onModelChange,
                onLeftPromptChange = onLeftPromptChange,
                onRightPromptChange = onRightPromptChange,
                onThemeChange = onThemeChange,
            )
        }
    }
}

@Composable
fun TranslationCard(
    title: String,
    description: String,
    text: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title)
                    Text(text = description, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = onCopy) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = "Salin")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text.ifBlank { "Hasil akan tampil di sini" },
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
fun TextFieldWithLabel(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    singleLine: Boolean,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    androidx.compose.material3.OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        placeholder = { Text(text = placeholder) },
        singleLine = singleLine,
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )
}

@Composable
fun LineByLineComparison(
    leftText: String,
    rightText: String,
    modifier: Modifier = Modifier,
) {
    val hasContent = leftText.isNotBlank() || rightText.isNotBlank()
    val leftLines = leftText.split("\n")
    val rightLines = rightText.split("\n")
    val maxLines = maxOf(leftLines.size, rightLines.size)
    val linePairs = List(maxLines) { index ->
        leftLines.getOrNull(index).orEmpty() to rightLines.getOrNull(index).orEmpty()
    }

    Column(modifier = modifier) {
        Text(text = "Perbandingan per baris", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outline)
        ) {
            if (!hasContent) {
                Text(
                    text = "Hasil per baris akan tampil di sini",
                    modifier = Modifier.padding(12.dp),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                )
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    linePairs.forEachIndexed { index, (leftLine, rightLine) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = leftLine,
                                modifier = Modifier.weight(1f),
                                fontFamily = FontFamily.Monospace,
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = rightLine,
                                modifier = Modifier.weight(1f),
                                fontFamily = FontFamily.Monospace,
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            )
                        }
                        if (index != linePairs.lastIndex) Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSheet(
    settings: UserSettings,
    onApiKeyChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onLeftPromptChange: (String) -> Unit,
    onRightPromptChange: (String) -> Unit,
    onThemeChange: (ThemeOption) -> Unit,
) {
    var apiKeyText by rememberSaveable { mutableStateOf(settings.apiKey) }
    var modelText by rememberSaveable { mutableStateOf(settings.model) }
    var leftPromptText by rememberSaveable { mutableStateOf(settings.leftPrompt) }
    var rightPromptText by rememberSaveable { mutableStateOf(settings.rightPrompt) }

    LaunchedEffect(settings.apiKey) { apiKeyText = settings.apiKey }
    LaunchedEffect(settings.model) { modelText = settings.model }
    LaunchedEffect(settings.leftPrompt) { leftPromptText = settings.leftPrompt }
    LaunchedEffect(settings.rightPrompt) { rightPromptText = settings.rightPrompt }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Pengaturan", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
        TextFieldWithLabel(
            value = apiKeyText,
            onValueChange = {
                apiKeyText = it
                onApiKeyChange(it)
            },
            label = "API Key Gemini",
            placeholder = "Masukkan API key",
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        TextFieldWithLabel(
            value = modelText,
            onValueChange = {
                modelText = it
                onModelChange(it)
            },
            label = "Model (default gemini-3-pro-preview)",
            placeholder = "gemini-3-pro-preview",
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text(text = "Tema")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeOption.values().forEach { option ->
                val isSelected = option == settings.theme
                Button(
                    onClick = { onThemeChange(option) },
                    modifier = Modifier.weight(1f),
                    enabled = !isSelected
                ) { Text(text = option.name.lowercase().replaceFirstChar { it.titlecase() }) }
            }
        }
        Text(text = "Gaya Terjemahan Natural")
        TextFieldWithLabel(
            value = leftPromptText,
            onValueChange = {
                leftPromptText = it
                onLeftPromptChange(it)
            },
            label = "Prompt gaya natural",
            placeholder = DEFAULT_LEFT_PROMPT,
            singleLine = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        )
        Text(text = "Gaya Terjemahan Semi Formal")
        TextFieldWithLabel(
            value = rightPromptText,
            onValueChange = {
                rightPromptText = it
                onRightPromptChange(it)
            },
            label = "Prompt gaya semi formal",
            placeholder = DEFAULT_RIGHT_PROMPT,
            singleLine = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    AstralTLTheme(themeOption = ThemeOption.SYSTEM) {
        MainScreen(
            settings = UserSettings(),
            state = TranslationUiState(),
            onInputChange = {},
            onTranslate = {},
            onApiKeyChange = {},
            onModelChange = {},
            onLeftPromptChange = {},
            onRightPromptChange = {},
            onThemeChange = {},
        )
    }
}
