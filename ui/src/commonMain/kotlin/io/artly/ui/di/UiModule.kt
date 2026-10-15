package io.artly.ui.di

import io.artly.data.PlaceNameResolver
import io.artly.ui.screen.artworks.ArtworkDetailViewModel
import io.artly.ui.screen.artworks.ArtworkListViewModel
import io.artly.ui.screen.entry.SaveArtworkViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * The dependencies shared by all platforms: the place names and the screens. Each platform adds its own module with
 * the artworks, the location services, the connectivity, the picture storage and the settings.
 */
val uiModule = module {
    singleOf(::PlaceNameResolver)

    viewModelOf(::ArtworkListViewModel)
    viewModelOf(::ArtworkDetailViewModel)
    viewModelOf(::SaveArtworkViewModel)
}
