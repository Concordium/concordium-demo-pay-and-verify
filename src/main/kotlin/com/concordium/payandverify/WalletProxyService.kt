package com.concordium.payandverify

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.PUT

interface WalletProxyService {

    @PUT("v0/submitRawTransaction")
    suspend fun submitRawTransaction(@Body transactionBytesBody: RequestBody): Any
}
