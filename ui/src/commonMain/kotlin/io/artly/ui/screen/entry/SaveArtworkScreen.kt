package io.artly.ui.screen.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kotlin.time.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import io.artly.artworks.picture.PhotoMetadata
import io.artly.location.model.Coordinates
import io.artly.ui.component.SectionTitle
import io.artly.ui.platform.LocationPermissionEffect
import io.artly.ui.platform.rememberPhotoPicker
import io.artly.ui.resources.Res
import io.artly.ui.resources.artist_label
import io.artly.ui.resources.artist_placeholder
import io.artly.ui.resources.artwork_photo_desc
import io.artly.ui.resources.browse_action
import io.artly.ui.resources.camera_action
import io.artly.ui.resources.cancel
import io.artly.ui.resources.change_action
import io.artly.ui.resources.edit_artwork
import io.artly.ui.resources.ignore_action
import io.artly.ui.resources.location_from_device
import io.artly.ui.resources.location_from_photo
import io.artly.ui.resources.location_label
import io.artly.ui.resources.location_none
import io.artly.ui.resources.new_artwork
import io.artly.ui.resources.note_optional_title
import io.artly.ui.resources.note_placeholder
import io.artly.ui.resources.now
import io.artly.ui.resources.photo_metadata_time
import io.artly.ui.resources.photo_metadata_time_and_place
import io.artly.ui.resources.remove_action
import io.artly.ui.resources.retake_action
import io.artly.ui.resources.save_artwork_action
import io.artly.ui.resources.save_changes_action
import io.artly.ui.resources.title_label
import io.artly.ui.resources.title_placeholder
import io.artly.ui.resources.use_action
import io.artly.ui.resources.use_my_location_action
import io.artly.ui.resources.waiting_for_location
import io.artly.ui.resources.when_title
import io.artly.ui.theme.ArtlyTheme
import io.artly.ui.util.format
import io.artly.ui.util.formatDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SaveArtworkScreenRoute(
    onDismiss: () -> Unit,
) {
    val viewModel: SaveArtworkViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val locationStatus by viewModel.locationStatus.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onDismiss()
    }
    // Only while the location of the device is used, e.g. not for a shared photo.
    if (uiState.locationSource == LocationSource.DEVICE) LocationPermissionEffect(locationStatus)

    SaveArtworkScreen(
        uiState = uiState,
        title = viewModel.title,
        artist = viewModel.artist,
        note = viewModel.note,
        isEditing = viewModel.isEditing,
        onPhotoTaken = viewModel::onPhotoTaken,
        onPhotoPicked = viewModel::onPhotoPicked,
        onTitleChanged = viewModel::onTitleChanged,
        onArtistChanged = viewModel::onArtistChanged,
        onNoteChanged = viewModel::onNoteChanged,
        onUseDeviceLocation = viewModel::onUseDeviceLocation,
        onRemoveLocation = viewModel::onRemoveLocation,
        onDateTimePicked = viewModel::onDateTimePicked,
        onUsePhotoMetadata = viewModel::onUsePhotoMetadata,
        onDismissPhotoMetadata = viewModel::onDismissPhotoMetadata,
        onSave = viewModel::saveArtwork,
        onCancel = onDismiss,
    )
}

@Composable
fun SaveArtworkScreen(
    uiState: SaveArtworkScreenState,
    title: String,
    artist: String,
    note: String,
    isEditing: Boolean,
    onPhotoTaken: (fileName: String) -> Unit,
    onPhotoPicked: (fileName: String) -> Unit,
    onTitleChanged: (String) -> Unit,
    onArtistChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onUseDeviceLocation: () -> Unit,
    onRemoveLocation: () -> Unit,
    onDateTimePicked: (LocalDateTime) -> Unit,
    onUsePhotoMetadata: () -> Unit,
    onDismissPhotoMetadata: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    val photoPicker = rememberPhotoPicker(onPhotoTaken = onPhotoTaken, onPhotoPicked = onPhotoPicked)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Header(
                title = stringResource(if (isEditing) Res.string.edit_artwork else Res.string.new_artwork),
                onCancel = onCancel,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PictureSelector(
                    imagePath = uiState.imagePath,
                    onTakePicture = photoPicker::takePhoto,
                    onPickImage = photoPicker::pickFromGallery,
                )
                uiState.photoMetadata?.let {
                    PhotoMetadataSuggestion(
                        metadata = it,
                        onUse = onUsePhotoMetadata,
                        onDismiss = onDismissPhotoMetadata,
                    )
                }
                TextInput(
                    title = stringResource(Res.string.title_label),
                    value = title,
                    placeholder = stringResource(Res.string.title_placeholder),
                    onValueChange = onTitleChanged,
                )
                TextInput(
                    title = stringResource(Res.string.artist_label),
                    value = artist,
                    placeholder = stringResource(Res.string.artist_placeholder),
                    onValueChange = onArtistChanged,
                )
                WhenCard(dateTime = uiState.dateTime, onDateTimePicked = onDateTimePicked)
                LocationCard(
                    location = uiState.location,
                    source = uiState.locationSource,
                    onUseDeviceLocation = onUseDeviceLocation,
                    onRemoveLocation = onRemoveLocation,
                )
                NoteInput(note = note, onNoteChange = onNoteChanged)
            }
            SaveBar(
                label = stringResource(if (isEditing) Res.string.save_changes_action else Res.string.save_artwork_action),
                enabled = !uiState.isLoading && !uiState.isSaving,
                onClick = onSave,
            )
        }
    }
}

@Composable
private fun Header(title: String, onCancel: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        TextButton(onClick = onCancel, modifier = Modifier.align(Alignment.CenterStart)) {
            Text(stringResource(Res.string.cancel), style = MaterialTheme.typography.titleMedium)
        }
        Text(text = title, style = MaterialTheme.typography.titleLarge)
    }
}

/**
 * A white card with a small title, grouping the fields of the artwork.
 */
@Composable
private fun FormSection(title: String?, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            title?.let { SectionTitle(it) }
            content()
        }
    }
}

/**
 * The photo of the artwork, or the buttons to take or pick one.
 */
@Composable
private fun PictureSelector(
    imagePath: String?,
    onTakePicture: () -> Unit,
    onPickImage: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (imagePath != null) {
            AsyncImage(
                model = imagePath,
                contentDescription = stringResource(Res.string.artwork_photo_desc),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PhotoAction(icon = Icons.Outlined.PhotoCamera, label = stringResource(Res.string.retake_action), onClick = onTakePicture)
                PhotoAction(icon = Icons.Outlined.PhotoLibrary, label = stringResource(Res.string.browse_action), onClick = onPickImage)
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onTakePicture) {
                    Icon(Icons.Outlined.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.camera_action))
                }
                FilledTonalButton(
                    onClick = onPickImage,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.browse_action))
                }
            }
        }
    }
}

@Composable
private fun PhotoAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
            contentColor = MaterialTheme.colorScheme.surface,
        ),
        contentPadding = PaddingValues(horizontal = 14.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label)
    }
}

/**
 * Offers to use when and where the photo picked from the gallery was taken, e.g. when recording art afterwards.
 */
@Composable
private fun PhotoMetadataSuggestion(metadata: PhotoMetadata, onUse: () -> Unit, onDismiss: () -> Unit) {
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(
                    if (metadata.location != null) Res.string.photo_metadata_time_and_place else Res.string.photo_metadata_time
                ),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            metadata.dateTime?.let {
                Text(
                    text = formatDateTime(it, LocalLocale.current),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onUse) { Text(stringResource(Res.string.use_action)) }
                TextButton(onClick = onDismiss) { Text(stringResource(Res.string.ignore_action)) }
            }
        }
    }
}

/**
 * A card with an icon, a title, a value and a button to change it.
 */
@Composable
private fun ChangeableCard(
    icon: ImageVector,
    title: String,
    value: String,
    onChangeClick: () -> Unit,
    actionLabel: String = stringResource(Res.string.change_action),
) {
    FormSection(title = null) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FilledTonalButton(
                onClick = onChangeClick,
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Text(actionLabel)
            }
        }
    }
}

/**
 * When the art was seen, now unless picked, e.g. when recording art afterwards.
 */
@Composable
private fun WhenCard(dateTime: LocalDateTime?, onDateTimePicked: (LocalDateTime) -> Unit) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    ChangeableCard(
        icon = Icons.Outlined.Event,
        title = stringResource(Res.string.when_title),
        value = dateTime?.let { formatDateTime(it, LocalLocale.current) }
            ?: stringResource(Res.string.now),
        onChangeClick = { showPicker = true },
    )
    if (showPicker) {
        DateTimePickerDialog(
            initial = dateTime ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            onPicked = {
                showPicker = false
                onDateTimePicked(it)
            },
            onDismiss = { showPicker = false },
        )
    }
}

/**
 * A single line of text, e.g. the title of the artwork.
 */
@Composable
private fun TextInput(title: String, value: String, placeholder: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionTitle(title)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            )
        )
    }
}

/**
 * Where the art was seen: the location of the device, where the photo was taken, or none, e.g. indoors where the
 * device can't tell.
 */
@Composable
private fun LocationCard(
    location: Coordinates?,
    source: LocationSource,
    onUseDeviceLocation: () -> Unit,
    onRemoveLocation: () -> Unit,
) {
    val value = location?.let {
        // Coordinates always use a decimal point, regardless of the locale.
        val coordinates = it.format()
        when (source) {
            LocationSource.DEVICE -> stringResource(Res.string.location_from_device, coordinates)
            LocationSource.PHOTO -> stringResource(Res.string.location_from_photo, coordinates)
            LocationSource.SAVED,
            LocationSource.NONE -> coordinates
        }
    } ?: if (source == LocationSource.DEVICE) {
        stringResource(Res.string.waiting_for_location)
    } else {
        stringResource(Res.string.location_none)
    }
    val hasLocation = location != null || source == LocationSource.DEVICE
    ChangeableCard(
        icon = Icons.Outlined.LocationOn,
        title = stringResource(Res.string.location_label),
        value = value,
        onChangeClick = if (hasLocation) onRemoveLocation else onUseDeviceLocation,
        actionLabel = stringResource(if (hasLocation) Res.string.remove_action else Res.string.use_my_location_action),
    )
}

@Composable
private fun NoteInput(
    note: String,
    onNoteChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionTitle(stringResource(Res.string.note_optional_title))
        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 96.dp),
            placeholder = { Text(stringResource(Res.string.note_placeholder)) },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            )
        )
    }
}

/**
 * The button to save the artwork, below the form.
 */
@Composable
private fun SaveBar(label: String, enabled: Boolean, onClick: () -> Unit) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(52.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(text = label, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Preview
@Composable
private fun PreviewSaveArtworkScreen() {
    ArtlyTheme {
        SaveArtworkScreen(
            uiState = SaveArtworkScreenState(location = Coordinates(52.36, 4.8852)),
            title = "The Milkmaid",
            artist = "Johannes Vermeer",
            note = "",
            isEditing = false,
            onPhotoTaken = { },
            onPhotoPicked = { },
            onTitleChanged = { },
            onArtistChanged = { },
            onNoteChanged = { },
            onUseDeviceLocation = { },
            onRemoveLocation = { },
            onDateTimePicked = { },
            onUsePhotoMetadata = { },
            onDismissPhotoMetadata = { },
            onSave = { },
            onCancel = { },
        )
    }
}
