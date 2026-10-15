plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

// The Compose resources plugin doesn't remove deleted resources from the Android assets it copies, so they would
// still be packaged into the app. Clear the assets before every copy.
subprojects {
    tasks.matching { it.name == "copyAndroidMainComposeResourcesToAndroidAssets" }.configureEach {
        doFirst { outputs.files.forEach { it.deleteRecursively() } }
    }
}
