# iOS app

The Xcode project around the shared UI of the `ui` module. Its build phase runs Gradle, which builds the `ArtlyUi`
framework with the Compose resources, so open `iosApp.xcodeproj` in Xcode and run it like any app.

From the command line, e.g. on the iPhone 18 Pro simulator:

```sh
xcodebuild -project app/ios/iosApp.xcodeproj -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 18 Pro' build
xcodebuild -project app/ios/iosApp.xcodeproj -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 18 Pro' test
```

The tests in `iosAppUITests` walk through the main screens with XCUITest, as Compose UI on iOS is accessible like
UIKit. The test of recording an artwork picks a photo of the simulator's sample photos.

Uncaught Kotlin exceptions are logged with their causes before the app terminates, as the system log doesn't keep the
standard error: `xcrun simctl spawn booted log show --predicate 'process == "Artly" AND eventMessage BEGINSWITH "Uncaught"'`.
