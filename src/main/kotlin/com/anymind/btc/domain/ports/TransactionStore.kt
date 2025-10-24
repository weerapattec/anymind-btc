package com.anymind.btc.domain.ports

import com.anymind.btc.domain.model.Transaction

interface TransactionStore {
    fun save(tx: Transaction): Transaction
}
