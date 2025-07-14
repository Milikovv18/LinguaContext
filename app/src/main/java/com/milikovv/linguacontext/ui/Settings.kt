package com.milikovv.linguacontext.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.milikovv.linguacontext.R
import com.milikovv.linguacontext.ui.theme.LinguaContextTheme
import com.milikovv.linguacontext.utils.Settings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: Settings,
    onBaseUrlChanged: (String) -> Unit,
    onModelNameChanged: (String) -> Unit,
    onThinkModeChanged: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.imePadding())
        },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_llm_data)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.description_go_back)
                        )
                    }
                },
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = padding.calculateTopPadding() + 16.dp, horizontal = 16.dp)
            ) {
                TextInputField(
                    text = settings.baseUrl,
                    header = stringResource(R.string.ip_port),
                    snackbarText = stringResource(id = R.string.base_url_updated),
                    onTextEdited = onBaseUrlChanged,
                    snackbarHostState = snackbarHostState,
                )

                TextInputField(
                    text = settings.modelName,
                    header = stringResource(R.string.llm_model),
                    snackbarText = stringResource(id = R.string.model_name_updated),
                    onTextEdited = onModelNameChanged,
                    snackbarHostState = snackbarHostState
                )

                ToggleField(
                    settingName = "No think",
                    description = "Disable thinking for LLM if supported",
                    checked = settings.thinkDisable,
                    onCheckedChange = onThinkModeChanged
                )
            }
        }
    )
}

@Composable
fun TextInputField(
    text: String,
    header: String,
    snackbarText: String,
    onTextEdited: (String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var textField by remember { mutableStateOf(text) }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    Text(
        text = header,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(bottom = 4.dp)
    )

    val baseUrlUpdated = snackbarText
    OutlinedTextField(
        value = textField,
        singleLine = true,
        onValueChange = { textField = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        keyboardActions = KeyboardActions(
            onDone = {
                onTextEdited(textField)
                keyboardController?.hide()
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = baseUrlUpdated,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        )
    )
}

@Composable
fun ToggleField(
    settingName: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = settingName,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray // Optional: use a less prominent color
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    LinguaContextTheme {
        SettingsScreen(Settings(), {}, {}, {}, {})
    }
}

