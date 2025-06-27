package com.milikovv.linguacontext.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    onBack: () -> Unit
) {
    var baseUrlField by remember { mutableStateOf(settings.baseUrl) }
    var modelNameField by remember { mutableStateOf(settings.modelName) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val keyboardController = LocalSoftwareKeyboardController.current

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
                // Label + Rounded OutlinedTextField 1
                Text(
                    text = stringResource(R.string.ip_port),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                val baseUrlUpdated = stringResource(id = R.string.base_url_updated)
                OutlinedTextField(
                    value = baseUrlField,
                    singleLine = true,
                    onValueChange = { baseUrlField = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onBaseUrlChanged(baseUrlField)
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

                // Label + Rounded OutlinedTextField 3
                Text(
                    text = stringResource(R.string.llm_model),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                val modelNameUpdated = stringResource(id = R.string.model_name_updated)
                OutlinedTextField(
                    value = modelNameField,
                    singleLine = true,
                    onValueChange = { modelNameField = it },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onModelNameChanged(modelNameField)
                            keyboardController?.hide()
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = modelNameUpdated,
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    )
                )
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    LinguaContextTheme {
        SettingsScreen(Settings(), {}, {}, {})
    }
}

