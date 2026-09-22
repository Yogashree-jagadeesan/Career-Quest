package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PeacockBlue,
    onPrimary = Color.White,
    primaryContainer = TurquoiseSoft,
    onPrimaryContainer = PeacockBlueDark,
    secondary = LotusPink,
    onSecondary = Color.White,
    secondaryContainer = LotusPinkLight,
    onSecondaryContainer = LotusPinkRose,
    tertiary = GoldYellow,
    onTertiary = Color.White,
    tertiaryContainer = GoldYellowLight,
    onTertiaryContainer = GoldTrim,
    background = CreamBackground,
    onBackground = TextPrimary,
    surface = CreamSurface,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF1EBE1),
    onSurfaceVariant = TextSecondary,
    outline = CreamCardBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = TurquoiseAccent,
    onPrimary = PeacockBlueDark,
    primaryContainer = PeacockBlue,
    onPrimaryContainer = TurquoiseSoft,
    secondary = LotusPink,
    onSecondary = Color.White,
    secondaryContainer = LotusPinkRose,
    onSecondaryContainer = LotusPinkLight,
    tertiary = GoldYellowWarm,
    onTertiary = PeacockBlueDark,
    tertiaryContainer = GoldTrim,
    onTertiaryContainer = GoldYellowLight,
    background = PeacockBlueDark,
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF0F3854),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF16486B),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF1E5B84)
)

@Composable
fun CareerQuestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Keep it bright and inviting as requested: avoid excessively dark interface
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
