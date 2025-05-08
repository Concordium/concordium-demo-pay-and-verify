package com.concordium.payandverify

import io.javalin.http.Context
import okhttp3.HttpUrl
import java.math.BigDecimal

class DashboardPageController(
    private val ccdExplorerRootUrl: HttpUrl,
    private val invoiceRepository: InvoiceRepository,
) {

    fun render(context: Context) = with(context) {

        val recentlyPaidInvoices = invoiceRepository
            .getPaidInvoices()
            .sortedByDescending { (it.status as Invoice.Status.Paid).paidAt }
            .take(10)

        render(
            "dashboard.html",
            mapOf(
                "invoices" to recentlyPaidInvoices.map { invoice ->
                    val paidStatus = invoice.status as Invoice.Status.Paid

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

                    val paymentTransactionUrl = ccdExplorerRootUrl
                        .newBuilder()
                        .addPathSegment("transaction")
                        .addPathSegment(paidStatus.transactionHash)
                        .build()
                        .toString()

                    mapOf(
                        "utcPaymentTime" to paidStatus.paidAt.toString(),
                        "payerAccountAddress" to paidStatus.payerAccountAddress
                            .ellipsisMiddle(),
                        "minAgeYears" to invoice.minAgeYears,
                        "amountDecimal" to amountDecimal.toPlainString(),
                        "tokenSymbol" to tokenSymbol,
                        "paymentTransactionUrl" to paymentTransactionUrl,
                        "paymentTransactionHash" to paidStatus.transactionHash
                            .ellipsisMiddle(),
                    )
                }
            )
        )
    }

    private fun String.ellipsisMiddle(): String =
        if (length > 11)
            take(5) + "…" + takeLast(5)
        else
            this
}
