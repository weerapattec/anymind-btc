package com.anymind.btc.application

import com.anymind.btc.infrastructure.persistence.HourlyDeltaRepository
import com.anymind.btc.shared.TimeUtil
import com.anymind.btc.shared.compact
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant

data class HistoryPoint(val datetime: String, val amount: BigDecimal)

data class HistoryQuery(
    val startDatetime: String,
    val endDatetime: String
)

@Service
class GetBalanceHistoryUseCase(
    private val hourlyRepo: HourlyDeltaRepository,
    @Value("\${app.initial-balance}") private val initial: BigDecimal
) {
    fun execute(q: HistoryQuery): List<HistoryPoint> {
        val start = TimeUtil.parseToInstant(q.startDatetime)
        val end = TimeUtil.parseToInstant(q.endDatetime)
        require(!start.isAfter(end)) { "startDatetime must be <= endDatetime" }

        val startHour = TimeUtil.floorToUtcHour(start)
        val endHour = TimeUtil.floorToUtcHour(end)

        val prefix = hourlyRepo.prefixBefore(startHour) ?: BigDecimal.ZERO
        val deltas = hourlyRepo.findWindow(startHour, endHour)

        val out = mutableListOf<HistoryPoint>()
        var running = initial.add(prefix)
        var h: Instant = startHour
        var i = 0
        while (!h.isAfter(endHour)) {
            while (i < deltas.size && !deltas[i].hourEnd.isAfter(h)) {
                running = running.add(deltas[i].delta)
                i++
            }
            out += HistoryPoint(TimeUtil.formatUtcHour(h), running.compact())
            h = h.plus(Duration.ofHours(1))
        }
        return out
    }
}
