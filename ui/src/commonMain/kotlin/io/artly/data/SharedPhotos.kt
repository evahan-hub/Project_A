package io.artly.data

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Photos shared with the app from other apps, e.g. the photo library, to record an artwork of each. The platform stores them
 * in the picture storage, the main screen opens them.
 */
class SharedPhotos {
    // Kept until the main screen takes them, e.g. if a photo is shared while the app starts.
    private val channel = Channel<String>(Channel.UNLIMITED)

    /** The file names of the shared photos, each received once. */
    val photos: Flow<String> = channel.receiveAsFlow()

    fun add(fileName: String) {
        channel.trySend(fileName)
    }
}
