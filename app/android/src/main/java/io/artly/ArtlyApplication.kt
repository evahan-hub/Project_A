package io.artly

import android.app.Application
import io.artly.data.PlaceNameResolver
import io.artly.di.appModule
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ArtlyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ArtlyApplication)
            modules(appModule)
        }
        get<PlaceNameResolver>().start()
    }
}
