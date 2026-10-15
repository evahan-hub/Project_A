package io.artly.ui.screen.artworks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import io.artly.artworks.repository.ArtworkRepository
import io.artly.data.Notices

class ArtworkListViewModel(
    artworkRepository: ArtworkRepository,
    private val notices: Notices,
) : ViewModel() {

    private val _sortOption = MutableStateFlow(ArtworkSortOption.DATE_DESCENDING)

    val uiState: StateFlow<ArtworkListScreenState> = combine(
        artworkRepository.getArtworks(),
        _sortOption,
        notices.placeNamesDismissed,
    ) { artworks, sortOption, placeNamesDismissed ->
        ArtworkListScreenState(
            artworks = when (sortOption) {
                ArtworkSortOption.DATE_DESCENDING -> artworks.sortedByDescending { it.dateTime }
                ArtworkSortOption.DATE_ASCENDING -> artworks.sortedBy { it.dateTime }
                // Untitled ones last.
                ArtworkSortOption.TITLE -> artworks.sortedWith(
                    compareBy(nullsLast(String.CASE_INSENSITIVE_ORDER)) { it.title }
                )
            },
            sortOption = sortOption,
            // Once the first names were looked up.
            showPlaceNamesNotice = !placeNamesDismissed && artworks.any { it.placeName != null },
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ArtworkListScreenState()
    )

    fun onDismissPlaceNamesNotice() {
        notices.dismissPlaceNames()
    }

    fun onSortOptionSelected(sortOption: ArtworkSortOption) {
        _sortOption.value = sortOption
    }
}
