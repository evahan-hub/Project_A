package io.artly.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// A gallery palette: warm paper walls, ink text and a terracotta accent. Only the color scheme below uses it, so that
// a dark mode needs nothing but a dark variant of this scheme.
private val Terracotta = Color(0xFFA9432A)
private val TerracottaDark = Color(0xFF6E2414)
private val Blush = Color(0xFFF6E3DC)
private val Linen = Color(0xFFF0EDE6)
private val Paper = Color(0xFFF7F5F0)
private val Stone = Color(0xFFE7E3DA)
private val Line = Color(0xFFDDD8CD)
private val Ink = Color(0xFF1F1D1A)
private val InkMuted = Color(0xFF5E5A53)
private val Pebble = Color(0xFF948F86)
private val Ochre = Color(0xFFF5E9CC)
private val OchreInk = Color(0xFF6B4A00)
private val Blood = Color(0xFF8E2414)
private val Rose = Color(0xFFF9E1DC)

// Screens are paper, cards and bars on them white.
val LightColorScheme = lightColorScheme(
    primary = Terracotta,
    onPrimary = Color.White,
    primaryContainer = Blush,
    onPrimaryContainer = TerracottaDark,
    secondary = InkMuted,
    onSecondary = Color.White,
    secondaryContainer = Linen,
    onSecondaryContainer = Ink,
    tertiary = OchreInk,
    onTertiary = Color.White,
    tertiaryContainer = Ochre,
    onTertiaryContainer = OchreInk,
    background = Paper,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Linen,
    onSurfaceVariant = InkMuted,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Linen,
    surfaceContainerHighest = Stone,
    outline = Pebble,
    outlineVariant = Line,
    error = Blood,
    onError = Color.White,
    errorContainer = Rose,
    onErrorContainer = Blood,
)
