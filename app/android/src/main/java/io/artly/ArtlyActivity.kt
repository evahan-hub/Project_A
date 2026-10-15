package io.artly

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import io.artly.data.SharedPhotos
import io.artly.artworks.picture.AndroidPictureStorage
import io.artly.ui.screen.main.MainScreen
import io.artly.ui.theme.ArtlyTheme
import org.koin.android.ext.android.inject

class ArtlyActivity : ComponentActivity() {
    private val pictureStorage: AndroidPictureStorage by inject()

    // Photos shared with this window of the app, e.g. from the photo library. Each share opens a window of its own in
    // the sharing app, which returns there once the artwork is recorded.
    private val sharedPhotos = SharedPhotos()

    override fun onCreate(savedInstanceState: Bundle?) {
        // The app is light only, so the system bars need dark icons, also when the system is in dark mode.
        val lightBars = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = lightBars, navigationBarStyle = lightBars)
        super.onCreate(savedInstanceState)

        // Not again when recreated, e.g. after rotating, as the shared photo is open already.
        if (savedInstanceState == null) onShare(intent)
        // Opened to record a shared photo, so it returns to the app it was shared from once done.
        val isShare = intent.action == Intent.ACTION_SEND

        setContent {
            ArtlyTheme {
                MainScreen(sharedPhotos = sharedPhotos, onSharedPhotoDone = if (isShare) ::finish else null)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        onShare(intent)
    }

    /**
     * Imports a shared photo. Without asking for the permission to read where it was taken, as asking shows a picker
     * of photos to allow on recent Android versions. Photo apps share the original anyway, e.g. Google Photos.
     */
    private fun onShare(intent: Intent) {
        if (intent.action != Intent.ACTION_SEND) return
        val uri = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java) ?: return
        lifecycleScope.launch { pictureStorage.importPicture(uri)?.let(sharedPhotos::add) }
    }
}
