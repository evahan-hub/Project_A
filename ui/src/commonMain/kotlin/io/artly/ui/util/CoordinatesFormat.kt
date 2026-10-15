package io.artly.ui.util

import kotlin.math.abs
import kotlin.math.roundToLong
import io.artly.location.model.Coordinates

/**
 * Formats coordinates as latitude and longitude with five decimals, about a metre. They always use a decimal point,
 * regardless of the locale.
 */
fun Coordinates.format(): String = "${formatDegrees(latitude)}, ${formatDegrees(longitude)}"

private fun formatDegrees(value: Double): String {
    val scaled = (abs(value) * DECIMALS_FACTOR).roundToLong()
    val sign = if (value < 0 && scaled != 0L) "-" else ""
    val fraction = (scaled % DECIMALS_FACTOR).toString().padStart(DECIMALS, '0')
    return "$sign${scaled / DECIMALS_FACTOR}.$fraction"
}

private const val DECIMALS = 5
private const val DECIMALS_FACTOR = 100_000L
