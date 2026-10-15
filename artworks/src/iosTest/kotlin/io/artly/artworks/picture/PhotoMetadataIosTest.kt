package io.artly.artworks.picture

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import cnames.structs.__CFData
import cnames.structs.__CFDictionary
import cnames.structs.__CFString
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import io.artly.location.model.Coordinates
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSDictionary
import platform.Foundation.NSMutableData
import platform.Foundation.create
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.dictionaryWithDictionary
import platform.ImageIO.CGImageDestinationAddImageFromSource
import platform.ImageIO.CGImageDestinationCreateWithData
import platform.ImageIO.CGImageDestinationFinalize
import platform.ImageIO.CGImageSourceCreateWithData
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIImagePNGRepresentation

class PhotoMetadataIosTest {

    @Test
    fun readsDateTimeAndLocation() {
        val properties = dictionary(
            "{Exif}" to mapOf("DateTimeOriginal" to "2026:10:04 14:32:07", "OffsetTimeOriginal" to "+02:00"),
            "{GPS}" to mapOf(
                "Latitude" to 51.9,
                "LatitudeRef" to "N",
                "Longitude" to 4.5,
                "LongitudeRef" to "W",
                "Altitude" to 3.0,
                "AltitudeRef" to 1,
            ),
        )

        assertEquals(
            PhotoMetadata(
                dateTime = LocalDateTime(2026, 10, 4, 14, 32, 7),
                timeZone = TimeZone.of("+02:00"),
                location = Coordinates(51.9, -4.5, -3.0),
            ),
            photoMetadata(properties),
        )
    }

    @Test
    fun returnsNullWithoutDateTimeAndLocation() {
        assertNull(photoMetadata(dictionary("{Exif}" to mapOf("OffsetTimeOriginal" to "+02:00"))))
    }

    @Test
    fun returnsNullForMissingOrForeignFiles() = runTest {
        val storage = IosPictureStorage(directory = NSTemporaryDirectory())
        assertNull(storage.readMetadata("missing.jpg"))
        assertNull(storage.readMetadata("../missing.jpg"))
    }

    @Test
    fun importKeepsWhenAndWhereAPhotoWasTaken() = runTest {
        val storage = IosPictureStorage(directory = NSTemporaryDirectory() + "artly-test")
        val fileName = assertNotNull(storage.importPicture(pngPhoto()))

        assertTrue(fileName.endsWith(".jpg"))
        assertEquals(
            PhotoMetadata(
                dateTime = LocalDateTime(2026, 10, 4, 14, 32, 7),
                timeZone = null,
                location = Coordinates(51.9, 4.5),
            ),
            storage.readMetadata(fileName),
        )
        assertTrue(storage.delete(fileName))
    }

    @Test
    fun importRejectsDataThatIsNoPicture() = runTest {
        val storage = IosPictureStorage(directory = NSTemporaryDirectory() + "artly-test")
        assertNull(storage.importPicture("no picture".encodeToByteArray().toNSData()))
    }

    // A PNG of a single pixel, with the EXIF and GPS data a camera writes, like a photo in another format than JPEG.
    @OptIn(ExperimentalForeignApi::class)
    private fun pngPhoto(): NSData {
        val image = UIGraphicsImageRenderer(size = CGSizeMake(1.0, 1.0)).imageWithActions { }
        val png = assertNotNull(UIImagePNGRepresentation(image))
        val source = CGImageSourceCreateWithData(CFBridgingRetain(png)?.reinterpret<__CFData>(), null)
        val output = NSMutableData()
        val destination = CGImageDestinationCreateWithData(
            CFBridgingRetain(output)?.reinterpret<__CFData>(),
            CFBridgingRetain("public.png")?.reinterpret<__CFString>(),
            1u,
            null,
        )
        val properties = mapOf(
            "{Exif}" to mapOf("DateTimeOriginal" to "2026:10:04 14:32:07"),
            "{GPS}" to mapOf("Latitude" to 51.9, "LatitudeRef" to "N", "Longitude" to 4.5, "LongitudeRef" to "E"),
        )
        CGImageDestinationAddImageFromSource(
            destination,
            source,
            0u,
            CFBridgingRetain(properties)?.reinterpret<__CFDictionary>(),
        )
        assertTrue(CGImageDestinationFinalize(destination))
        return output
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private fun ByteArray.toNSData(): NSData = usePinned {
        NSData.create(bytes = it.addressOf(0), length = size.toULong())
    }

    // Like the properties ImageIO returns, with Foundation's dictionaries and numbers.
    private fun dictionary(vararg entries: Pair<String, Map<String, Any>>): Map<*, *> {
        return NSDictionary.dictionaryWithDictionary(entries.toMap<Any?, Any?>())
    }
}
