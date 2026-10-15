# artly.io

Take photos of art you see and keep a record of it on your phone: the photo, title, artist, when and where you saw
it, and a note. Everything is stored locally; only the coordinates are sent online, to look up place names.

A Kotlin Multiplatform app for Android and iOS with a shared Compose UI, started from parts of Cantha.

## Modules

- `artworks`: the artworks in a Room database, and their photos in the app's private files, with when and where they
  were taken from their EXIF data.
- `location`: the location of the device and place names, with Google Play services and Android's geocoder, or Core
  Location and Apple's geocoder.
- `ui`: the shared UI: theme, screens, navigation and dependency injection with Koin.
- `app/android`: the Android app. `app/ios`: the Xcode project of the iOS app, see its README.

## Building

```sh
./gradlew assembleDebug                      # Android app
./gradlew testDebugUnitTest :ui:testAndroidHostTest :artworks:jvmTest :location:jvmTest
```

The artworks are the user's own data: never wipe the database on a schema change. Bump the version of
`ArtworkDatabase` together with a migration, and keep the exported schemas in `artworks/schemas`.
