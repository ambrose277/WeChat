package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = WeChatOrange,
    onPrimary = Color.White,
    primaryContainer = WeChatOrangeDark,
    onPrimaryContainer = Color.White,
    secondary = WeChatGreen,
    onSecondary = Color.White,
    secondaryContainer = WeChatGreenDark,
    onSecondaryContainer = Color.White,
    background = WeChatDarkBg,
    onBackground = Color(0xFFECECEC),
    surface = WeChatDarkSurface,
    onSurface = Color(0xFFECECEC),
    surfaceVariant = WeChatDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = WeChatDarkGray
)

private val LightColorScheme = lightColorScheme(
    primary = WeChatOrange,
    onPrimary = Color.White,
    primaryContainer = WeChatOrangeContainer,
    onPrimaryContainer = WeChatOrangeDark,
    secondary = WeChatGreen,
    onSecondary = Color.White,
    secondaryContainer = WeChatGreenContainer,
    onSecondaryContainer = WeChatGreenDark,
    background = WeChatLightBg,
    onBackground = Color(0xFF1F1F1F),
    surface = WeChatLightSurface,
    onSurface = Color(0xFF1F1F1F),
    surfaceVariant = WeChatLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF5A5A5A),
    outline = WeChatLightGray
)

@Composable
fun WeChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
