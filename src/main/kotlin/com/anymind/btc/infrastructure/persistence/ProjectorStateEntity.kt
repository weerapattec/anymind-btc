package com.anymind.btc.infrastructure.persistence

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "projector_state")
class ProjectorStateEntity(
    @Id
    val component: String,
    @Column(name = "last_recorded_at", nullable = false)
    var lastRecordedAt: Instant,
    @Column(name = "last_id")
    var lastId: UUID?
)
