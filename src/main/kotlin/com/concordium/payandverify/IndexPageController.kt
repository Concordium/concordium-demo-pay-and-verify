package com.concordium.payandverify

import io.javalin.http.Context
import okhttp3.HttpUrl
import qrcode.QRCode
import qrcode.raw.ErrorCorrectionLevel
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URLEncoder
import java.util.*

class IndexPageController(
    private val publicRootUrl: HttpUrl,
    private val createCis2InvoiceUseCase: CreateCis2InvoiceUseCase,
) {

    fun render(context: Context) = with(context) {

        val invoice = createCis2InvoiceUseCase(
            amount = BigInteger.TEN,
            minAgeYears = 18,
        )

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
            "index.html",
            mapOf(
                "invoiceId" to invoice.id,
                "amountDecimal" to amountDecimal.toPlainString(),
                "tokenSymbol" to tokenSymbol,
                "minAgeYears" to invoice.minAgeYears,
                "walletUri" to walletUri,
                "walletUriQrBase64" to walletUriQrBase64,
            )
        )
    }
}
