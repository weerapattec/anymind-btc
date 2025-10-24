package com.anymind.btc.application

import com.anymind.btc.domain.model.Transaction
import com.anymind.btc.domain.ports.TransactionStore
import com.anymind.btc.shared.TimeUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant

data class SaveRecordCommand(
    val datetimeIso: String,
    val amount: BigDecimal
)

@Service
class SaveRecordUseCase(
    private val store: TransactionStore,
    @Value("\${app.future-skew-seconds:120}") private val skewSeconds: Long
) {
    @Transactional
    fun execute(cmd: SaveRecordCommand): Transaction {
        require(cmd.amount > BigDecimal.ZERO) { "amount must be > 0" }
        val occurredAt = TimeUtil.parseToInstant(cmd.datetimeIso)
        val now = Instant.now()
        require(!occurredAt.isAfter(now.plusSeconds(skewSeconds))) {
            "datetime is too far in the future"
        }
        return store.save(Transaction(occurredAt = occurredAt, amount = cmd.amount))
    }
}
