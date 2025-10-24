package com.anymind.btc.interfaces.web

import com.anymind.btc.application.GetBalanceHistoryUseCase
import com.anymind.btc.application.HistoryPoint
import com.anymind.btc.application.HistoryQuery
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/balances")
class HistoryControllerV1(
    private val getBalanceHistoryUseCase: GetBalanceHistoryUseCase
) {
    @GetMapping("/history")
    fun historyGet(
        @RequestParam("startDatetime") start: String,
        @RequestParam("endDatetime") end: String
    ): List<HistoryPoint> = getBalanceHistoryUseCase.execute(HistoryQuery(start, end))
}
