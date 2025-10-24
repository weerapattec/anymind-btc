package com.anymind.btc.it

import com.anymind.btc.infrastructure.persistence.ProjectorScheduler
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    class KPostgres : PostgreSQLContainer<KPostgres>("postgres:16")

    companion object {
        @Container
        @JvmStatic
        val postgres = KPostgres()
            .withDatabaseName("anymind")
            .withUsername("anymind")
            .withPassword("anymind")

        @JvmStatic
        @DynamicPropertySource
        fun overrideProps(reg: DynamicPropertyRegistry) {
            reg.add("spring.datasource.url") { postgres.jdbcUrl }
            reg.add("spring.datasource.username") { postgres.username }
            reg.add("spring.datasource.password") { postgres.password }
            // deterministic test run: disable the scheduled projector
            reg.add("app.projector.enabled") { "false" }
        }
    }

    @Autowired lateinit var mvc: MockMvc
    @Autowired lateinit var json: ObjectMapper
    @Autowired lateinit var projector: ProjectorScheduler

    data class HistoryPoint(val datetime: String, val amount: BigDecimal)

    @Test
    fun `save record then history returns expected hourly series`() {
        val savePayload = mapOf(
            "datetime" to "2019-10-05T14:45:05+07:00",
            "amount" to 1.1
        )
        mvc.perform(
            post("/api/v1/records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(savePayload))
        ).andExpect(status().isCreated)

        projector.processBatch()

        val res = mvc.perform(
            get("/api/v1/balances/history")
                .param("startDatetime", "2019-10-05T06:48:01+00:00")
                .param("endDatetime", "2019-10-05T11:48:02+00:00")
        ).andExpect(status().isOk)
            .andReturn()

        val list: List<HistoryPoint> = json.readValue(
            res.response.contentAsString,
            object : TypeReference<List<HistoryPoint>>() {}
        )

        val expectedTimes = listOf(
            "2019-10-05T06:00:00+00:00",
            "2019-10-05T07:00:00+00:00",
            "2019-10-05T08:00:00+00:00",
            "2019-10-05T09:00:00+00:00",
            "2019-10-05T10:00:00+00:00",
            "2019-10-05T11:00:00+00:00",
        )
        val expectedAmounts = listOf("1000","1000","1001.1","1001.1","1001.1","1001.1").map(::BigDecimal)

        assertEquals(expectedTimes, list.map { it.datetime })
        assertEquals(expectedAmounts, list.map { it.amount })
    }

    @Test
    fun `invalid datetime string returns 400`() {
        mvc.perform(
            get("/api/v1/balances/history")
                .param("startDatetime", "invalid_datetime")
                .param("endDatetime", "2019-10-05T11:48:02+00:00")
        ).andExpect(status().isBadRequest)

        val savePayload = mapOf(
            "datetime" to "invalid_datetime",
            "amount" to "invalid_amount",
        )
        mvc.perform(
            post("/api/v1/records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(savePayload))
        ).andExpect(status().isBadRequest)
    }
}
