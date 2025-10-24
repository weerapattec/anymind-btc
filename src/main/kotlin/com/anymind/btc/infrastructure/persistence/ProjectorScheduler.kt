package com.anymind.btc.infrastructure.persistence

import com.anymind.btc.shared.TimeUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.Instant
import java.util.*

@Component
class ProjectorScheduler(
    private val jdbc: JdbcTemplate,
    private val stateRepo: ProjectorStateRepository,
    @Value("\${app.projector.enabled:true}") private val enabled: Boolean,
    @Value("\${app.projector.batch-size:1000}") private val batchSize: Int
) {
    private val component = "hourly_delta"
    private val lockKey: Long = 424242424242L

    @Scheduled(fixedDelayString = "\${app.projector.interval-ms:1000}")
    fun tick() {
        if (!enabled) return
        val gotLock = jdbc.queryForObject("SELECT pg_try_advisory_lock(?)", Boolean::class.java, lockKey)
        if (gotLock != true) return
        try { processBatch() } finally { jdbc.execute("SELECT pg_advisory_unlock($lockKey)") }
    }

    @Transactional
    fun processBatch() {
        val state = stateRepo.findById(component).orElseGet {
            ProjectorStateEntity(component, Instant.EPOCH, null)
        }

        val rows = jdbc.query(
            """
            SELECT id, occurred_at, recorded_at, amount
            FROM txn
            WHERE (recorded_at, id) > (?, ?)
            ORDER BY recorded_at, id
            LIMIT ?
            """.trimIndent(),
            { rs, _ ->
                mapOf(
                    "id" to UUID.fromString(rs.getString("id")),
                    "occurred_at" to rs.getTimestamp("occurred_at").toInstant(),
                    "recorded_at" to rs.getTimestamp("recorded_at").toInstant(),
                    "amount" to rs.getBigDecimal("amount")
                )
            },
            Timestamp.from(state.lastRecordedAt), state.lastId, batchSize
        )
        if (rows.isEmpty()) return

        val aggregates = mutableMapOf<Instant, java.math.BigDecimal>()
        for (r in rows) {
            val occurred = r["occurred_at"] as Instant
            val hour = TimeUtil.ceilToUtcHour(occurred)
            val amt = r["amount"] as java.math.BigDecimal
            aggregates.merge(hour, amt, java.math.BigDecimal::add)
        }

        val sql = """
            INSERT INTO hourly_delta(hour_end, delta)
            VALUES (?, ?)
            ON CONFLICT (hour_end) DO UPDATE
              SET delta = hourly_delta.delta + EXCLUDED.delta
        """.trimIndent()
        jdbc.batchUpdate(sql, aggregates.entries.toList(), aggregates.size) { ps, entry ->
            ps.setTimestamp(1, Timestamp.from(entry.key))
            ps.setBigDecimal(2, entry.value)
        }

        val last = rows.last()
        state.lastRecordedAt = last["recorded_at"] as Instant
        state.lastId = last["id"] as UUID
        stateRepo.save(state)
    }
}
