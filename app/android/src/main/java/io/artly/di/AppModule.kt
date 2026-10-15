package io.artly.di

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import io.artly.data.AndroidConnectivity
import io.artly.data.Connectivity
import io.artly.data.Notices
import io.artly.artworks.createArtworkRepository
import io.artly.artworks.picture.AndroidPictureStorage
import io.artly.artworks.picture.PictureStorage
import io.artly.artworks.repository.ArtworkRepository
import io.artly.location.data.FusedLocationRepository
import io.artly.location.data.GeocoderPlaceNameRepository
import io.artly.location.repository.LocationRepository
import io.artly.location.repository.PlaceNameRepository
import io.artly.ui.di.uiModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * The dependencies of the Android app, along with the shared [uiModule]. Singletons, as they hold state or open
 * resources like the database.
 */
val appModule = module {
    includes(uiModule)

    // Artworks
    single<ArtworkRepository> { createArtworkRepository(androidContext()) }
    singleOf(::AndroidPictureStorage) bind PictureStorage::class
    singleOf(::AndroidConnectivity) bind Connectivity::class

    // Location
    singleOf(::FusedLocationRepository) bind LocationRepository::class
    singleOf(::GeocoderPlaceNameRepository) bind PlaceNameRepository::class

    // App state
    single { Notices(settings("notices")) }
}

/** Settings in shared preferences of their own, with the given name. */
private fun Scope.settings(name: String): Settings {
    return SharedPreferencesSettings(androidContext().getSharedPreferences(name, Context.MODE_PRIVATE))
}
