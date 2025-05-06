package com.concordium.payandverify

import java.math.BigInteger
import java.time.Instant

data class Invoice(
    val id: String,
    val createdAt: Instant,
    val paymentDetails: PaymentDetails,
    val minAgeYears: Int,
    val proofRequestJson: String,
    val status: Status,
) {

    sealed interface PaymentDetails {

        data class Cis2(
            val amount: BigInteger,
            val tokenSymbol: String,
            val tokenDecimals: Int,
            val tokenContractIndex: Int,
            val tokenContractName: String,
            val recipientAccountAddress: String,
        ) : PaymentDetails
    }

    sealed interface Status {

        data object Pending : Status

        data class Failed(
            val reason: String,
        ) : Status

        data class Paid(
            val paidAt: Instant,
            val proofJson: String,
            val proofVerificationJson: String,
            val transactionHash: String,
            val payerAccountAddress: String,
        ) : Status
    }
}
