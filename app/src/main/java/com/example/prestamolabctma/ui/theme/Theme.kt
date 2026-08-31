package com.example.prestamolabctma.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NeonColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonPurple,
    tertiary = NeonPink,
    background = DeepSpace,
    surface = SurfaceDark,
    onBackground = TextWhite,
    onSurface = TextWhite,
    primaryContainer = NeonCyan.copy(alpha = 0.1f),
    onPrimaryContainer = NeonCyan
)

@Composable
fun PrestamoLabCTMATheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NeonColorScheme,
        typography = Typography,
        content = content
    )
}