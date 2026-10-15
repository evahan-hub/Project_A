package io.artly.ui.screen.entry

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.Clock
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import io.artly.artworks.model.Artwork
import io.artly.artworks.picture.PhotoMetadata
import io.artly.artworks.picture.PictureStorage
import io.artly.artworks.repository.ArtworkRepository
import io.artly.location.model.LocationStatus
import io.artly.location.repository.LocationRepository

class SaveArtworkViewModel(
    private val artworkRepository: ArtworkRepository,
    private val pictureStorage: PictureStorage,
    locationRepository: LocationRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    /** ID of the artwork to edit, or null to record a new one. */
    private val editedArtworkId: Int? = savedStateHandle["artworkId"]

    /** File name of a photo shared from another app, to record an artwork of, or null. */
    private val sharedPhotoFile: String? = savedStateHandle["photoFile"]

    val isEditing: Boolean
        get() = editedArtworkId != null

    // The artwork being edited, once loaded.
    private var editedArtwork: Artwork? = null

    val locationStatus: StateFlow<LocationStatus> = locationRepository.locationStatus
        .stateIn(
            viewModelScope,
            started = SharingStarted.Eagerly,
            locationRepository.locationStatus.value
        )

    private val _uiState = MutableStateFlow(SaveArtworkScreenState(isLoading = editedArtworkId != null))
    val uiState: StateFlow<SaveArtworkScreenState> = combine(_uiState, locationStatus) { state, locationStatus ->
        // The location follows the device, unless it was set otherwise.
        if (state.locationSource == LocationSource.DEVICE) {
            state.copy(location = (locationStatus as? LocationStatus.Available)?.coordinates)
        } else {
            state
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = _uiState.value,
    )

    // The text of the form, updated synchronously, as text fields lose keystrokes otherwise.
    var title by mutableStateOf("")
        private set
    var artist by mutableStateOf("")
        private set
    var note by mutableStateOf("")
        private set

    // A new picture, which must be deleted if it doesn't end up in an artwork.
    private var pictureFile: String? = null

    init {
        sharedPhotoFile?.let { file ->
            // A new picture of this artwork, deleted if it isn't saved.
            pictureFile = file
            // The art was most likely seen elsewhere, so the location of the device would be wrong.
            _uiState.value = _uiState.value.copy(
                imagePath = pictureStorage.path(file),
                location = null,
                locationSource = LocationSource.NONE,
            )
            viewModelScope.launch {
                pictureStorage.readMetadata(file)?.let { metadata -> update { it.withPhotoMetadata(metadata) } }
            }
        }
        editedArtworkId?.let { id -> viewModelScope.launch { loadArtwork(id) } }
    }

    private suspend fun loadArtwork(id: Int) {
        val artwork = artworkRepository.getArtwork(id).first() ?: return
        editedArtwork = artwork
        title = artwork.title.orEmpty()
        artist = artwork.artist.orEmpty()
        note = artwork.note.orEmpty()
        _uiState.value = SaveArtworkScreenState(
            imagePath = artwork.pictureFile?.let(pictureStorage::path),
            location = artwork.location,
            locationSource = if (artwork.location != null) LocationSource.SAVED else LocationSource.NONE,
            dateTime = artwork.dateTime,
            timeZone = artwork.timeZone,
        )
    }

    fun onTitleChanged(title: String) {
        this.title = title
    }

    fun onArtistChanged(artist: String) {
        this.artist = artist
    }

    fun onNoteChanged(note: String) {
        this.note = note
    }

    /**
     * Sets when the art was seen. It's assumed to be in the time zone of the device, e.g. when recording a visit of
     * today.
     */
    fun onDateTimePicked(dateTime: LocalDateTime) {
        update { it.copy(dateTime = dateTime, timeZone = TimeZone.currentSystemDefault()) }
    }

    /** Uses the location of the device, which follows it until saved. */
    fun onUseDeviceLocation() = update { it.copy(locationSource = LocationSource.DEVICE) }

    fun onRemoveLocation() = update { it.copy(location = null, locationSource = LocationSource.NONE) }

    /**
     * Uses when and where the photo was taken for the artwork, as far as the photo tells.
     */
    fun onUsePhotoMetadata() {
        update { state ->
            val metadata = state.photoMetadata ?: return@update state
            state.copy(photoMetadata = null).withPhotoMetadata(metadata)
        }
    }

    private fun SaveArtworkScreenState.withPhotoMetadata(metadata: PhotoMetadata): SaveArtworkScreenState {
        var updated = this
        metadata.dateTime?.let {
            // Without an offset in the photo, it was most likely taken in the time zone of the device.
            updated = updated.copy(dateTime = it, timeZone = metadata.timeZone ?: TimeZone.currentSystemDefault())
        }
        metadata.location?.let { updated = updated.copy(location = it, locationSource = LocationSource.PHOTO) }
        return updated
    }

    fun onDismissPhotoMetadata() = update { it.copy(photoMetadata = null) }

    private fun update(update: (SaveArtworkScreenState) -> SaveArtworkScreenState) {
        _uiState.value = update(_uiState.value)
    }

    fun onPhotoTaken(fileName: String) {
        // Taken just now, here, so it tells nothing new.
        replacePicture(fileName, metadata = null)
    }

    fun onPhotoPicked(fileName: String) {
        viewModelScope.launch { replacePicture(fileName, pictureStorage.readMetadata(fileName)) }
    }

    private fun replacePicture(fileName: String, metadata: PhotoMetadata?) {
        // The picture of an edited artwork is only replaced when saving.
        discardPicture()
        pictureFile = fileName
        update { it.copy(imagePath = pictureStorage.path(fileName), photoMetadata = metadata) }
    }

    private fun discardPicture() {
        pictureFile?.let { deleteInBackground(it) }
        pictureFile = null
    }

    fun saveArtwork() {
        val currentState = uiState.value
        if (currentState.isLoading || currentState.isSaving) return
        val editedArtwork = editedArtwork

        _uiState.value = _uiState.value.copy(isSaving = true)
        viewModelScope.launch {
            val newPictureFile = pictureFile
            val location = currentState.location
            val artwork = if (editedArtwork != null) {
                editedArtwork.copy(
                    title = title.trim().ifEmpty { null },
                    artist = artist.trim().ifEmpty { null },
                    location = location,
                    // Looked up again for a new location.
                    placeName = editedArtwork.placeName.takeIf { location == editedArtwork.location },
                    dateTime = currentState.dateTime ?: editedArtwork.dateTime,
                    timeZone = currentState.timeZone,
                    note = note.trim().ifEmpty { null },
                    pictureFile = newPictureFile ?: editedArtwork.pictureFile,
                )
            } else {
                val timeZone = currentState.timeZone ?: TimeZone.currentSystemDefault()
                Artwork(
                    title = title.trim().ifEmpty { null },
                    artist = artist.trim().ifEmpty { null },
                    location = location,
                    dateTime = currentState.dateTime ?: Clock.System.now().toLocalDateTime(timeZone),
                    timeZone = timeZone,
                    note = note.trim().ifEmpty { null },
                    pictureFile = newPictureFile,
                )
            }
            artworkRepository.saveArtwork(artwork)
            pictureFile = null
            // The replaced picture is no longer referenced. Deleted after saving, so that it's never lost before.
            val replacedPictureFile = editedArtwork?.pictureFile
            if (newPictureFile != null && replacedPictureFile != null) {
                withContext(NonCancellable) { pictureStorage.delete(replacedPictureFile) }
            }
            _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
        }
    }

    override fun onCleared() {
        // The screen was left without saving, so the new picture is not referenced anywhere.
        discardPicture()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun deleteInBackground(fileName: String) {
        GlobalScope.launch { pictureStorage.delete(fileName) }
    }
}
