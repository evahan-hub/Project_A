package io.artly.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.timeZoneForSecondsFromGMT

actual fun formatDateTime(dateTime: LocalDateTime, locale: Locale): String {
    return format(dateTime, locale) {
        dateStyle = NSDateFormatterMediumStyle
        timeStyle = NSDateFormatterShortStyle
    }
}

actual fun formatShortDate(dateTime: LocalDateTime, locale: Locale): String {
    return format(dateTime, locale) { setLocalizedDateFormatFromTemplate("EEEdMMM") }
}

actual fun formatMonth(dateTime: LocalDateTime, locale: Locale): String {
    return format(dateTime, locale) { setLocalizedDateFormatFromTemplate("MMMMyyyy") }
}

// The local date and time is formatted as is, so it's taken as UTC and formatted in UTC.
private fun format(dateTime: LocalDateTime, locale: Locale, configure: NSDateFormatter.() -> Unit): String {
    val formatter = NSDateFormatter()
    formatter.locale = NSLocale(localeIdentifier = locale.toLanguageTag())
    formatter.timeZone = NSTimeZone.timeZoneForSecondsFromGMT(0)
    formatter.configure()
    return formatter.stringFromDate(dateTime.toInstant(TimeZone.UTC).toNSDate())
}

@Composable
actual fun is24HourClock(): Boolean {
    // The hour in the user's preferred format, which has an AM/PM marker on a 12-hour clock.
    val format = NSDateFormatter.dateFormatFromTemplate("j", options = 0u, locale = NSLocale.currentLocale)
    return format?.contains('a') != true
}
