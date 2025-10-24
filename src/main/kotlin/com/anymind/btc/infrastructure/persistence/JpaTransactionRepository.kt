package com.anymind.btc.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface JpaTransactionRepository : JpaRepository<TransactionEntity, UUID>
