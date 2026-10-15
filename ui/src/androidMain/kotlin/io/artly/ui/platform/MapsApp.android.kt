package io.artly.ui.platform

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import io.artly.location.model.Coordinates
import io.artly.ui.resources.Res
import io.artly.ui.resources.no_maps_app
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun rememberMapsAppOpener(): (location: Coordinates, label: String) -> Unit {
    val context = LocalContext.current
    val noMapsApp = stringResource(Res.string.no_maps_app)
    return remember(context, noMapsApp) {
        { location, label ->
            val position = "${location.latitude},${location.longitude}"
            val uri = "geo:$position?q=$position(${Uri.encode(label)})".toUri()
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, noMapsApp, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
