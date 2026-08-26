package com.example.prestamolabctma.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = SurfaceWhite,
    primaryContainer = PrimaryBlueLight,
    onPrimaryContainer = PrimaryBlue,
    secondary = AccentGreen,
    onSecondary = SurfaceWhite,
    secondaryContainer = AccentGreenLight,
    onSecondaryContainer = StatusGreen,
    background = BackgroundSlate,
    surface = SurfaceWhite,
    onSurface = TextDark,
    onBackground = TextDark,
    outline = TextGray.copy(alpha = 0.2f)
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    secondary = AccentGreen,
    tertiary = Pink80
)

val Shapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun PrestamoLabCTMATheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
