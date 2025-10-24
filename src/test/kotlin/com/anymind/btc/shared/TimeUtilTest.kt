package com.anymind.btc.shared

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class TimeUtilTest {
    @Test
    fun floorToUtcHour() {
        val t = TimeUtil.parseToInstant("2019-10-05T14:45:05+07:00")
        val floored = TimeUtil.floorToUtcHour(t)
        assertEquals("2019-10-05T07:00:00Z", floored.toString())
    }

    @Test
    fun parseToInstant() {
        assertDoesNotThrow { TimeUtil.parseToInstant("2019-10-05T14:45:05Z") }
    }
}
