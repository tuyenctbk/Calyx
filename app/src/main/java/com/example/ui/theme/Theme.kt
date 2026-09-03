package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalIsDarkTheme = staticCompositionLocalOf { true }

val CalyxDarkColorScheme = darkColorScheme(
    primary = CalyxPrimary,
    onPrimary = CalyxOnPrimary,
    primaryContainer = CalyxPrimaryContainer,
    onPrimaryContainer = CalyxOnPrimaryContainer,
    secondary = CalyxSecondary,
    onSecondary = CalyxOnSecondary,
    tertiary = CalyxTertiary,
    onTertiary = CalyxOnTertiary,
    background = CalyxDarkBg,
    onBackground = CalyxTextPrimary,
    surface = CalyxSurface,
    onSurface = CalyxTextPrimary,
    surfaceVariant = CalyxSurfaceVariant,
    onSurfaceVariant = CalyxTextSecondary,
    outline = CalyxOutline
)

val CalyxLightColorScheme = lightColorScheme(
    primary = CalyxLightPrimary,
    onPrimary = CalyxLightOnPrimary,
    primaryContainer = CalyxLightPrimaryContainer,
    onPrimaryContainer = CalyxLightOnPrimaryContainer,
    secondary = CalyxLightSecondary,
    onSecondary = CalyxLightOnSecondary,
    tertiary = CalyxLightTertiary,
    onTertiary = CalyxLightOnTertiary,
    background = CalyxLightBg,
    onBackground = CalyxLightTextPrimary,
    surface = CalyxLightSurface,
    onSurface = CalyxLightTextPrimary,
    surfaceVariant = CalyxLightSurfaceVariant,
    onSurfaceVariant = CalyxLightTextSecondary,
    outline = CalyxLightOutline
)

@Composable
fun CalyxTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CalyxDarkColorScheme else CalyxLightColorScheme

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

