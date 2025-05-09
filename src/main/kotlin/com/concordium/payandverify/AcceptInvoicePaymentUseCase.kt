package com.concordium.payandverify

import com.concordium.sdk.crypto.wallet.web3Id.QualifiedRequest
import com.concordium.sdk.crypto.wallet.web3Id.Statement.RangeStatement
import com.concordium.sdk.crypto.wallet.web3Id.Statement.RequestStatement
import com.concordium.sdk.serializing.JsonMapper
import com.concordium.sdk.transactions.AccountTransaction
import com.concordium.sdk.transactions.BlockItem
import com.concordium.sdk.transactions.RawTransaction
import mu.KotlinLogging
import java.nio.ByteBuffer
import java.time.Instant

class AcceptInvoicePaymentUseCase(
    private val verifyPaymentIdProofUseCase: VerifyPaymentIdProofUseCase,
    private val submitPaymentTransactionUseCase: SubmitPaymentTransactionUseCase,
    private val invoiceRepository: InvoiceRepository,
) {

    private val log = KotlinLogging.logger("AcceptInvoicePaymentUC")

    operator fun invoke(
        invoiceId: String,
        proofJson: String,
        paymentTransaction: RawTransaction,
    ): Result = try {
        val invoice = invoiceRepository.getInvoiceById(invoiceId)
            ?: error("Invoice $invoiceId not found")
        val paymentTransactionHash = paymentTransaction.hash.toString()
        val payerAccountAddress = try {
            (BlockItem.fromVersionedBytes(ByteBuffer.wrap(paymentTransaction.versionedBytes)) as AccountTransaction)
                .sender
                .encoded()
        } catch (e: Exception) {
            error("Failed decoding sender address from the paid transaction")
        }

        val proofVerificationResult: QualifiedRequest = verifyPaymentIdProofUseCase(
            proofRequestJson = invoice.proofRequestJson,
            proofJson = proofJson,
            paymentTransactionHash = paymentTransactionHash,
        )

        submitPaymentTransactionUseCase(paymentTransaction)

        val paidStatus = Invoice.Status.Paid(
            paidAt = Instant.now(),
            proofJson = proofJson,
            proofVerificationJson = JsonMapper.INSTANCE.writeValueAsString(proofVerificationResult),
            transactionHash = paymentTransactionHash,
            payerAccountAddress = payerAccountAddress,
            proofSummary = proofVerificationResult
                .credentialStatements
                .flatMap(RequestStatement::getStatement)
                .joinToString(
                    transform = { statement ->
                        when (statement) {
                            is RangeStatement ->
                                "${statement.attributeTag} before ${statement.upper.value}"

                            else ->
                                JsonMapper.INSTANCE.writeValueAsString(statement)
                        }
                    }
                )
        )

        invoiceRepository.updateInvoiceStatusById(
            id = invoiceId,
            newStatus = paidStatus,
        )

        log.debug {
            "invoke(): accepted:" +
                    "\ninvoiceId=$invoiceId," +
                    "\npaidStatus=$paidStatus," +
                    "\npayerAccountAddress=$payerAccountAddress"
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
