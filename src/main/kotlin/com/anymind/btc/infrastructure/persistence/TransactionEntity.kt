package com.anymind.btc.infrastructure.persistence

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "txn")
class TransactionEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "occurred_at", nullable = false)
    val occurredAt: Instant,

    @Column(name = "recorded_at", nullable = false)
    val recordedAt: Instant = Instant.now(),

    @Column(name = "amount", nullable = false, precision = 38, scale = 8)
    val amount: BigDecimal
)
