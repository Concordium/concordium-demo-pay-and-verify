package com.concordium.payandverify

import io.javalin.http.Context
import io.javalin.http.NotFoundResponse
import okhttp3.HttpUrl
import java.math.BigDecimal

class InvoicePageController(
    private val ccdExplorerRootUrl: HttpUrl,
    private val invoiceRepository: InvoiceRepository,
) {

    fun render(context: Context) = with(context) {

        val invoiceId = pathParam("id")
        val invoice = invoiceRepository.getInvoiceById(invoiceId)
            ?: throw NotFoundResponse("Invoice $invoiceId not found")

        when (val status = invoice.status) {
            Invoice.Status.Pending -> {
                result("Pending")
            }

            is Invoice.Status.Failed -> {
                result("Failed: ${status.reason}")
            }

            is Invoice.Status.Paid -> {
                val paymentTransactionUrl = ccdExplorerRootUrl
                    .newBuilder()
                    .addPathSegment("transaction")
                    .addPathSegment(status.transactionHash)
                    .build()
                    .toString()

                val amountDecimal: BigDecimal
                val tokenSymbol: String

                when (val paymentDetails = invoice.paymentDetails) {
                    is Invoice.PaymentDetails.Cis2 -> {
                        amountDecimal = paymentDetails.amount
                            .toBigDecimal()
                            .movePointLeft(paymentDetails.tokenDecimals)
                        tokenSymbol = paymentDetails.tokenSymbol
                    }
                }

                render(
                    "invoice_paid.html",
                    mapOf(
                        "paymentTransactionUrl" to paymentTransactionUrl,
                        "invoiceId" to invoiceId,
                        "amountDecimal" to amountDecimal.toPlainString(),
                        "tokenSymbol" to tokenSymbol,
                        "minAgeYears" to invoice.minAgeYears,
                    )
                )
            }
        }
    }
}
