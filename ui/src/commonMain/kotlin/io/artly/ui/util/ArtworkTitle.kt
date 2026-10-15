package io.artly.ui.util

import androidx.compose.runtime.Composable
import io.artly.artworks.model.Artwork
import io.artly.ui.resources.Res
import io.artly.ui.resources.untitled
import org.jetbrains.compose.resources.stringResource

/** The title of the artwork, or "Untitled" if it has none. */
@Composable
fun Artwork.displayTitle(): String = title ?: stringResource(Res.string.untitled)
