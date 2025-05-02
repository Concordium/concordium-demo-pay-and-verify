package com.concordium.payandverify

import com.concordium.sdk.crypto.wallet.web3Id.QualifiedRequest
import com.concordium.sdk.crypto.wallet.web3Id.Statement.IdQualifier
import com.concordium.sdk.crypto.wallet.web3Id.UnqualifiedRequest
import com.concordium.sdk.serializing.JsonMapper
import io.javalin.http.HttpStatus
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class VerifyPaymentIdProofUseCase(
    private val web3IdVerifierService: Web3IdVerifierService,
) {

    /**
     * @return proof verification result JSON (qualified request) if it is valid.
     */
    operator fun invoke(
        proofRequestJson: String,
        paymentTransactionHash: String,
        proofJson: String,
    ): String {
        val proofVerificationResponse = runBlocking {
            web3IdVerifierService.verify(
                proofJsonBody = proofJson.toRequestBody(
                    contentType = "application/json".toMediaType(),
                )
            )
        }

        if (!proofVerificationResponse.isSuccessful) {
            val errorBodyBytes = proofVerificationResponse.errorBody()?.bytes()
            val errorString =
                if (proofVerificationResponse.code() == HttpStatus.BAD_REQUEST.code
                    && errorBodyBytes != null
                )
                    String(errorBodyBytes)
                else
                    "Unsuccessful verification result: ${proofVerificationResponse.code()}"

            error("Proof verification failed: $errorString")
        }

        val proofVerificationResponseBodyBytes = proofVerificationResponse.body()
            ?: error("Proof verification failed: Verification result has no body")

        val unqualifiedRequest = try {
            JsonMapper.INSTANCE
                .readValue(proofRequestJson, UnqualifiedRequest::class.java)
        } catch (e: Exception) {
            error("Failed to decode proof request JSON: $e")
        }

        val qualifiedRequest = try {
            JsonMapper.INSTANCE
                .readValue(proofVerificationResponseBodyBytes, QualifiedRequest::class.java)
        } catch (e: Exception) {
            error("Failed to decode proof verification response JSON: $e")
        }

        check(
            unqualifiedRequest.credentialStatements
                .zip(qualifiedRequest.credentialStatements)
                .all { (requestedStatement, provenStatement) ->
                    JsonMapper.INSTANCE.writeValueAsString(requestedStatement.statement) ==
                            JsonMapper.INSTANCE.writeValueAsString(provenStatement.statement)
                            && requestedStatement.idQualifier is IdQualifier
                }
        ) {
            "Provided proof doesn't match the requested one"
        }

        check(qualifiedRequest.challenge == paymentTransactionHash) {
            "Provided proof doesn't have the payment transaction hash in it's challenge"
        }

        return String(proofVerificationResponseBodyBytes)
    }
}
