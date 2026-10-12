package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = JarvisCyan,
    onPrimary = Color(0xFF00202E),
    primaryContainer = JarvisCyanDark,
    onPrimaryContainer = Color(0xFFE2F1FF),
    secondary = JarvisGold,
    onSecondary = Color(0xFF2E1F00),
    secondaryContainer = Color(0xFF473300),
    onSecondaryContainer = Color(0xFFFFECC4),
    tertiary = JarvisCrimson,
    onTertiary = Color.White,
    background = JarvisBgDark,
    onBackground = JarvisTextPrimary,
    surface = JarvisSurfaceDark,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisSurfaceVariantDark,
    onSurfaceVariant = JarvisTextSecondary,
    outline = JarvisBorderCyan,
    outlineVariant = Color(0xFF14273E),
  )

private val LightColorScheme = DarkColorScheme // JARVIS always remains high-tech dark HUD mode for authentic sci-fi experience

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep Stark HUD aesthetic consistent
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
