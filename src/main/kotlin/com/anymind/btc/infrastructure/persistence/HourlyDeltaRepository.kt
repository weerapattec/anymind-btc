package com.anymind.btc.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.time.Instant

interface HourlyDeltaRepository : JpaRepository<HourlyDeltaEntity, Instant> {
    @Query("""
        SELECT COALESCE(SUM(h.delta), 0)
        FROM HourlyDeltaEntity h
        WHERE h.hourEnd < :startExclusive
    """)
    fun prefixBefore(@Param("startExclusive") startExclusive: Instant): BigDecimal?

    @Query("""
        SELECT h FROM HourlyDeltaEntity h
        WHERE h.hourEnd BETWEEN :startInclusive AND :endInclusive
        ORDER BY h.hourEnd ASC
    """)
    fun findWindow(
        @Param("startInclusive") startInclusive: Instant,
        @Param("endInclusive") endInclusive: Instant
    ): List<HourlyDeltaEntity>
}
