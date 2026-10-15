package io.artly.ui.util

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

actual fun formatDateTime(dateTime: LocalDateTime, locale: Locale): String {
    return dateTime.toJavaLocalDateTime().format(
        DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
            .withLocale(locale.platformLocale)
    )
}

actual fun formatShortDate(dateTime: LocalDateTime, locale: Locale): String {
    return formatSkeleton(dateTime, locale, "EEEdMMM")
}

actual fun formatMonth(dateTime: LocalDateTime, locale: Locale): String {
    return formatSkeleton(dateTime, locale, "MMMMyyyy")
}

private fun formatSkeleton(dateTime: LocalDateTime, locale: Locale, skeleton: String): String {
    val pattern = DateFormat.getBestDateTimePattern(locale.platformLocale, skeleton)
    return dateTime.toJavaLocalDateTime().format(DateTimeFormatter.ofPattern(pattern, locale.platformLocale))
}

@Composable
actual fun is24HourClock(): Boolean = DateFormat.is24HourFormat(LocalContext.current)
