package com.anymind.btc.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface ProjectorStateRepository : JpaRepository<ProjectorStateEntity, String>
