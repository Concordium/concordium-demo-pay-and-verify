package com.concordium.payandverify

import java.math.BigInteger

data class Invoice(
    val id: String,
    val amount: BigInteger,
    val minAgeYears: Int,
    val tokenIndex: Int,
    val recipientAccountAddress: String,
    val proofRequestJson: String,
    val status: Status,
) {

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
