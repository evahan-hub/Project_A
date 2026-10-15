package io.artly.artworks.picture

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import io.artly.artworks.applicationSupportDirectory
import io.artly.location.model.Coordinates
import kotlinx.cinterop.reinterpret
import platform.CoreFoundation.CFRelease
import cnames.structs.__CFData
import cnames.structs.__CFDictionary
import cnames.structs.__CFString
import cnames.structs.__CFURL
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSMutableData
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.writeToFile
import platform.ImageIO.CGImageDestinationAddImageFromSource
import platform.ImageIO.CGImageDestinationCreateWithData
import platform.ImageIO.CGImageDestinationFinalize
import platform.ImageIO.CGImageSourceCopyPropertiesAtIndex
import platform.ImageIO.CGImageSourceCreateWithData
import platform.ImageIO.CGImageSourceCreateWithURL

/**
 * Stores pictures in the app's application support directory.
 */
@OptIn(ExperimentalForeignApi::class)
class IosPictureStorage(
    private val directory: String = "${applicationSupportDirectory()}/$PICTURE_DIR",
) : PictureStorage {

    /**
     * Saves a picture as JPEG, e.g. taken with the camera or picked from the photo library, which often has HEIC
     * pictures. Keeps when and where it was taken. Returns its file name, or null if it couldn't be converted or
     * written.
     */
    suspend fun importPicture(data: NSData): String? = withContext(Dispatchers.IO) {
        jpegWithMetadata(data)?.let { savePicture(it, "jpg") }
    }

    private suspend fun savePicture(data: NSData, extension: String): String? = withContext(Dispatchers.IO) {
        NSFileManager.defaultManager.createDirectoryAtPath(
            directory,
            withIntermediateDirectories = true,
            attributes = null,
            error = null,
        )
        val fileName = "artly_${NSUUID().UUIDString}.$extension"
        fileName.takeIf { data.writeToFile(path(it), atomically = true) }
    }

    override fun path(fileName: String): String = "$directory/$fileName"

    override suspend fun readMetadata(fileName: String): PhotoMetadata? = withContext(Dispatchers.IO) {
        if (!isPlainFileName(fileName)) return@withContext null
        val url = CFBridgingRetain(NSURL.fileURLWithPath(path(fileName)))
        val source = CGImageSourceCreateWithURL(url?.reinterpret<__CFURL>(), null)
        CFRelease(url)
        source ?: return@withContext null
        val properties = CFBridgingRelease(CGImageSourceCopyPropertiesAtIndex(source, 0u, null)) as? Map<*, *>
        CFRelease(source)
        properties?.let(::photoMetadata)
    }

    override suspend fun delete(fileName: String): Boolean = withContext(Dispatchers.IO) {
        isPlainFileName(fileName) && NSFileManager.defaultManager.removeItemAtPath(path(fileName), error = null)
    }

    private companion object {
        const val PICTURE_DIR = "Pictures"
    }
}

/**
 * Converts a picture to JPEG with ImageIO, keeping its metadata, or returns null if it can't be read.
 */
@OptIn(ExperimentalForeignApi::class)
internal fun jpegWithMetadata(data: NSData): NSData? {
    val input = CFBridgingRetain(data)
    val source = CGImageSourceCreateWithData(input?.reinterpret<__CFData>(), null)
    CFRelease(input)
    source ?: return null
    val output = NSMutableData()
    val outputRef = CFBridgingRetain(output)
    val type = CFBridgingRetain("public.jpeg")
    // The value of ImageIO's kCGImageDestinationLossyCompressionQuality.
    val options = CFBridgingRetain(mapOf("kCGImageDestinationLossyCompressionQuality" to JPEG_QUALITY))
    val destination = CGImageDestinationCreateWithData(
        outputRef?.reinterpret<__CFData>(),
        type?.reinterpret<__CFString>(),
        1u,
        null,
    )
    val written = destination != null && run {
        CGImageDestinationAddImageFromSource(destination, source, 0u, options?.reinterpret<__CFDictionary>())
        CGImageDestinationFinalize(destination)
    }
    destination?.let { CFRelease(it) }
    CFRelease(source)
    CFRelease(outputRef)
    CFRelease(type)
    CFRelease(options)
    return output.takeIf { written }
}

private const val JPEG_QUALITY = 0.85

/**
 * Reads when and where a photo was taken from its image properties, see `CGImageSourceCopyPropertiesAtIndex`. The keys
 * are the values of ImageIO's `kCGImageProperty…` constants.
 */
internal fun photoMetadata(properties: Map<*, *>): PhotoMetadata? {
    val exif = properties["{Exif}"] as? Map<*, *>
    val gps = properties["{GPS}"] as? Map<*, *>
    val location = gps?.let {
        val latitude = it.number("Latitude") ?: return@let null
        val longitude = it.number("Longitude") ?: return@let null
        // The coordinates are unsigned, with the hemisphere in a reference letter.
        Coordinates(
            latitude = if (it["LatitudeRef"] == "S") -latitude else latitude,
            longitude = if (it["LongitudeRef"] == "W") -longitude else longitude,
            // An altitude reference of 1 means below sea level.
            altitude = it.number("Altitude")?.let { altitude ->
                if (it.number("AltitudeRef") == 1.0) -altitude else altitude
            },
        )
    }
    return PhotoMetadata(
        dateTime = parseExifDateTime(exif?.get("DateTimeOriginal") as? String),
        timeZone = parseExifOffset(exif?.get("OffsetTimeOriginal") as? String),
        location = location,
    ).takeUnless { it.isEmpty }
}

private fun Map<*, *>.number(key: String): Double? = when (val value = get(key)) {
    is NSNumber -> value.doubleValue
    is Number -> value.toDouble()
    else -> null
}
