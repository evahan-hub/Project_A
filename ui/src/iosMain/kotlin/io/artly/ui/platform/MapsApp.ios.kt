package io.artly.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.artly.location.model.Coordinates
import platform.Foundation.NSURL
import platform.Foundation.NSURLComponents
import platform.Foundation.NSURLQueryItem
import platform.UIKit.UIApplication

@Composable
actual fun rememberMapsAppOpener(): (location: Coordinates, label: String) -> Unit = remember {
    { location, label ->
        // Apple Maps is always installed, and opens these links.
        val components = NSURLComponents(string = "https://maps.apple.com/")
        components.queryItems = listOf(
            NSURLQueryItem(name = "ll", value = "${location.latitude},${location.longitude}"),
            NSURLQueryItem(name = "q", value = label),
        )
        components.URL?.let { url: NSURL ->
            UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
        }
    }
}
