package com.concordium.payandverify

import io.javalin.http.Context
import java.math.BigInteger

class IndexPageController(
    private val createInvoiceUseCase: CreateInvoiceUseCase,
) {

    fun render(context: Context) = with(context) {

        val invoice = createInvoiceUseCase(
            amount = BigInteger.TEN,
            minAgeYears = 18,
        )

        render(
            "index.html",
            mapOf(
                "invoiceStatusPartUrl" to "/invoices/${invoice.id}",
                "invoiceId" to invoice.id,
            )
        )
    }
}
