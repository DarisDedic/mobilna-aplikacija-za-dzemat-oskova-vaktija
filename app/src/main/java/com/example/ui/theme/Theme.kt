package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkEmeraldColorScheme = darkColorScheme(
    primary = EmeraldPrimaryDark,
    onPrimary = Color(0xFF042F24),
    primaryContainer = EmeraldDarker,
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = GoldAccent,
    onSecondary = Color(0xFF451A03),
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = GoldLight,
    tertiary = GoldAccentBright,
    onTertiary = Color(0xFF451A03),
    background = DarkBackground,
    onBackground = TextHighContrastDark,
    surface = DarkSurface,
    onSurface = TextHighContrastDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextMediumContrastDark,
    outline = DarkSurfaceBorder,
    error = ErrorRed
)

private val LightEmeraldColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = EmeraldDarker,
    secondary = GoldMuted,
    onSecondary = Color.White,
    secondaryContainer = GoldLight,
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = Color(0xFFB45309),
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = TextHighContrastLight,
    surface = LightSurface,
    onSurface = TextHighContrastLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextMediumContrastLight,
    outline = LightSurfaceBorder,
    error = ErrorRed
)

@Composable
fun VaktijaTheme(
    themeMode: String = "SYSTEM", // "SYSTEM", "DARK", "LIGHT"
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkEmeraldColorScheme else LightEmeraldColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

