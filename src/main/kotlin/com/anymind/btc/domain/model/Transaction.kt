package com.anymind.btc.domain.model

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class Transaction(
    val id: UUID = UUID.randomUUID(),
    val occurredAt: Instant,
    val amount: BigDecimal
)
