package com.anymind.btc.infrastructure.persistence

import com.anymind.btc.domain.model.Transaction
import com.anymind.btc.domain.ports.TransactionStore
import org.springframework.stereotype.Component

@Component
class TransactionStoreAdapter(
    private val repo: JpaTransactionRepository
) : TransactionStore {
    override fun save(tx: Transaction): Transaction {
        val saved = repo.save(TransactionEntity(
            id = tx.id,
            occurredAt = tx.occurredAt,
            amount = tx.amount
        ))
        return Transaction(
            id = saved.id,
            occurredAt = saved.occurredAt,
            amount = saved.amount
        )
    }
}
