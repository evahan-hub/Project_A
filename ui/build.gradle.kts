plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

// The UI of the app, shared by Android and iOS: theme, components, screens and their resources.
kotlin {
    android {
        namespace = "io.artly.ui"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        androidResources {
            enable = true
        }
        // Runs the tests of commonTest on the computer, besides the iOS simulator.
        withHostTest { }
    }

    // The framework the iOS app in app/ios is built with.
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "ArtlyUi"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":artworks"))
            api(project(":location"))
            api(libs.compose.runtime)
            api(libs.cmp.foundation)
            api(libs.cmp.ui)
            api(libs.cmp.material3)
            api(libs.cmp.material.icons.extended)
            api(libs.compose.components.resources)
            implementation(libs.cmp.ui.tooling.preview)
            api(libs.kotlinx.datetime)
            api(libs.cmp.lifecycle.viewmodel.compose)
            api(libs.cmp.lifecycle.viewmodel.savedstate)
            api(libs.cmp.lifecycle.runtime.compose)
            api(libs.navigation.compose)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.coil.compose)
            api(libs.multiplatform.settings)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.multiplatform.settings.test)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "io.artly.ui.resources"
}
