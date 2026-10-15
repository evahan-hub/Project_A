package io.artly.ui.util

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Checks the formats of iOS by their parts. Android formats with the Android framework, which host tests lack.
 */
class LocalizedFormatTest {

    private val dateTime = LocalDateTime(2026, 10, 4, 14, 32)
    private val german = Locale("de-DE")
    private val english = Locale("en-US")

    @Test
    fun namesCountriesInTheLanguage() {
        assertEquals("Deutschland", countryName("DE", german))
        assertEquals("Netherlands", countryName("NL", english))
    }

    @Test
    fun formatsDatesInTheLanguage() {
        val short = formatShortDate(dateTime, german)
        assertTrue("4" in short && "Okt" in short, short)
        assertEquals("Oktober 2026", formatMonth(dateTime, german))
        assertEquals("October 2026", formatMonth(dateTime, english))
        val full = formatDateTime(dateTime, english)
        assertTrue("Oct" in full && "2026" in full && "2:32" in full, full)
    }
}
