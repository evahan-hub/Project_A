package io.artly.ui.screen.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.artly.data.SharedPhotos
import io.artly.ui.navigation.ArtlyNavHost
import io.artly.ui.navigation.Screen
import io.artly.ui.resources.Res
import io.artly.ui.resources.add_artwork_desc
import org.jetbrains.compose.resources.stringResource

/**
 * The app with its navigation.
 *
 * @param sharedPhotos Photos shared from other apps, each opened to record an artwork of it.
 * @param onSharedPhotoDone Called once a shared photo is recorded or dismissed, e.g. to return to the app it was
 *   shared from, or null to go back in this app.
 */
@Composable
fun MainScreen(sharedPhotos: SharedPhotos, onSharedPhotoDone: (() -> Unit)? = null) {
    val navController = rememberNavController()
    LaunchedEffect(sharedPhotos) {
        sharedPhotos.photos.collect { navController.navigate(Screen.LogSharedPhoto.createRoute(it)) }
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        floatingActionButton = {
            if (currentRoute == Screen.ArtworkList.route) {
                val addArtwork = stringResource(Res.string.add_artwork_desc)
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Screen.SaveArtwork.route) },
                    icon = { Icon(Icons.Outlined.PhotoCamera, contentDescription = null) },
                    text = { Text(addArtwork) },
                    // Its text isn't its label on iOS, so VoiceOver wouldn't read it.
                    modifier = Modifier.semantics { contentDescription = addArtwork },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    ) { innerPadding ->
        ArtlyNavHost(
            navController = navController,
            onSharedPhotoDone = onSharedPhotoDone,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}
