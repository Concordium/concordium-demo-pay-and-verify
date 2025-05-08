package com.concordium.payandverify

import com.fasterxml.jackson.databind.JsonNode
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface Web3IdVerifierService {

    @POST("v0/verify")
    suspend fun verify(
        @Body
        proofJsonBody: RequestBody,
    ): Response<JsonNode>
}
