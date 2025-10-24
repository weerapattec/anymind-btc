package com.anymind.btc.infrastructure.persistence

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "hourly_delta")
class HourlyDeltaEntity(
    @Id
    @Column(name = "hour_end")
    val hourEnd: Instant,

    @Column(name = "delta", nullable = false, precision = 38, scale = 8)
    val delta: BigDecimal
)
