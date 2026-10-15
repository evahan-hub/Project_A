package io.artly.location.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlaceNameTest {

    @Test
    fun namesTheLocalityAndRegion() {
        assertEquals("Hoog Soeren, Gelderland", placeName("Hoog Soeren", "Apeldoorn", "Gelderland"))
    }

    @Test
    fun namesTheMunicipalityOutsideOfLocalities() {
        assertEquals("Ermelo, Gelderland", placeName(null, "Ermelo", "Gelderland"))
    }

    @Test
    fun namesTheRegionAlone_onceOnly() {
        assertEquals("Bayern", placeName(null, null, "Bayern"))
        assertEquals("Berlin", placeName("Berlin", "Berlin", "Berlin"))
    }

    @Test
    fun hasNoNameWithoutParts() {
        assertNull(placeName(null, " ", null))
    }
}
