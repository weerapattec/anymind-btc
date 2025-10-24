package com.anymind.btc.application

import com.anymind.btc.infrastructure.persistence.HourlyDeltaEntity
import com.anymind.btc.infrastructure.persistence.HourlyDeltaRepository
import com.anymind.btc.shared.TimeUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.math.BigDecimal
import java.time.Instant

class GetBalanceHistoryUseCaseTest {

    @Test
    fun `builds hourly series with prefix and gaps`() {
        val repo = Mockito.mock(HourlyDeltaRepository::class.java)
        val uc = GetBalanceHistoryUseCase(repo, BigDecimal("1000.0"))

        val start = "2019-10-05T12:48:01+00:00"
        val end   = "2019-10-05T17:48:02+00:00"

        val startHour = TimeUtil.floorToUtcHour(TimeUtil.parseToInstant(start))
        val endHour   = TimeUtil.floorToUtcHour(TimeUtil.parseToInstant(end))

        Mockito.`when`(repo.prefixBefore(startHour)).thenReturn(BigDecimal("100.0"))

        val deltas = listOf(
            HourlyDeltaEntity(Instant.parse("2019-10-05T14:00:00Z"), BigDecimal("1.5")),
            HourlyDeltaEntity(Instant.parse("2019-10-05T16:00:00Z"), BigDecimal("2.0"))
        )
        Mockito.`when`(repo.findWindow(startHour, endHour)).thenReturn(deltas)

        val res = uc.execute(HistoryQuery(start, end))

        val expectedTimes = listOf(
            "2019-10-05T12:00:00+00:00",
            "2019-10-05T13:00:00+00:00",
            "2019-10-05T14:00:00+00:00",
            "2019-10-05T15:00:00+00:00",
            "2019-10-05T16:00:00+00:00",
            "2019-10-05T17:00:00+00:00"
        )
        val expectedAmounts = listOf("1100","1100","1101.5","1101.5","1103.5","1103.5")

        assertEquals(expectedTimes, res.map { it.datetime })
        assertEquals(expectedAmounts, res.map { it.amount.toPlainString() })
    }

    @Test
    fun `rejects start after end`() {
        val repo = Mockito.mock(HourlyDeltaRepository::class.java)
        val uc = GetBalanceHistoryUseCase(repo, BigDecimal("1000.0"))

        assertThrows(IllegalArgumentException::class.java) {
            uc.execute(
                HistoryQuery(
                    startDatetime = "2019-10-05T18:00:00+00:00",
                    endDatetime   = "2019-10-05T17:00:00+00:00"
                )
            )
        }
    }

    @Test
    fun `start equals end returns exactly one snapshot`() {
        val repo = Mockito.mock(HourlyDeltaRepository::class.java)
        val uc = GetBalanceHistoryUseCase(repo, BigDecimal("1000.0"))

        val start = "2019-10-05T14:30:00+00:00"
        val end   = "2019-10-05T14:59:59+00:00"

        val startHour = TimeUtil.floorToUtcHour(TimeUtil.parseToInstant(start))
        val endHour   = TimeUtil.floorToUtcHour(TimeUtil.parseToInstant(end))

        Mockito.`when`(repo.prefixBefore(startHour)).thenReturn(BigDecimal.ZERO)
        Mockito.`when`(repo.findWindow(startHour, endHour)).thenReturn(emptyList())

        val res = uc.execute(HistoryQuery(start, end))

        assertEquals(listOf("2019-10-05T14:00:00+00:00"), res.map { it.datetime })
        assertEquals(listOf("1000"), res.map { it.amount.toPlainString() })
    }
}
