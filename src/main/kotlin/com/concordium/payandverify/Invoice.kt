package com.concordium.payandverify

import java.math.BigInteger

data class Invoice(
    val id: String,
    val paymentDetails: PaymentDetails,
    val minAgeYears: Int,
    val proofRequestJson: String,
    val status: Status,
) {

    sealed interface PaymentDetails {

        data class Cis2(
            val amount: BigInteger,
            val tokenIndex: Int,
            val tokenSymbol: String,
            val tokenContractName: String,
            val tokenDecimals: Int,
            val recipientAccountAddress: String,
        ) : PaymentDetails
    }

    sealed interface Status {

        data object Pending : Status

        data class Failed(
            val reason: String,
        ) : Status

        data class Paid(
            val proofJson: String,
            val proofVerificationJson: String,
            val transactionHash: String,
        ) : Status
    }
}
