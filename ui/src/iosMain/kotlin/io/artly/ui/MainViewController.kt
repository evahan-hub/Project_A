package io.artly.ui

import androidx.compose.ui.window.ComposeUIViewController
import io.artly.ui.screen.main.MainScreen
import io.artly.ui.theme.ArtlyTheme
import org.koin.compose.koinInject
import platform.UIKit.UIViewController

/**
 * The app's only view controller, which shows the shared UI. Call [io.artly.ui.di.startApp] first.
 */
@Suppress("FunctionName", "unused") // Called from Swift.
fun MainViewController(): UIViewController = ComposeUIViewController {
    ArtlyTheme {
        MainScreen(sharedPhotos = koinInject())
    }
}
