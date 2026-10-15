package io.artly.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import io.artly.artworks.repository.ArtworkRepository
import io.artly.location.repository.PlaceNameRepository
import io.artly.location.repository.PlaceNameResult

/**
 * Looks up the place names of artworks that have none yet: whenever artworks change, e.g. one is recorded, and
 * whenever the device gets online, as geocoding needs a network connection, which may be missing, e.g. abroad.
 */
class PlaceNameResolver(
    private val artworkRepository: ArtworkRepository,
    private val placeNameRepository: PlaceNameRepository,
    private val connectivity: Connectivity,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun start() {
        scope.launch {
            merge(artworkRepository.getArtworks().map { }, connectivity.networkAvailable)
                // One pass at a time, catching up with all changes meanwhile.
                .conflate()
                .collect { resolvePending() }
        }
    }

    private suspend fun resolvePending() {
        for (artwork in artworkRepository.getArtworksWithoutPlaceName()) {
            val location = artwork.location ?: continue
            when (val result = placeNameRepository.getPlaceName(location)) {
                is PlaceNameResult.Found -> artworkRepository.setPlaceName(artwork.id, location, result.name)
                PlaceNameResult.NoName -> artworkRepository.setPlaceName(artwork.id, location, null)
                // Likely offline, so the others would fail as well. Tried again when online.
                PlaceNameResult.Unavailable -> return
            }
        }
    }
}
