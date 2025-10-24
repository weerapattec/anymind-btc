package com.anymind.btc.application

import com.anymind.btc.domain.model.Transaction
import com.anymind.btc.domain.ports.TransactionStore
import com.anymind.btc.shared.TimeUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit

class SaveRecordUseCaseTest {

    private val store = object : TransactionStore {
        override fun save(tx: Transaction): Transaction = tx
    }

    @Test
    fun `rejects zero and negative amounts`() {
        val uc = SaveRecordUseCase(store, 0) // skew = 0 (strict)
        assertThrows(IllegalArgumentException::class.java) {
            uc.execute(SaveRecordCommand("2019-10-05T13:00:00+00:00", BigDecimal.ZERO))
        }
        assertThrows(IllegalArgumentException::class.java) {
            uc.execute(SaveRecordCommand("2019-10-05T13:00:00+00:00", BigDecimal("-0.01")))
        }
    }

    @Test
    fun `strict no-skew accepts now and rejects future`() {
        val uc = SaveRecordUseCase(store, 0)
        val now = Instant.now().truncatedTo(ChronoUnit.SECONDS)

        val ok = uc.execute(SaveRecordCommand(TimeUtil.formatUtcHour(now), BigDecimal("1.00")))
        assertEquals(now, ok.occurredAt)

        assertThrows(IllegalArgumentException::class.java) {
            uc.execute(SaveRecordCommand(TimeUtil.formatUtcHour(now.plusSeconds(5)), BigDecimal("1.00")))
        }
    }

    @Test
    fun `with skew accepts within window and rejects beyond`() {
        val skew = 60L
        val uc = SaveRecordUseCase(store, skew)
        val base = Instant.now().truncatedTo(ChronoUnit.SECONDS)

        uc.execute(SaveRecordCommand(TimeUtil.formatUtcHour(base.plusSeconds(skew - 1)), BigDecimal("1.00")))

        assertThrows(IllegalArgumentException::class.java) {
            uc.execute(SaveRecordCommand(TimeUtil.formatUtcHour(base.plusSeconds(skew + 5)).toString(), BigDecimal("1.00")))
        }
    }

    @Test
    fun `parses ISO with offset and preserves amount`() {
        val uc = SaveRecordUseCase(store, 0)
        val tx = uc.execute(SaveRecordCommand("2019-10-05T21:00:00+07:00", BigDecimal("1.23")))
        assertEquals(Instant.parse("2019-10-05T14:00:00Z"), tx.occurredAt)
        assertEquals("1.23", tx.amount.toPlainString())
    }

    @Test
    fun `invalid ISO datetime throws parse error`() {
        val uc = SaveRecordUseCase(store, 0)
        assertThrows(IllegalArgumentException::class.java) {
            uc.execute(SaveRecordCommand("not-a-timestamp", BigDecimal("1.0")))
        }
    }
}
