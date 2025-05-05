package com.concordium.payandverify

import io.javalin.http.Context
import io.javalin.http.NotFoundResponse

class InvoiceApiV1Controller(
    private val invoiceRepository: InvoiceRepository,
) {

    fun getInvoiceById(context: Context) = with(context) {
        val invoiceId = pathParam("id")
        val invoice = invoiceRepository.getInvoiceById(invoiceId)
            ?: throw NotFoundResponse("Invoice $invoiceId not found")

        val cis2PaymentDetails = invoice.paymentDetails as? Invoice.PaymentDetails.Cis2

        json(
            InvoiceResponse(
                id = invoice.id,
                minAgeYears = invoice.minAgeYears,
                proofRequestJson = invoice.proofRequestJson,
                status = when (invoice.status) {
                    is Invoice.Status.Failed -> "failed"
                    is Invoice.Status.Paid -> "paid"
                    Invoice.Status.Pending -> "pending"
                },
                paymentType = when (invoice.paymentDetails) {
                    is Invoice.PaymentDetails.Cis2 -> "cis2"
                },
                cis2Amount = cis2PaymentDetails?.amount?.toString(),
                cis2TokenContractName = cis2PaymentDetails?.tokenContractName,
                cis2TokenContractSymbol = cis2PaymentDetails?.tokenSymbol,
                cis2TokenDecimals = cis2PaymentDetails?.tokenDecimals,
                cis2TokenContractIndex = cis2PaymentDetails?.tokenIndex,
            )
        )
    }

    private class InvoiceResponse(
        val version: Int = 1,
        val id: String,
        val status: String,
        val minAgeYears: Int,
        val proofRequestJson: String,
        val paymentType: String,
        val cis2Amount: String?,
        val cis2TokenContractIndex: Int?,
        val cis2TokenContractName: String?,
        val cis2TokenContractSymbol: String?,
        val cis2TokenDecimals: Int?,
    )
}
