package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AccentTeal,
    onPrimary = DeepNavy,
    primaryContainer = DarkNavyElevated,
    onPrimaryContainer = SoftIvory,
    secondary = AccentCoral,
    onSecondary = SoftIvory,
    tertiary = AccentAmber,
    onTertiary = DeepNavy,
    background = DeepNavy,
    onBackground = SoftIvory,
    surface = DarkNavyCard,
    onSurface = SoftIvory,
    surfaceVariant = DarkNavyElevated,
    onSurfaceVariant = IvorySecondary,
    outline = NavyBorder
)

private val LightColorScheme = DarkColorScheme // Vector Exit is intentionally styled with deep navy & luminous accents

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent high-contrast puzzle aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
