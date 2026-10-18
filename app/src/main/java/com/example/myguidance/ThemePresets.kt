package com.example.myguidance

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private fun light(
    primary: Color,
    container: Color,
    onContainer: Color,
    bg: Color,
    onPrimary: Color = Color.White
) =
    lightColorScheme(
        onErrorContainer = Color(0xFFF87A7A),
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = container,
        onPrimaryContainer = onContainer,
        secondaryContainer = container,
        onSecondaryContainer = onContainer,
        background = bg,
        surface = Color.White,
        onBackground = Color(0xFF1B1B1F),
        onSurface = Color(0xFF1B1B1F),
        surfaceVariant = Color(0xFFF3F3F3),
        onSurfaceVariant = Color(0xFF5F6368),
        outlineVariant = Color(0xFFE6E6E6),
        outline = Color(0xFF9AA0A6),
        surfaceTint = Color.Transparent,
        secondary = primary,
        onSecondary = onPrimary,
        tertiary = primary,
        onTertiary = onPrimary,
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = Color(0xFFF8F8F8),
        surfaceContainer = Color(0xFFF3F3F3),
        surfaceContainerHigh = Color(0xFFEEEEEE),
        surfaceContainerHighest = Color(0xFFE8E8E8),
        surfaceDim = Color(0xFFDDDDDD),
        surfaceBright = Color.White
    )

private fun darkOf(primary: Color, container: Color, onContainer: Color) =
    darkColorScheme(
        onErrorContainer = Color(0xFFF7DCDB),
        primary = primary,
        onPrimary = Color(0xFF101418),
        primaryContainer = container,
        onPrimaryContainer = onContainer,
        secondaryContainer = container,
        onSecondaryContainer = onContainer,
        background = Color(0xFF0F1115),
        surface = Color(0xFF1A1D23),
        onBackground = Color(0xFFE6E6E6),
        onSurface = Color(0xFFE6E6E6),
        surfaceVariant = Color(0xFF2A2E36),
        onSurfaceVariant = Color(0xFFB0B5BD),
        outlineVariant = Color(0xFF3A3F47),
        outline = Color(0xFF6B7280),
        surfaceTint = Color.Transparent,
        secondary = primary,
        onSecondary = Color(0xFF101418),
        tertiary = primary,
        onTertiary = Color(0xFF101418),
        surfaceContainerLowest = Color(0xFF0B0D10),
        surfaceContainerLow = Color(0xFF15181D),
        surfaceContainer = Color(0xFF1E2229),
        surfaceContainerHigh = Color(0xFF262B33),
        surfaceContainerHighest = Color(0xFF2F353E),
        surfaceDim = Color(0xFF0F1115),
        surfaceBright = Color(0xFF343A44)
    )

enum class ThemePreset(val label: String, val preview: Color, val scheme: ColorScheme) {
    CRIMSON("Crimson", Color(0xFFD32F2F),
        light(Color(0xFFD32F2F), Color(0xFFFFEBEE), Color(0xFFB71C1C), Color(0xFFF8F9FA))),
    OCEAN("Ocean", Color(0xFF1565C0),
        light(Color(0xFF1565C0), Color(0xFFFDFDFD), Color(0xFF0D47A1), Color(0xFFFFFFFF))),
    FOREST("Forest", Color(0xFF2E7D32),
        light(Color(0xFF2E7D32), Color(0xFFE8F5E9), Color(0xFF1B5E20), Color(0xFFF6FAF6))),
    MIDNIGHT("Midnight", Color(0xFF1E3A5F),
        darkOf(Color(0xFF90CAF9), Color(0xFF1E3A5F), Color(0xFFD6E8FF))),
    // Matches the login screen: black buttons on white cards with light gray details
    CLASSIC("Classic", Color(0xFF424242),
        light(Color.Black, Color(0xFFEEEEEE), Color.Black, Color(0xFFF8F9FA))),
    // Sky blue 48CAE4, with dark navy text on buttons because the blue is light
    SKY("Sky", Color(0xFF48CAE4),
        light(Color(0xFF48CAE4), Color(0xFFD9F4FA), Color(0xFF03506F), Color(0xFFF2FBFD),
            onPrimary = Color(0xFF023047)))
}

/** Returns the light or dark version of a preset. Midnight is already dark, so it never changes. */
fun ThemePreset.schemeFor(dark: Boolean): ColorScheme =
    if (!dark) scheme else when (this) {
        ThemePreset.CRIMSON -> darkOf(Color(0xFFEF9A9A), Color(0xFF5C1A1A), Color(0xFFFFDAD6))
        ThemePreset.OCEAN -> darkOf(Color(0xFF90CAF9), Color(0xFF123A63), Color(0xFFD6E8FF))
        ThemePreset.FOREST -> darkOf(Color(0xFFA5D6A7), Color(0xFF1B4D1F), Color(0xFFD7F0D8))
        ThemePreset.MIDNIGHT -> scheme
        ThemePreset.CLASSIC -> darkOf(Color.White, Color(0xFF2A2E36), Color.White)
        ThemePreset.SKY -> darkOf(Color(0xFF48CAE4), Color(0xFF0B3C4D), Color(0xFFCAF0F8))
    }

/** Separate colour set used only for buttons and schedule screens. */
enum class AccentPreset(val label: String, val preview: Color) {
    MATCH("Match", Color(0xFF9E9E9E)),
    ORANGE("Orange", Color(0xFFF57C00)),
    PURPLE("Purple", Color(0xFF7B1FA2)),
    TEAL("Teal", Color(0xFF00897B)),
    GOLD("Gold", Color(0xFFFFB300))
}

private fun ColorScheme.withAccent(
    main: Color, onMain: Color, container: Color, onContainer: Color
) = copy(
    primary = main,
    onPrimary = onMain,
    primaryContainer = container,
    onPrimaryContainer = onContainer
)

/** Returns [base] with its main colour family swapped for this accent. MATCH changes nothing. */
fun AccentPreset.applyTo(base: ColorScheme, dark: Boolean): ColorScheme = when (this) {
    AccentPreset.MATCH -> base
    AccentPreset.ORANGE ->
        if (dark) base.withAccent(Color(0xFFFFB74D), Color(0xFF2B1700), Color(0xFF5A3300), Color(0xFFFFDDB8))
        else base.withAccent(Color(0xFFF57C00), Color.White, Color(0xFFFFE8D1), Color(0xFF7A3B00))
    AccentPreset.PURPLE ->
        if (dark) base.withAccent(Color(0xFFCE93D8), Color(0xFF2A0A38), Color(0xFF4A1A63), Color(0xFFF3D9FF))
        else base.withAccent(Color(0xFF7B1FA2), Color.White, Color(0xFFF3E1FA), Color(0xFF4A0E63))
    AccentPreset.TEAL ->
        if (dark) base.withAccent(Color(0xFF4DB6AC), Color(0xFF00201D), Color(0xFF00504A), Color(0xFFBDF2EC))
        else base.withAccent(Color(0xFF00897B), Color.White, Color(0xFFD5F3EF), Color(0xFF00504A))
    AccentPreset.GOLD ->
        if (dark) base.withAccent(Color(0xFFFFCA28), Color(0xFF2B1F00), Color(0xFF5C4300), Color(0xFFFFE9A8))
        else base.withAccent(Color(0xFFFFB300), Color(0xFF2B1F00), Color(0xFFFFF1CC), Color(0xFF5C4300))
}