package com.concordium.payandverify

import com.concordium.sdk.transactions.Transaction
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class SubmitPaymentTransactionUseCase(
    private val walletProxyService: WalletProxyService,
) {

    operator fun invoke(
        paymentTransaction: Transaction,
    ) = runBlocking {

        walletProxyService.submitRawTransaction(
            transactionBytesBody = paymentTransaction
                .bytes
                .toRequestBody(
                    contentType = "application/octet-stream".toMediaType(),
                )
        )
    }
}
