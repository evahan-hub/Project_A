package io.artly.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.LocalDateTime

/**
 * Formats a local date and time for display in the given locale, e.g. "Oct 4, 2026, 2:32 PM".
 */
expect fun formatDateTime(dateTime: LocalDateTime, locale: Locale): String

/**
 * Formats the date briefly, e.g. "Sat, Oct 4" or "Sa., 4. Okt.", depending on the locale.
 */
expect fun formatShortDate(dateTime: LocalDateTime, locale: Locale): String

/**
 * Formats the month and year, e.g. "October 2026" or "Oktober 2026".
 */
expect fun formatMonth(dateTime: LocalDateTime, locale: Locale): String

/**
 * Whether the user's clock shows 24 hours rather than AM and PM, as set on the device.
 */
@Composable
expect fun is24HourClock(): Boolean
