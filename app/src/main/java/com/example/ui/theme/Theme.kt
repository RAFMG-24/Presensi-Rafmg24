package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = DamkarPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = DamkarPrimaryContainer,
    onPrimaryContainer = DamkarOnPrimaryContainer,
    secondary = DamkarAccentGold,
    onSecondary = Color(0xFF3E2800),
    secondaryContainer = DamkarGoldContainer,
    onSecondaryContainer = Color(0xFF523600),
    tertiary = DamkarInfo,
    onTertiary = Color.White,
    tertiaryContainer = DamkarInfoContainer,
    error = DamkarDanger,
    onError = Color.White,
    errorContainer = DamkarDangerContainer,
    onErrorContainer = DamkarOnDangerContainer,
    background = DamkarBackgroundLight,
    onBackground = DamkarTextPrimaryLight,
    surface = DamkarSurfaceLight,
    onSurface = DamkarTextPrimaryLight,
    surfaceVariant = DamkarSurfaceVariantLight,
    onSurfaceVariant = DamkarTextSecondaryLight,
    outline = DamkarBorderLightMode
)

private val DarkColorScheme = darkColorScheme(
    primary = DamkarPrimaryDark,
    onPrimary = Color(0xFF03264F),
    primaryContainer = Color(0xFF0C3875),
    onPrimaryContainer = Color(0xFFD3E4FF),
    secondary = DamkarAccentGold,
    onSecondary = Color(0xFF3E2800),
    secondaryContainer = Color(0xFF5A3E00),
    error = Color(0xFFFFB4AB),
    background = DamkarBackgroundDark,
    onBackground = DamkarTextPrimaryDark,
    surface = DamkarSurfaceDark,
    onSurface = DamkarTextPrimaryDark,
    surfaceVariant = DamkarSurfaceVariantDark,
    onSurfaceVariant = DamkarTextSecondaryDark,
    outline = DamkarBorderDarkMode
)

@Composable
fun DamkarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MyApplicationTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve official Damkar governmental branding
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
