package com.kaushalpanjee.core.util.optimize

import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

object DateTimeUtil {

    fun nowIso(): String =
        LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)

    fun formatUtcToLocal(input: String, output: String): String {
        val utc = ZonedDateTime.parse(input)
        return utc.withZoneSameInstant(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern(output))
    }

    fun formatLocalToUtc(input: String, pattern: String): String {
        val formatter = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)
        val local = LocalDateTime.parse(input, formatter)
        return local.atZone(ZoneId.systemDefault())
            .withZoneSameInstant(ZoneId.of("UTC"))
            .format(DateTimeFormatter.ISO_INSTANT)
    }
}
