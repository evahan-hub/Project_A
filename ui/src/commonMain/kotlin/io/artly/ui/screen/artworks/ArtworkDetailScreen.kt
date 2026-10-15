package io.artly.ui.screen.artworks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import io.artly.artworks.model.Artwork
import io.artly.location.model.Coordinates
import io.artly.ui.component.ArtworkPicture
import io.artly.ui.component.DetailLayout
import io.artly.ui.component.PhotoButton
import io.artly.ui.component.SectionTitle
import io.artly.ui.platform.rememberMapsAppOpener
import io.artly.ui.resources.Res
import io.artly.ui.resources.cancel
import io.artly.ui.resources.delete
import io.artly.ui.resources.delete_artwork_message
import io.artly.ui.resources.delete_artwork_title
import io.artly.ui.resources.edit
import io.artly.ui.resources.error_artwork_not_found
import io.artly.ui.resources.location_label
import io.artly.ui.resources.note_title
import io.artly.ui.resources.open_in_maps_app
import io.artly.ui.resources.seen_label
import io.artly.ui.theme.ArtlyTheme
import io.artly.ui.util.displayTitle
import io.artly.ui.util.format
import io.artly.ui.util.formatDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ArtworkDetailRoute(
    onBackClick: () -> Unit,
    onEditClick: (Artwork) -> Unit,
) {
    val viewModel: ArtworkDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState is ArtworkDetailUiState.Deleted) {
        if (uiState is ArtworkDetailUiState.Deleted) onBackClick()
    }

    ArtworkDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onDeleteClick = viewModel::deleteArtwork,
        onEditClick = onEditClick,
    )
}

@Composable
fun ArtworkDetailScreen(
    uiState: ArtworkDetailUiState,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: (Artwork) -> Unit,
) {
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    when (uiState) {
        ArtworkDetailUiState.Loading,
        ArtworkDetailUiState.Deleted -> Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        ArtworkDetailUiState.NotFound -> Text(
            text = stringResource(Res.string.error_artwork_not_found),
            modifier = Modifier.padding(16.dp)
        )

        is ArtworkDetailUiState.Success -> ArtworkDetailContent(
            artwork = uiState.artwork,
            onBackClick = onBackClick,
            onDeleteClick = { showDeleteDialog = true },
            onEditClick = { onEditClick(uiState.artwork) },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(Res.string.delete_artwork_title)) },
            text = { Text(stringResource(Res.string.delete_artwork_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    }
                ) {
                    Text(
                        text = stringResource(Res.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun ArtworkDetailContent(
    artwork: Artwork,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
) {
    DetailLayout(
        image = { modifier ->
            val pictureFile = artwork.pictureFile
            if (pictureFile != null) {
                // The whole artwork, uncropped.
                ArtworkPicture(fileName = pictureFile, modifier = modifier, contentScale = ContentScale.FillWidth)
            } else {
                Box(
                    modifier = modifier
                        .aspectRatio(4f / 3f)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(56.dp),
                    )
                }
            }
        },
        onBackClick = onBackClick,
        actions = {
            PhotoButton(onClick = onEditClick) {
                Icon(Icons.Outlined.Edit, contentDescription = stringResource(Res.string.edit))
            }
            PhotoButton(onClick = onDeleteClick) {
                Icon(Icons.Outlined.Delete, contentDescription = stringResource(Res.string.delete))
            }
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = artwork.displayTitle(), style = MaterialTheme.typography.headlineMedium)
            artwork.artist?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        InfoTile(
            label = stringResource(Res.string.seen_label),
            value = formatDateTime(artwork.dateTime, LocalLocale.current),
            modifier = Modifier.fillMaxWidth(),
        )

        artwork.location?.let { LocationTile(artwork = artwork, location = it) }

        artwork.note?.takeIf { it.isNotBlank() }?.let {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionTitle(stringResource(Res.string.note_title))
                Text(text = it, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

/**
 * Where the art was seen, with the button to open it in a maps app, e.g. to go there again.
 */
@Composable
private fun LocationTile(artwork: Artwork, location: Coordinates) {
    val openInMapsApp = rememberMapsAppOpener()
    val label = artwork.displayTitle()
    val coordinates = location.format()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(modifier = Modifier.padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                SectionTitle(stringResource(Res.string.location_label))
                Text(text = artwork.placeName ?: coordinates, style = MaterialTheme.typography.titleSmall)
                if (artwork.placeName != null) {
                    Text(
                        text = coordinates,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = { openInMapsApp(location, label) }) {
                Icon(
                    Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = stringResource(Res.string.open_in_maps_app),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun InfoTile(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SectionTitle(label)
            Text(text = value, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Preview
@Composable
private fun PreviewArtworkDetailScreen() {
    ArtlyTheme {
        ArtworkDetailScreen(
            uiState = ArtworkDetailUiState.Success(
                Artwork(
                    title = "The Milkmaid",
                    artist = "Johannes Vermeer",
                    location = Coordinates(52.36, 4.8852),
                    dateTime = LocalDateTime(2026, 10, 9, 14, 3),
                    timeZone = TimeZone.of("Europe/Amsterdam"),
                    note = "Gallery of Honour, second floor",
                    placeName = "Amsterdam, North Holland",
                )
            ),
            onBackClick = { },
            onDeleteClick = { },
            onEditClick = { },
        )
    }
}
