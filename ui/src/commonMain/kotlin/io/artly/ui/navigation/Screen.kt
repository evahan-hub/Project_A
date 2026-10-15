package io.artly.ui.navigation

sealed class Screen(val route: String) {
    object ArtworkList : Screen("artworks")
    object SaveArtwork : Screen("save_artwork")

    /** Records an artwork of a photo shared from another app, by its file name in the picture storage. */
    object LogSharedPhoto : Screen("log_photo/{photoFile}") {
        fun createRoute(photoFile: String): String {
            return "log_photo/$photoFile"
        }
    }
    object EditArtwork : Screen("edit_artwork/{artworkId}") {
        fun createRoute(artworkId: Int): String {
            return "edit_artwork/$artworkId"
        }
    }
    object ArtworkDetails : Screen("artwork/{artworkId}") {
        fun createRoute(artworkId: Int): String {
            return "artwork/$artworkId"
        }
    }
}
