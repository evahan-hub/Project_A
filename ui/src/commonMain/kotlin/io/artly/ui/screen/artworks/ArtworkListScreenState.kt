package io.artly.ui.screen.artworks

import io.artly.artworks.model.Artwork

data class ArtworkListScreenState(
    val artworks: List<Artwork> = emptyList(),
    val sortOption: ArtworkSortOption = ArtworkSortOption.DATE_DESCENDING,
    /** Whether to tell, once, that place names are looked up online. */
    val showPlaceNamesNotice: Boolean = false,
    val isLoading: Boolean = true,
)

enum class ArtworkSortOption {
    DATE_DESCENDING,
    DATE_ASCENDING,
    TITLE,
}
