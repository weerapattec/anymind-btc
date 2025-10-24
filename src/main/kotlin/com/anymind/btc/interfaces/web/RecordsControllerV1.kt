package com.anymind.btc.interfaces.web

import com.anymind.btc.application.SaveRecordCommand
import com.anymind.btc.application.SaveRecordUseCase
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

data class SaveRecordRequest(
    @field:NotNull val datetime: String,
    @field:Positive val amount: BigDecimal
)

data class SaveRecordResponse(val id: String, val status: String = "ok")

@RestController
@RequestMapping("/api/v1/records")
class RecordsControllerV1(
    private val saveRecordUseCase: SaveRecordUseCase
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun saveRecordV1(@Valid @RequestBody req: SaveRecordRequest): SaveRecordResponse {
        val saved = saveRecordUseCase.execute(SaveRecordCommand(req.datetime, req.amount))
        return SaveRecordResponse(id = saved.id.toString())
    }
}
