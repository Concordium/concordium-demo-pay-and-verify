package com.concordium.payandverify

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.POST

interface Web3IdVerifierService {

    @POST("v0/verify")
    suspend fun verify(
        proofJsonBody: RequestBody,
    ): Response<ByteArray>
}
