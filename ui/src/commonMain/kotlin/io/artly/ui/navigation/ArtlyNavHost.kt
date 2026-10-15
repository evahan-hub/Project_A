package io.artly.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.artly.ui.screen.artworks.ArtworkDetailRoute
import io.artly.ui.screen.artworks.ArtworkListRoute
import io.artly.ui.screen.entry.SaveArtworkScreenRoute

@Composable
fun ArtlyNavHost(
    navController: NavHostController,
    /** Called once a shared photo is recorded or dismissed, instead of going back, or null to go back. */
    onSharedPhotoDone: (() -> Unit)?,
    modifier: Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.ArtworkList.route,
        modifier = modifier,
    ) {
        composable(Screen.ArtworkList.route) {
            ArtworkListRoute(
                onArtworkClick = { navController.navigate(Screen.ArtworkDetails.createRoute(it.id)) }
            )
        }
        composable(Screen.SaveArtwork.route) {
            SaveArtworkScreenRoute { navController.popBackStack() }
        }
        composable(
            route = Screen.LogSharedPhoto.route,
            arguments = listOf(navArgument("photoFile") { type = NavType.StringType })
        ) {
            SaveArtworkScreenRoute { onSharedPhotoDone?.invoke() ?: navController.popBackStack() }
        }
        composable(
            route = Screen.EditArtwork.route,
            arguments = listOf(navArgument("artworkId") { type = NavType.IntType })
        ) {
            SaveArtworkScreenRoute { navController.popBackStack() }
        }
        composable(
            route = Screen.ArtworkDetails.route,
            arguments = listOf(navArgument("artworkId") { type = NavType.IntType })
        ) {
            ArtworkDetailRoute(
                onBackClick = { navController.popBackStack() },
                onEditClick = { navController.navigate(Screen.EditArtwork.createRoute(it.id)) },
            )
        }
    }
}
