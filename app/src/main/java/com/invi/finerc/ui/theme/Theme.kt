package com.invi.finerc.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// CRED-style color scheme
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00D4AA), // CRED green
    onPrimary = Color.Black,
    secondary = Color(0xFF1A1A1A), // Dark gray
    tertiary = Color(0xFF2A2A2A), // Lighter dark gray
    background = Color(0xFF0A0A0A), // Very dark background
    surface = Color(0xFF1A1A1A), // Card background
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFCCCCCC),
    outline = Color(0xFF333333),
    outlineVariant = Color(0xFF444444),
    scrim = Color(0x80000000),
    inverseSurface = Color(0xFFE0E0E0),
    inverseOnSurface = Color(0xFF1A1A1A),
    inversePrimary = Color(0xFF00B894)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00D4AA),
    onPrimary = Color.White,
    secondary = Color(0xFFF5F5F5),
    tertiary = Color(0xFFE0E0E0),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onBackground = Color.Black,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF666666),
    outline = Color(0xFFCCCCCC),
    outlineVariant = Color(0xFFE0E0E0),
    scrim = Color(0x80000000),
    inverseSurface = Color(0xFF1A1A1A),
    inverseOnSurface = Color(0xFFF5F5F5),
    inversePrimary = Color(0xFF00B894)
)

@Composable
fun FinercTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
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
        content = content
    )
} 