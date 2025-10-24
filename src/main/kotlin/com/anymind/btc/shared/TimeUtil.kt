package com.anymind.btc.shared

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object TimeUtil {
    private val iso: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx")

    fun parseToInstant(s: String): Instant =
        try {
            Instant.parse(s)
        } catch (e: DateTimeParseException) {
            // Convert to an IllegalArgumentException so it maps to 400
            throw IllegalArgumentException(
                e.message,
                e
            )
        }

    fun floorToUtcHour(instant: Instant): Instant =
        instant.atOffset(ZoneOffset.UTC)
            .withMinute(0).withSecond(0).withNano(0).toInstant()

    fun ceilToUtcHour(instant: Instant): Instant =
        instant.atOffset(ZoneOffset.UTC)
            .withMinute(0).withSecond(0).withNano(0).plusHours(1).toInstant()

    fun formatUtcHour(instant: Instant): String =
        iso.format(instant.atOffset(ZoneOffset.UTC))
}
