package io.artly.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val ArtlyShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/**
 * The theme of the app. It's light only for now. A dark mode needs a dark counterpart of [LightColorScheme], chosen
 * here, e.g. by isSystemInDarkTheme(), and system bar icons to match in ArtlyActivity.
 */
@Composable
fun ArtlyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = artlyTypography(),
        shapes = ArtlyShapes,
        content = content,
    )
}
