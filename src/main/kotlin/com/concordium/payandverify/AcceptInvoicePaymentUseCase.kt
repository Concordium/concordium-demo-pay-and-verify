package com.concordium.payandverify

import com.concordium.sdk.transactions.Transaction
import mu.KotlinLogging

class AcceptInvoicePaymentUseCase(
    private val verifyPaymentIdProofUseCase: VerifyPaymentIdProofUseCase,
    private val submitPaymentTransactionUseCase: SubmitPaymentTransactionUseCase,
    private val invoiceRepository: InvoiceRepository,
) {

    private val log = KotlinLogging.logger("AcceptInvoicePaymentUC")

    operator fun invoke(
        invoiceId: String,
        proofJson: String,
        paymentTransaction: Transaction,
    ): Result = try {
        val invoice = invoiceRepository.getInvoiceById(invoiceId)
            ?: error("Invoice $invoiceId not found")
        val paymentTransactionHash = paymentTransaction.hash.toString()

        val proofVerificationJson = verifyPaymentIdProofUseCase(
            proofRequestJson = invoice.proofRequestJson,
            proofJson = proofJson,
            paymentTransactionHash = paymentTransactionHash,
        )

        submitPaymentTransactionUseCase(paymentTransaction)

        val paidStatus = Invoice.Status.Paid(
            proofJson = proofJson,
            proofVerificationJson = proofVerificationJson,
            transactionHash = paymentTransactionHash,
        )

        invoiceRepository.updateInvoiceStatusById(
            id = invoiceId,
            newStatus = paidStatus,
        )

        log.debug {
            "invoke(): accepted:" +
                    "\ninvoiceId=$invoiceId," +
                    "\npaidStatus=$paidStatus"
        }

        Result.Accepted(
            paidStatus = paidStatus,
        )
    } catch (validationException: IllegalStateException) {
        log.error(validationException) {
            "invoke(): validation failed"
        }

        Result.Rejected(
            reason = "Payment invalid: ${validationException.message ?: validationException}"
        )
    }

    sealed interface Result {

        class Accepted(
            val paidStatus: Invoice.Status.Paid,
        ) : Result

        class Rejected(
            val reason: String,
        ) : Result
    }
}
