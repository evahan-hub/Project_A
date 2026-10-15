import CoreLocation
import XCTest

/// Walks through the main screens of the app, which come from the shared Compose UI.
final class SmokeTests: XCTestCase {
    private let app = XCUIApplication()

    override func setUp() {
        continueAfterFailure = false
        // Amsterdam, as the simulator might have no location.
        XCUIDevice.shared.location = XCUILocation(location: CLLocation(latitude: 52.3600, longitude: 4.8852))
        app.launch()
    }

    /// Allows the app to use the location, if it asks for the permission.
    private func allowLocation() {
        let springboard = XCUIApplication(bundleIdentifier: "com.apple.springboard")
        let allow = springboard.buttons["Allow While Using App"]
        if allow.waitForExistence(timeout: 3) {
            allow.tap()
        }
    }

    func testRecordAnArtworkWithAPhotoFromTheLibrary() {
        // The button on the list of artworks, which the app starts with.
        app.buttons["Record art"].tap()
        allowLocation()

        app.buttons["Browse"].tap()
        // The photos in the system's picker, e.g. the sample photos of the simulator.
        let photo = app.images.matching(identifier: "PXGGridLayout-Info").firstMatch
        XCTAssertTrue(photo.waitForExistence(timeout: 10))
        attachScreenshot("photo-picker")
        photo.tap()
        XCTAssertTrue(app.staticTexts["Retake"].waitForExistence(timeout: 10))

        // Compose text fields are text views on iOS.
        let title = app.textViews.firstMatch
        XCTAssertTrue(title.waitForExistence(timeout: 5))
        title.tap()
        title.typeText("Smoke Test Painting")
        attachScreenshot("artwork-with-photo")

        app.buttons["Save artwork"].tap()
        // Back on the list.
        let artwork = app.staticTexts["Smoke Test Painting"].firstMatch
        XCTAssertTrue(artwork.waitForExistence(timeout: 10))
        attachScreenshot("artworks-list")

        artwork.tap()
        XCTAssertTrue(app.staticTexts["SEEN"].waitForExistence(timeout: 5))
        attachScreenshot("artwork-details")
    }

    private func attachScreenshot(_ name: String) {
        let attachment = XCTAttachment(screenshot: app.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
