package io.artly.location.model

/**
 * Names a place by the locality, or the municipality outside of localities, and the region, e.g. a province or state:
 * "Hoog Soeren, Gelderland". Never by street, which is too precise in a forest, and reveals too much when shared.
 * Returns null if none of them is known.
 */
fun placeName(locality: String?, municipality: String?, region: String?): String? {
    val place = locality?.takeIf { it.isNotBlank() } ?: municipality?.takeIf { it.isNotBlank() }
    val area = region?.takeIf { it.isNotBlank() && it != place }
    return listOfNotNull(place, area).joinToString(", ").takeIf { it.isNotEmpty() }
}
