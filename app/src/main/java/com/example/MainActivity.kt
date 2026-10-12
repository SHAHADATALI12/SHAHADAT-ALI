package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.BlueprintScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.GuideScreen
import com.example.ui.theme.JarvisBgDark
import com.example.ui.theme.JarvisBorderCyan
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisNeonGreen
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.JarvisScreen
import com.example.viewmodel.JarvisViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: JarvisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }

                // Handle back button for secondary screens
                BackHandler(enabled = uiState.currentScreen != JarvisScreen.CORE_ASSISTANT) {
                    viewModel.setScreen(JarvisScreen.CORE_ASSISTANT)
                }

                LaunchedEffect(uiState.bannerMessage) {
                    uiState.bannerMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearBanner()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    containerColor = JarvisBgDark,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        JarvisBottomNavigation(
                            currentScreen = uiState.currentScreen,
                            onScreenSelected = { viewModel.setScreen(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (uiState.currentScreen) {
                            JarvisScreen.CORE_ASSISTANT -> {
                                AssistantScreen(
                                    state = uiState,
                                    onToggleListening = { viewModel.toggleListening() },
                                    onSendQuery = { viewModel.processUserCommand(it) },
                                    onToggleTorch = { viewModel.toggleTorch() },
                                    onToggleMute = { viewModel.toggleMute() },
                                    onRepeatSpeech = { viewModel.testVoiceSynthesizer(it) },
                                    onSetAge = { viewModel.setUserAge(it) }
                                )
                            }
                            JarvisScreen.SETUP_GUIDES -> {
                                GuideScreen(
                                    state = uiState,
                                    onCategorySelected = { viewModel.setGuideCategory(it) },
                                    onSearchQueryChanged = { viewModel.setGuideSearchQuery(it) },
                                    onToggleExpanded = { viewModel.toggleGuideExpanded(it) },
                                    onAskJarvis = {
                                        viewModel.setScreen(JarvisScreen.CORE_ASSISTANT)
                                        viewModel.processUserCommand(it)
                                    },
                                    onShowBanner = { viewModel.showBanner(it) }
                                )
                            }
                            JarvisScreen.BLUEPRINTS -> {
                                BlueprintScreen(
                                    state = uiState,
                                    onSelectTrigger = { viewModel.selectBlueprintTrigger(it) },
                                    onSelectAction = { viewModel.selectBlueprintAction(it) },
                                    onExecuteProtocol = {
                                        viewModel.setScreen(JarvisScreen.CORE_ASSISTANT)
                                        viewModel.processUserCommand(it)
                                    },
                                    onShowBanner = { viewModel.showBanner(it) }
                                )
                            }
                            JarvisScreen.DIAGNOSTICS -> {
                                DiagnosticsScreen(
                                    state = uiState,
                                    onRefreshTelemetry = { viewModel.refreshTelemetry() },
                                    onUpdateVoiceSettings = { pitch, rate, lang ->
                                        viewModel.updateVoiceSettings(pitch, rate, lang)
                                    },
                                    onTestVoice = { viewModel.testVoiceSynthesizer(it) },
                                    onToggleTorch = { viewModel.toggleTorch() },
                                    onSetAge = { viewModel.setUserAge(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JarvisBottomNavigation(
    currentScreen: JarvisScreen,
    onScreenSelected: (JarvisScreen) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = JarvisBorderCyan.copy(alpha = 0.5f)
            ),
        containerColor = JarvisSurfaceDark,
        contentColor = JarvisTextPrimary
    ) {
        // 1. Assistant Core
        NavigationBarItem(
            selected = currentScreen == JarvisScreen.CORE_ASSISTANT,
            onClick = { onScreenSelected(JarvisScreen.CORE_ASSISTANT) },
            icon = {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "Neural Core",
                    modifier = Modifier.testTag("nav_neural_core")
                )
            },
            label = {
                Text(
                    text = "CORE",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (currentScreen == JarvisScreen.CORE_ASSISTANT) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = JarvisCyan,
                selectedTextColor = JarvisCyan,
                indicatorColor = JarvisSurfaceDark,
                unselectedIconColor = JarvisTextSecondary,
                unselectedTextColor = JarvisTextSecondary
            )
        )

        // 2. Setup Guides
        NavigationBarItem(
            selected = currentScreen == JarvisScreen.SETUP_GUIDES,
            onClick = { onScreenSelected(JarvisScreen.SETUP_GUIDES) },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = "Guides",
                    modifier = Modifier.testTag("nav_setup_guides")
                )
            },
            label = {
                Text(
                    text = "GUIDES",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (currentScreen == JarvisScreen.SETUP_GUIDES) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = JarvisGold,
                selectedTextColor = JarvisGold,
                indicatorColor = JarvisSurfaceDark,
                unselectedIconColor = JarvisTextSecondary,
                unselectedTextColor = JarvisTextSecondary
            )
        )

        // 3. Blueprints
        NavigationBarItem(
            selected = currentScreen == JarvisScreen.BLUEPRINTS,
            onClick = { onScreenSelected(JarvisScreen.BLUEPRINTS) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Blueprints",
                    modifier = Modifier.testTag("nav_blueprints")
                )
            },
            label = {
                Text(
                    text = "BLUEPRINT",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (currentScreen == JarvisScreen.BLUEPRINTS) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = JarvisNeonGreen,
                selectedTextColor = JarvisNeonGreen,
                indicatorColor = JarvisSurfaceDark,
                unselectedIconColor = JarvisTextSecondary,
                unselectedTextColor = JarvisTextSecondary
            )
        )

        // 4. Diagnostics & Voice Lab
        NavigationBarItem(
            selected = currentScreen == JarvisScreen.DIAGNOSTICS,
            onClick = { onScreenSelected(JarvisScreen.DIAGNOSTICS) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "Diagnostics",
                    modifier = Modifier.testTag("nav_diagnostics")
                )
            },
            label = {
                Text(
                    text = "VOICE LAB",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (currentScreen == JarvisScreen.DIAGNOSTICS) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = JarvisCyan,
                selectedTextColor = JarvisCyan,
                indicatorColor = JarvisSurfaceDark,
                unselectedIconColor = JarvisTextSecondary,
                unselectedTextColor = JarvisTextSecondary
            )
        )
    }
}
