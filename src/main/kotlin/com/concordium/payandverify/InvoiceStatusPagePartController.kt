package com.concordium.payandverify

import io.javalin.http.Context
import io.javalin.http.NotFoundResponse
import okhttp3.HttpUrl
import qrcode.QRCode
import qrcode.raw.ErrorCorrectionLevel
import java.net.URLEncoder
import java.util.*

class InvoiceStatusPagePartController(
    private val publicRootUrl: HttpUrl,
    private val ccdExplorerRootUrl: HttpUrl,
    private val invoiceRepository: InvoiceRepository,
) {

    fun render(context: Context) = with(context) {

        val invoiceId = pathParam("invoiceId")
        val invoice = invoiceRepository
            .getInvoiceById(invoiceId)
            ?: throw NotFoundResponse("Invoice $invoiceId not found")

        when (val status = invoice.status) {
            Invoice.Status.Pending ->
                renderPending(
                    invoice = invoice,
                )

            is Invoice.Status.Paid ->
                renderPaid(
                    invoice = invoice,
                    paidStatus = status,
                )

            is Invoice.Status.Failed ->
                renderFailed(
                    failedStatus = status,
                )
        }
    }

    private fun Context.renderPending(
        invoice: Invoice,
    ) {
        val invoiceApiUrl = publicRootUrl
            .newBuilder()
            .addPathSegments("api/v1/invoices")
            .addPathSegment(invoice.id)
            .build()
            .toString()

        val walletUri = "cryptoxmwallet-testnet://demo_pay_and_verify?invoice=" +
                URLEncoder.encode(invoiceApiUrl, Charsets.UTF_8)

        val walletUriQrBase64 = QRCode
            .ofSquares()
            .withInnerSpacing(0)
            .withErrorCorrectionLevel(ErrorCorrectionLevel.LOW)
            .build(
                data = walletUri,
            )
            .renderToBytes()
            .let { "data:image/png;base64, " + Base64.getMimeEncoder().encodeToString(it) }

        render(
            "invoice_pending.html",
            mapOf(
                "invoiceId" to invoice.id,
                "amountDecimal" to invoice.amount.toString(),
                "minAgeYears" to invoice.minAgeYears,
                "walletUri" to walletUri,
                "walletUriQrBase64" to walletUriQrBase64,
            )
        )
    }

    private fun Context.renderPaid(
        invoice: Invoice,
        paidStatus: Invoice.Status.Paid,
    ) {
        val paymentTransactionUrl = ccdExplorerRootUrl
            .newBuilder()
            .addPathSegment("transaction")
            .addPathSegment(paidStatus.transactionHash)

        // Send special status code to stop polling.
        status(286)
        render(
            "invoice_paid.html",
            mapOf(
                "amountDecimal" to invoice.amount.toString(),
                "minAgeYears" to invoice.minAgeYears,
                "paymentTransactionUrl" to paymentTransactionUrl,
                "proofVerificationJson" to paidStatus.proofVerificationJson,
                "proofJson" to paidStatus.proofJson,
            )
        )
    }

    private fun Context.renderFailed(
        failedStatus: Invoice.Status.Failed,
    ) {
        // Send special status code to stop polling.
        status(286)
        render(
            "invoice_failed.html",
            mapOf(
                "reason" to failedStatus.reason,
            )
        )
    }
}
