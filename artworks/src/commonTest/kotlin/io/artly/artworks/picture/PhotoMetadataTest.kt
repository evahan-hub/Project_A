package io.artly.artworks.picture

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.Test

class PhotoMetadataTest {

    @Test
    fun parsesExifDateTime() {
        assertEquals(LocalDateTime(2026, 10, 4, 14, 32, 7), parseExifDateTime("2026:10:04 14:32:07"))
    }

    @Test
    fun rejectsMissingOrInvalidExifDateTime() {
        assertNull(parseExifDateTime(null))
        // Cameras without a clock write blanks or zeros.
        assertNull(parseExifDateTime("    :  :     :  :  "))
        assertNull(parseExifDateTime("0000:00:00 00:00:00"))
        assertNull(parseExifDateTime("2026-10-04T14:32:07"))
    }

    @Test
    fun parsesExifOffset() {
        assertEquals(TimeZone.of("+02:00"), parseExifOffset("+02:00"))
        assertNull(parseExifOffset(null))
        assertNull(parseExifOffset("Europe/Berlin"))
    }
}
