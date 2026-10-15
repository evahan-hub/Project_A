package io.artly.ui.di

import com.russhwolf.settings.NSUserDefaultsSettings
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.setUnhandledExceptionHook
import kotlin.native.terminateWithUnhandledException
import io.artly.data.Connectivity
import io.artly.data.IosConnectivity
import io.artly.data.Notices
import io.artly.data.PlaceNameResolver
import io.artly.data.SharedPhotos
import io.artly.artworks.createArtworkRepository
import io.artly.artworks.picture.IosPictureStorage
import io.artly.artworks.picture.PictureStorage
import io.artly.artworks.repository.ArtworkRepository
import io.artly.location.data.CoreLocationRepository
import io.artly.location.data.GeocoderPlaceNameRepository
import io.artly.location.repository.LocationRepository
import io.artly.location.repository.PlaceNameRepository
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import platform.Foundation.NSLog
import platform.Foundation.NSUserDefaults

/**
 * The dependencies of the iOS app, along with the shared [uiModule]. Singletons, as they hold state or open resources
 * like the database.
 */
val iosModule = module {
    includes(uiModule)

    // Artworks
    single<ArtworkRepository> { createArtworkRepository() }
    // Created explicitly, as singleOf would also try to inject the directory, which has a default.
    single { IosPictureStorage() } bind PictureStorage::class
    singleOf(::IosConnectivity) bind Connectivity::class
    singleOf(::SharedPhotos)

    // Location
    singleOf(::CoreLocationRepository) bind LocationRepository::class
    singleOf(::GeocoderPlaceNameRepository) bind PlaceNameRepository::class

    // App state
    single { Notices(NSUserDefaultsSettings(NSUserDefaults(suiteName = "notices"))) }
}

/**
 * Starts the dependency injection and the background work of the app. Called once, when the app starts.
 */
@OptIn(ExperimentalNativeApi::class)
fun startApp() {
    // Uncaught exceptions only go to the standard error, which the system log of the device doesn't keep.
    setUnhandledExceptionHook { exception ->
        // The system log cuts long messages, so the causes come first. Kotlin strings can't be passed to the format
        // of NSLog, so the message is the format, escaped.
        val causes = generateSequence(exception) { it.cause }.joinToString(" <- ")
        NSLog("Uncaught exception: $causes".replace("%", "%%"))
        NSLog(exception.stackTraceToString().replace("%", "%%"))
        terminateWithUnhandledException(exception)
    }
    val koin = startKoin { modules(iosModule) }.koin
    koin.get<PlaceNameResolver>().start()
}
