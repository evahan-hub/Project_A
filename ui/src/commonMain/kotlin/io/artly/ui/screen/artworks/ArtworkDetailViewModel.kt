package io.artly.ui.screen.artworks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.artly.artworks.model.Artwork
import io.artly.artworks.picture.PictureStorage
import io.artly.artworks.repository.ArtworkRepository

class ArtworkDetailViewModel(
    private val artworkRepository: ArtworkRepository,
    private val pictureStorage: PictureStorage,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val artworkId: Int = checkNotNull(savedStateHandle["artworkId"])

    private val isDeleted = MutableStateFlow(false)

    val uiState: StateFlow<ArtworkDetailUiState> = combine(
        artworkRepository.getArtwork(artworkId),
        isDeleted,
    ) { artwork, isDeleted ->
        when {
            isDeleted -> ArtworkDetailUiState.Deleted
            artwork != null -> ArtworkDetailUiState.Success(artwork)
            else -> ArtworkDetailUiState.NotFound
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ArtworkDetailUiState.Loading,
    )

    fun deleteArtwork() {
        val artwork = (uiState.value as? ArtworkDetailUiState.Success)?.artwork ?: return
        isDeleted.value = true
        viewModelScope.launch {
            // The screen is closed right away, so the deletion must outlive the view model.
            withContext(NonCancellable) {
                artworkRepository.deleteArtwork(artwork)
                artwork.pictureFile?.let { pictureStorage.delete(it) }
            }
        }
    }
}

sealed interface ArtworkDetailUiState {
    object Loading : ArtworkDetailUiState
    data class Success(val artwork: Artwork) : ArtworkDetailUiState
    object NotFound : ArtworkDetailUiState
    object Deleted : ArtworkDetailUiState
}
