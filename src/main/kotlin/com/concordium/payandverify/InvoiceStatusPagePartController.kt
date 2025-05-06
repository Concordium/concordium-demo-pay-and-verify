package com.concordium.payandverify

import io.javalin.http.Context
import io.javalin.http.NotFoundResponse

class InvoiceStatusPagePartController(
    private val invoiceRepository: InvoiceRepository,
) {

    fun render(context: Context) = with(context) {

        val invoiceId = pathParam("id")
        val invoice = invoiceRepository
            .getInvoiceById(invoiceId)
            ?: throw NotFoundResponse("Invoice $invoiceId not found")

        when (val status = invoice.status) {
            Invoice.Status.Pending -> {
                result("Pending")
            }

            is Invoice.Status.Paid -> {
                header("HX-Redirect", "/success")
                result("Paid, redirecting")
            }

            is Invoice.Status.Failed -> {
                result("Rejected: ${status.reason}")
            }
        }
    }
}
