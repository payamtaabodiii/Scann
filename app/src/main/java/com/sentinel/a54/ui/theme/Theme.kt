package com.sentinel.a54.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val CoreBlack = Color(0xFF0F0F0F)
val SurfaceDark = Color(0xFF161616)
val SurfaceVariantDark = Color(0xFF222222)
val AccentGreen = Color(0xFF00FF7F)
val ErrorRed = Color(0xFFFF4C4C)

private val DarkColorScheme = darkColorScheme(
    primary = AccentGreen,
    secondary = Color(0xFFAAAAAA),
    background = CoreBlack,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onPrimary = CoreBlack,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    error = ErrorRed,
    onError = CoreBlack
)

@Composable
fun SentinelTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Ensure typography exists or define it
        content = content
    )
}
