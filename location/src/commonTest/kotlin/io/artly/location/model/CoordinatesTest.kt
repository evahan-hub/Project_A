package io.artly.location.model

import kotlin.test.Test
import kotlin.test.assertEquals

class CoordinatesTest {

    @Test
    fun distanceTo_isZeroForTheSamePlace() {
        val amsterdam = Coordinates(52.3676, 4.9041)
        assertEquals(0.0, amsterdam.distanceTo(amsterdam))
    }

    @Test
    fun distanceTo_matchesKnownDistances() {
        val amsterdam = Coordinates(52.3676, 4.9041)
        val berlin = Coordinates(52.5200, 13.4050)
        // 577 km along a great circle.
        assertEquals(577_000.0, amsterdam.distanceTo(berlin), absoluteTolerance = 3_000.0)
        assertEquals(amsterdam.distanceTo(berlin), berlin.distanceTo(amsterdam), absoluteTolerance = 0.001)
        // One degree of latitude is about 111 km.
        assertEquals(111_195.0, Coordinates(0.0, 0.0).distanceTo(Coordinates(1.0, 0.0)), absoluteTolerance = 1.0)
    }

    @Test
    fun distanceTo_ignoresAltitude() {
        assertEquals(0.0, Coordinates(47.0, 11.0, 2_000.0).distanceTo(Coordinates(47.0, 11.0, 500.0)))
    }
}
