package io.artly.ui.util

import kotlin.test.Test
import kotlin.test.assertEquals
import io.artly.location.model.Coordinates

class CoordinatesFormatTest {

    @Test
    fun formatsWithFiveDecimals() {
        assertEquals("51.92250, 4.47917", Coordinates(51.9225, 4.479168).format())
    }

    @Test
    fun formatsNegativeCoordinates() {
        assertEquals("-33.86882, -0.00500", Coordinates(-33.868820, -0.005).format())
    }

    @Test
    fun dropsTheSignOfValuesRoundedToZero() {
        assertEquals("0.00000, 180.00000", Coordinates(-0.000001, 179.999999).format())
    }
}
