package com.concordium.payandverify

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.javalin.http.HttpStatus
import okhttp3.RequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.buffer
import okio.sink
import org.junit.Assert
import org.junit.Test
import retrofit2.Response
import java.io.ByteArrayOutputStream

class VerifyPaymentIdProofUseCaseTest {

    private fun getVerifier(
        expectedRequest: String,
        response: Response<JsonNode>,
    ) = object : Web3IdVerifierService {

        override suspend fun verify(proofJsonBody: RequestBody): Response<JsonNode> {

            val requestBodyBytes = ByteArrayOutputStream()
                .also { stream ->
                    val buffer = stream.sink().buffer()
                    proofJsonBody.writeTo(buffer)
                    buffer.flush()
                }
                .toByteArray()

            Assert.assertEquals(
                "Verifier service must be called with the proof JSON",
                expectedRequest,
                String(requestBodyBytes),
            )

            return response
        }
    }

    @Test
    fun verify_IfValid() {

        val proofJson = """
            { "proof": true }
        """.trimIndent()

        val proofRequestJson = """
                {
                   "challenge":"payment-transaction-hash",
                   "credentialStatements":[
                      {
                         "statement":[
                            {
                               "type":"AttributeInRange",
                               "attributeTag":"dob",
                               "lower":"18000101",
                               "upper":"20070503"
                            }
                         ],
                         "idQualifier":{
                            "type":"cred",
                            "issuers":[
                               0,
                               1,
                               4
                            ]
                         }
                      }
                   ]
                }
            """.trimIndent()

        val verificationResultJson = """
                {
                  "block" : "aa8d816cdf2790a9cd1857691cc7491d27e0e85f31f902c80ca90539a69604be",
                  "blockTime" : "2025-05-02T13:55:57.615Z",
                  "challenge" : "4f93b7a5eb91228da1ce07c9da3189dc9ce04f1897d36ae9ddc3129a1bdeee78",
                  "credentialStatements" : [ {
                    "id" : "did:ccd:testnet:cred:b5feb5b11e5d664808a4b656fa90828ef0978a374ec3c608b4cc2029492f3e1305584691c4de2ac04048fa6e4b070dba",
                    "statement" : [ {
                      "attributeTag" : "dob",
                      "lower" : "18000101",
                      "type" : "AttributeInRange",
                      "upper" : "20070503"
                    } ]
                  } ]
                }
                """
            .trimIndent()
            .let(jacksonObjectMapper()::readTree)


        val verifier = getVerifier(
            expectedRequest = proofJson,
            response = verificationResultJson
                .let { Response.success(it) },
        )

        val resultJson = VerifyPaymentIdProofUseCase(
            web3IdVerifierService = verifier,
        )
            .invoke(
                proofRequestJson = proofRequestJson,
                proofJson = proofJson,
                paymentTransactionHash = "4f93b7a5eb91228da1ce07c9da3189dc9ce04f1897d36ae9ddc3129a1bdeee78",
            )

        Assert.assertEquals(
            "Returned JSON must be the one from the verifier",
            verificationResultJson,
            resultJson.let(jacksonObjectMapper()::readTree)
        )
    }

    @Test(expected = IllegalStateException::class)
    fun verify_FailsIfNotValid() {
        val proofJson = """
            { "proof": true }
        """.trimIndent()

        val proofRequestJson = """
                {
                   "challenge":"payment-transaction-hash",
                   "credentialStatements":[
                      {
                         "statement":[
                            {
                               "type":"AttributeInRange",
                               "attributeTag":"dob",
                               "lower":"18000101",
                               "upper":"20060503"
                            }
                         ],
                         "idQualifier":{
                            "type":"cred",
                            "issuers":[
                               0,
                               1,
                               4
                            ]
                         }
                      }
                   ]
                }
            """.trimIndent()

        val verifier = getVerifier(
            expectedRequest = proofJson,
            response = Response.error(
                HttpStatus.BAD_REQUEST.code,
                "Proof not OK".toResponseBody()
            ),
        )

        VerifyPaymentIdProofUseCase(
            web3IdVerifierService = verifier,
        )
            .invoke(
                proofRequestJson = proofRequestJson,
                proofJson = proofJson,
                paymentTransactionHash = "4f93b7a5eb91228da1ce07c9da3189dc9ce04f1897d36ae9ddc3129a1bdeee78",
            )
    }

    @Test(expected = IllegalStateException::class)
    fun verify_FailsIfValidButDifferent() {

        val proofJson = """
            { "proof": true }
        """.trimIndent()

        val proofRequestJson = """
                {
                   "challenge":"payment-transaction-hash",
                   "credentialStatements":[
                      {
                         "statement":[
                            {
                               "type":"AttributeInRange",
                               "attributeTag":"dob",
                               "lower":"18000101",
                               "upper":"20010503"
                            }
                         ],
                         "idQualifier":{
                            "type":"cred",
                            "issuers":[
                               0,
                               1,
                               4
                            ]
                         }
                      }
                   ]
                }
            """.trimIndent()

        val verificationResultJson = """
                {
                    "block": "aa8d816cdf2790a9cd1857691cc7491d27e0e85f31f902c80ca90539a69604be",
                    "blockTime": "2025-05-02T13:55:57.615Z",
                    "challenge": "4f93b7a5eb91228da1ce07c9da3189dc9ce04f1897d36ae9ddc3129a1bdeee78",
                    "credentialStatements": [
                        {
                            "id": "did:ccd:testnet:cred:b5feb5b11e5d664808a4b656fa90828ef0978a374ec3c608b4cc2029492f3e1305584691c4de2ac04048fa6e4b070dba",
                            "statement": [
                                {
                                    "attributeTag": "dob",
                                    "lower": "18000101",
                                    "type": "AttributeInRange",
                                    "upper": "20070503"
                                }
                            ]
                        }
                    ]
                }
                """.trimIndent()

        val verifier = getVerifier(
            expectedRequest = proofJson,
            response = verificationResultJson
                .let(jacksonObjectMapper()::readTree)
                .let { Response.success(it) },
        )

        VerifyPaymentIdProofUseCase(
            web3IdVerifierService = verifier,
        )
            .invoke(
                proofRequestJson = proofRequestJson,
                proofJson = proofJson,
                paymentTransactionHash = "4f93b7a5eb91228da1ce07c9da3189dc9ce04f1897d36ae9ddc3129a1bdeee78",
            )
    }

    @Test(expected = IllegalStateException::class)
    fun verify_FailsIfTransactionHashMismatch() {

        val proofJson = """
            { "proof": true }
        """.trimIndent()

        val proofRequestJson = """
                {
                   "challenge":"payment-transaction-hash",
                   "credentialStatements":[
                      {
                         "statement":[
                            {
                               "type":"AttributeInRange",
                               "attributeTag":"dob",
                               "lower":"18000101",
                               "upper":"20070503"
                            }
                         ],
                         "idQualifier":{
                            "type":"cred",
                            "issuers":[
                               0,
                               1,
                               4
                            ]
                         }
                      }
                   ]
                }
            """.trimIndent()

        val verificationResultJson = """
                {
                    "block": "aa8d816cdf2790a9cd1857691cc7491d27e0e85f31f902c80ca90539a69604be",
                    "blockTime": "2025-05-02T13:55:57.615Z",
                    "challenge": "4f93b7a5eb91228da1ce07c9da3189dc9ce04f1897d36ae9ddc3129a1bdeee78",
                    "credentialStatements": [
                        {
                            "id": "did:ccd:testnet:cred:b5feb5b11e5d664808a4b656fa90828ef0978a374ec3c608b4cc2029492f3e1305584691c4de2ac04048fa6e4b070dba",
                            "statement": [
                                {
                                    "attributeTag": "dob",
                                    "lower": "18000101",
                                    "type": "AttributeInRange",
                                    "upper": "20070503"
                                }
                            ]
                        }
                    ]
                }
                """.trimIndent()

        val verifier = getVerifier(
            expectedRequest = proofJson,
            response = verificationResultJson
                .let(jacksonObjectMapper()::readTree)
                .let { Response.success(it) },
        )

        VerifyPaymentIdProofUseCase(
            web3IdVerifierService = verifier,
        )
            .invoke(
                proofRequestJson = proofRequestJson,
                proofJson = proofJson,
                paymentTransactionHash = "different hash",
            )
    }
}
