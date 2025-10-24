package com.anymind.btc.application

import com.anymind.btc.domain.model.Transaction
import com.anymind.btc.domain.ports.TransactionStore
import com.anymind.btc.shared.TimeUtil
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class InMemoryStore : TransactionStore {
    override fun save(tx: Transaction): Transaction = tx
}

class FutureSkewTest {
    @Test
    fun rejects_far_future_event() {
        val uc = SaveRecordUseCase(InMemoryStore(), 0)
        val futureIso = TimeUtil.formatUtcHour(Instant.now().plusSeconds(10))
        assertThrows(IllegalArgumentException::class.java) {
            uc.execute(SaveRecordCommand(datetimeIso = futureIso, amount = BigDecimal("1.0")))
        }
    }
}
