package com.example.bookclub.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// The rest of the app (still-XML screens, colors.xml) is light-only with no
// values-night resources, so the Compose screens intentionally don't follow system
// dark theme or Android 12+ dynamic color - both would drift from the app's brand.
private val BookClubColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = BrandOnPrimary,
    secondary = BrandSecondary,
    tertiary = BrandAccent,
    error = BrandError,
    surface = BrandSurface,
    onSurface = BrandOnSurface,
    background = BrandSurface,
    onBackground = BrandOnSurface
)

@Composable
fun BookClubTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BookClubColorScheme,
        typography = Typography,
        content = content
    )
}
