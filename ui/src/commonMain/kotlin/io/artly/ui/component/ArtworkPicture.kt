package io.artly.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import io.artly.artworks.picture.PictureStorage
import org.koin.compose.koinInject

/**
 * Shows the user's own picture of an artwork, stored under [fileName].
 */
@Composable
fun ArtworkPicture(
    fileName: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val pictureStorage = koinInject<PictureStorage>()
    AsyncImage(
        model = pictureStorage.path(fileName),
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale,
    )
}
