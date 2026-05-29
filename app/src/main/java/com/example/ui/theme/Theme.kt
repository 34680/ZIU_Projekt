package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BentoPrimaryDark,
    secondary = BentoSecondaryDark,
    tertiary = BentoFocusCardBgDark,
    onTertiary = BentoFocusCardTextDark,
    primaryContainer = BentoQuickCountBgDark,
    onPrimaryContainer = Color(0xFFF3EDF7),
    background = BentoBgDark,
    surface = Color(0xFF1D1B20),
    onBackground = BentoTextPrimaryDark,
    onSurface = BentoTextPrimaryDark,
    onPrimary = BentoOnPrimaryDark,
    surfaceVariant = BentoCardProgressBgDark,
    onSurfaceVariant = BentoTextMutedDark,
    outline = BentoCardBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = BentoPrimary,
    secondary = BentoSecondary,
    tertiary = BentoFocusCardBg,
    onTertiary = BentoFocusCardText,
    primaryContainer = BentoQuickCountBg,
    onPrimaryContainer = Color(0xFF21005D),
    background = BentoBgLight,
    surface = Color(0xFFFFFFFF),
    onBackground = BentoTextPrimaryLight,
    onSurface = BentoTextPrimaryLight,
    onPrimary = BentoOnPrimary,
    surfaceVariant = BentoCardProgressBg,
    onSurfaceVariant = BentoTextMutedLight,
    outline = BentoCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disable dynamic colors to enforce our branded high-contrast UI theme
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
