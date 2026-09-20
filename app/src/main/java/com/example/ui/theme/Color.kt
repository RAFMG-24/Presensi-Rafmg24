package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Official DAMKAR Subang (Yudha Brama Jaya) Palette
val DamkarNavy = Color(0xFF0C3875) // Brand Navy
val DamkarPrimaryLight = Color(0xFF0C3875) // Primary Biru Tua (Light mode)
val DamkarPrimaryDark = Color(0xFF93C5FD)  // Primary Biru Langit Cerah (High contrast in Dark mode)
val DamkarPrimaryDarkBackground = Color(0xFF07234B)
val DamkarPrimaryContainer = Color(0xFFE0ECFF)
val DamkarOnPrimaryContainer = Color(0xFF041935)

val DamkarAccentGold = Color(0xFFFFB800) // Accent Kuning Emas
val DamkarGoldContainer = Color(0xFFFFF3CD)
val DamkarGoldDarkLight = Color(0xFFD97706)
val DamkarGoldDarkDark = Color(0xFFFBBF24)

val DamkarGoldDark: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) DamkarGoldDarkDark else DamkarGoldDarkLight

val DamkarPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) DamkarPrimaryDark else DamkarPrimaryLight

val DamkarDanger = Color(0xFFDC2626) // Danger Merah
val DamkarDangerContainer = Color(0xFFFEE2E2)
val DamkarOnDangerContainer = Color(0xFF7F1D1D)

val DamkarSuccess = Color(0xFF16A34A)
val DamkarSuccessContainer = Color(0xFFDCFCE7)

val DamkarInfo = Color(0xFF0284C7)
val DamkarInfoContainer = Color(0xFFE0F2FE)

// Light Mode Canvas & Text
val DamkarBackgroundLight = Color(0xFFF8FAFC)
val DamkarSurfaceLight = Color(0xFFFFFFFF)
val DamkarSurfaceVariantLight = Color(0xFFF1F5F9)
val DamkarTextPrimaryLight = Color(0xFF0F172A)
val DamkarTextSecondaryLight = Color(0xFF64748B)
val DamkarBorderLightMode = Color(0xFFCBD5E1)
val DamkarBorderSubtleLight = Color(0xFFE2E8F0)

// Dark Mode Canvas & Text
val DamkarBackgroundDark = Color(0xFF0B1322)
val DamkarSurfaceDark = Color(0xFF131F37)
val DamkarSurfaceVariantDark = Color(0xFF1E2D4A)
val DamkarTextPrimaryDark = Color(0xFFF8FAFC)
val DamkarTextSecondaryDark = Color(0xFF94A3B8)
val DamkarBorderDarkMode = Color(0xFF334155)
val DamkarBorderSubtleDark = Color(0xFF1E293B)

// Dynamic accessors that adapt based on darkTheme
val DamkarBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) DamkarBackgroundDark else DamkarBackgroundLight

val DamkarSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) DamkarSurfaceDark else DamkarSurfaceLight

val DamkarSurfaceVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) DamkarSurfaceVariantDark else DamkarSurfaceVariantLight

val DamkarTextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) DamkarTextPrimaryDark else DamkarTextPrimaryLight

val DamkarTextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) DamkarTextSecondaryDark else DamkarTextSecondaryLight

val DamkarBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) DamkarBorderDarkMode else DamkarBorderLightMode

val DamkarBorderLight: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) DamkarBorderSubtleDark else DamkarBorderSubtleLight

