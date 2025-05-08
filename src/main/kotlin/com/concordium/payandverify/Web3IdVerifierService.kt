package com.concordium.payandverify

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface Web3IdVerifierService {

    @POST("v0/verify")
    fun verify(
        @Body
        proofJsonBody: RequestBody,
    ): Call<ResponseBody>
}
