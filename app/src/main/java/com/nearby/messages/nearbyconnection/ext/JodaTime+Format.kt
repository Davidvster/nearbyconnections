package com.nearby.messages.nearbyconnection.ext

import android.content.Context
import android.text.format.DateFormat
import com.nearby.messages.nearbyconnection.R
import com.nearby.messages.nearbyconnection.arch.AppModule
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

enum class JodaTemporalFormat(private val formatter12: DateTimeFormatter, private val formatter24: DateTimeFormatter? = null) {
    DELIVERY_ETA(buildDateFormatter("EEEEE dd MMM")),
    PARCEL_DETAILS(buildDateFormatter("EEE dd MMM")),
    JOURNEY_EVENT(buildDateFormatter("hh:mm - EEE dd MMM"), buildDateFormatter("HH:mm - EEE dd MMM"));

    internal fun getFormatter(): DateTimeFormatter {
        if (formatter24 == null || !DateFormat.is24HourFormat(AppModule.application)) {
            return formatter12
        } else {
            return formatter24
        }
    }
}

private fun buildDateFormatter(pattern: String) =
        DateTimeFormat.forPattern(pattern)
                .withZone(DateTimeZone.getDefault())

fun DateTime.isYesterday(): Boolean {
    val today = DateTime.now().minusDays(1)
    return (today.dayOfYear == dayOfYear &&
            today.year == year)
}

fun DateTime.isToday(): Boolean {
    val today = DateTime.now()
    return (today.dayOfYear == dayOfYear &&
            today.year == year)
}

fun DateTime.isTomorrow(): Boolean {
    val tomorrow = DateTime.now().plusDays(1)
    return (tomorrow.dayOfYear == dayOfYear &&
            tomorrow.year == year)
}

fun DateTime.willFormatAsWord(): Boolean {
    return isTomorrow() || isToday() || isYesterday()
}

/**
 * prints Today or Tomorrow if instant is today or tomorrow
 * otherwise acts like format(InstantFormat)
 */

fun DateTime.format(format: JodaTemporalFormat) = format.getFormatter().print(this)

object JodaDateParser {
    private val dateFormatter = buildDateFormatter("yyyy-mm-dd")

    fun parseDateOnly(value: String): DateTime {
        return dateFormatter.parseDateTime(value)
    }
}
