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
    private val invoiceRepository: InvoiceRepository,
) {

    fun render(context: Context) = with(context) {

        val invoiceId = pathParam("invoiceId")
        val invoice = invoiceRepository
            .getInvoiceById(invoiceId)
            ?: throw NotFoundResponse("Invoice $invoiceId not found")

        when (invoice.status) {
            Invoice.Status.Pending ->
                renderPending(
                    invoice = invoice,
                )

            is Invoice.Status.Paid ->
                TODO()

            is Invoice.Status.Failed ->
                TODO()
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
}
