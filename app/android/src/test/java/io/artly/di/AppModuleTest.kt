package io.artly.di

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.russhwolf.settings.Settings
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

class AppModuleTest {

    /**
     * Koin resolves dependencies at runtime, so check that every constructor gets what it needs, which Hilt checked
     * at compile time.
     */
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun allDependenciesCanBeResolved() {
        // Provided by Koin itself: the context by androidContext(), the saved state to view models. The settings of
        // the notices are created along with them.
        appModule.verify(extraTypes = listOf(Context::class, SavedStateHandle::class, Settings::class))
    }
}
