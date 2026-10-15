package io.artly.ui.screen.artworks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.artly.artworks.model.Artwork
import io.artly.ui.component.ArtworkItem
import io.artly.ui.component.PillChip
import io.artly.ui.component.SectionTitle
import io.artly.ui.platform.geocoderName
import io.artly.ui.resources.Res
import io.artly.ui.resources.app_name
import io.artly.ui.resources.artworks_count
import io.artly.ui.resources.empty_artworks_message
import io.artly.ui.resources.empty_artworks_title
import io.artly.ui.resources.got_it_action
import io.artly.ui.resources.place_names_notice
import io.artly.ui.resources.sort_name
import io.artly.ui.resources.sort_newest
import io.artly.ui.resources.sort_oldest
import io.artly.ui.util.formatMonth
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ArtworkListRoute(
    onArtworkClick: (Artwork) -> Unit,
) {
    val viewModel: ArtworkListViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ArtworkListScreen(
        uiState = uiState,
        onSortOptionSelected = viewModel::onSortOptionSelected,
        onDismissPlaceNamesNotice = viewModel::onDismissPlaceNamesNotice,
        onArtworkClick = onArtworkClick,
    )
}

@Composable
fun ArtworkListScreen(
    uiState: ArtworkListScreenState,
    onSortOptionSelected: (ArtworkSortOption) -> Unit,
    onDismissPlaceNamesNotice: () -> Unit,
    onArtworkClick: (Artwork) -> Unit,
) {
    val locale = LocalLocale.current
    // Sorted by date, artworks are listed by month.
    val sections = if (uiState.sortOption == ArtworkSortOption.TITLE) {
        listOf(null to uiState.artworks)
    } else {
        uiState.artworks.groupBy { formatMonth(it.dateTime, locale) }.toList()
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            fullWidthItem {
                Text(
                    text = stringResource(Res.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(top = 24.dp, bottom = 6.dp),
                )
            }
            if (!uiState.isLoading && uiState.artworks.isEmpty()) {
                fullWidthItem { EmptyState() }
                return@LazyVerticalGrid
            }
            if (uiState.showPlaceNamesNotice) {
                fullWidthItem { PlaceNamesNotice(onDismiss = onDismissPlaceNamesNotice) }
            }
            if (uiState.artworks.isNotEmpty()) {
                fullWidthItem {
                    SortSelector(selectedOption = uiState.sortOption, onOptionSelected = onSortOptionSelected)
                }
            }
            sections.forEach { (month, artworks) ->
                if (month != null) {
                    fullWidthItem(key = month) {
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 4.dp, end = 4.dp)) {
                            SectionTitle(text = month, modifier = Modifier.weight(1f))
                            Text(
                                text = pluralStringResource(Res.plurals.artworks_count, artworks.size, artworks.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                items(artworks, key = { it.id }) { artwork ->
                    ArtworkItem(artwork = artwork, onClick = { onArtworkClick(artwork) })
                }
            }
        }
    }
}

private fun LazyGridScope.fullWidthItem(key: Any? = null, content: @Composable () -> Unit) {
    item(key = key, span = { GridItemSpan(maxLineSpan) }) { content() }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 96.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.Outlined.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp),
            )
        }
        Text(text = stringResource(Res.string.empty_artworks_title), style = MaterialTheme.typography.titleLarge)
        Text(
            text = stringResource(Res.string.empty_artworks_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SortSelector(
    selectedOption: ArtworkSortOption,
    onOptionSelected: (ArtworkSortOption) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ArtworkSortOption.entries.forEach { option ->
            PillChip(
                label = stringResource(
                    when (option) {
                        ArtworkSortOption.DATE_DESCENDING -> Res.string.sort_newest
                        ArtworkSortOption.DATE_ASCENDING -> Res.string.sort_oldest
                        ArtworkSortOption.TITLE -> Res.string.sort_name
                    }
                ),
                selected = selectedOption == option,
                onClick = { onOptionSelected(option) },
            )
        }
    }
}

/**
 * Tells once that place names are looked up online from the coordinates.
 */
@Composable
private fun PlaceNamesNotice(onDismiss: () -> Unit) {
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, top = 14.dp, end = 14.dp, bottom = 4.dp),
        ) {
            Text(
                text = stringResource(Res.string.place_names_notice, geocoderName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(Res.string.got_it_action))
            }
        }
    }
}
