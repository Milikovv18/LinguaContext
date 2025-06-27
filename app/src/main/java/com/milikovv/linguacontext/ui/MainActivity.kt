package com.milikovv.linguacontext.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.gif.AnimatedImageDecoder
import coil3.request.ImageRequest
import com.milikovv.linguacontext.R
import com.milikovv.linguacontext.ui.theme.LinguaContextTheme
import com.milikovv.linguacontext.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val settingsState by viewModel.storageState.collectAsState()

                MyApp(
                    settingsState,
                    onBaseUrlChanged = { viewModel.updateBaseUrl(it) },
                    onModelNameChanged = { viewModel.updateModelName(it) },
                    onOpenSettingsClick = this::openAccessibilitySettings
                )
            }
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }
}

@Composable
fun MyApp(
    settings: com.milikovv.linguacontext.utils.Settings,
    onBaseUrlChanged: (String) -> Unit,
    onModelNameChanged: (String) -> Unit,
    onOpenSettingsClick: () -> Unit
) {
    val navController = rememberNavController()
    AppNavHost(
        settings,
        navController = navController,
        onBaseUrlChanged = onBaseUrlChanged,
        onModelNameChanged = onModelNameChanged,
        onOpenSettingsClick = onOpenSettingsClick
    )
}

// Navigation graph composable
@Composable
fun AppNavHost(
    settings: com.milikovv.linguacontext.utils.Settings,
    navController: NavHostController,
    onBaseUrlChanged: (String) -> Unit,
    onModelNameChanged: (String) -> Unit,
    onOpenSettingsClick: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "main",
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
    ) {
        composable("main") {
            Surface(
                modifier = Modifier.fillMaxSize()
            ) {
                TranslatorScreen(
                    ipAddress = "192.168.1.1",
                    onEditIpClick = { navController.navigate("edit_ip") },
                    onOpenSettingsClick = onOpenSettingsClick,
                )
            }
        }
        composable("edit_ip") {
            SettingsScreen(
                settings,
                onBaseUrlChanged = onBaseUrlChanged,
                onModelNameChanged = onModelNameChanged,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorScreen(
    ipAddress: String,
    onEditIpClick: () -> Unit,
    onOpenSettingsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = onOpenSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        content = { padding ->
            val context = LocalContext.current

            // Create ImageLoader with GIF support for Coil 3
            val imageLoader = ImageLoader.Builder(context)
                .components {
                    add(AnimatedImageDecoder.Factory())
                }
                .build()

            Column(
                modifier = Modifier
                    .padding(vertical = padding.calculateTopPadding() + 16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(32.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(R.drawable.scrolling_rectangles)
                            .build(),
                        imageLoader = imageLoader,
                        contentDescription = "Main GIF",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Magnifying Glass",
                        modifier = Modifier.fillMaxSize(),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onEditIpClick() }
                ) {
                    Text(
                        text = ipAddress,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "click to edit",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    LinguaContextTheme {
        TranslatorScreen("127.0.0.1", {}, {})
    }
}
