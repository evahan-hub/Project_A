package io.artly.location.model

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A position on Earth in WGS 84, as used by GPS.
 *
 * @param altitude Meters above the WGS 84 ellipsoid, or null if unknown.
 */
data class Coordinates(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
)

/**
 * Returns the distance to [other] in meters along the surface of the Earth, ignoring the altitude. Precise to about
 * 0.5 %, as the Earth is treated as a sphere.
 */
fun Coordinates.distanceTo(other: Coordinates): Double {
    val lat1 = latitude.toRadians()
    val lat2 = other.latitude.toRadians()
    val dLat = lat2 - lat1
    val dLon = (other.longitude - longitude).toRadians()
    val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
    return 2 * EARTH_RADIUS_METERS * asin(sqrt(a))
}

private fun Double.toRadians() = this * PI / 180

private const val EARTH_RADIUS_METERS = 6_371_000.0
