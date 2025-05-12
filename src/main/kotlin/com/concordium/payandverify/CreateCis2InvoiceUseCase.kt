package com.concordium.payandverify

import com.concordium.sdk.crypto.wallet.web3Id.CredentialAttribute
import com.concordium.sdk.crypto.wallet.web3Id.Statement.IdentityQualifier
import com.concordium.sdk.crypto.wallet.web3Id.Statement.RangeStatement
import com.concordium.sdk.crypto.wallet.web3Id.Statement.StatementType
import com.concordium.sdk.crypto.wallet.web3Id.Statement.UnqualifiedRequestStatement
import com.concordium.sdk.crypto.wallet.web3Id.UnqualifiedRequest
import com.concordium.sdk.serializing.JsonMapper
import mu.KotlinLogging
import java.math.BigInteger
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class CreateCis2InvoiceUseCase(
    private val storeTokenDecimals: Int,
    private val storeTokenSymbol: String,
    private val storeTokenId: String,
    private val storeTokenContractIndex: Int,
    private val storeTokenContractName: String,
    private val storeAccountAddress: String,
    private val invoiceRepository: InvoiceRepository,
) {

    private val log = KotlinLogging.logger("CreateCis2InvoiceUC")

    /**
     * Creates a new pending invoice and adds it to the [InvoiceRepository].
     */
    operator fun invoke(
        amount: BigInteger,
        minAgeYears: Int,
    ): Invoice {
        val dateOfBirthUpperBound = LocalDate.now()
            .minusYears(minAgeYears.toLong())
            .format(DateTimeFormatter.ofPattern("yyyyMMdd"))

        log.debug {
            "invoke(): creating:" +
                    "\namount=$amount," +
                    "\nminAgeYears=$minAgeYears," +
                    "\ndateOfBirthUpperBound=$dateOfBirthUpperBound"
        }

        val proofRequest = UnqualifiedRequest.builder()
            .challenge(TX_HASH_PLACEHOLDER)
            .credentialStatements(
                listOf(
                    UnqualifiedRequestStatement.builder()
                        .idQualifier(object : IdentityQualifier() {
                            override fun getIssuers(): MutableList<Long> =
                                (0..4).mapTo(mutableListOf(), Int::toLong)

                            override fun getType(): StatementType =
                                StatementType.Credential

                        })
                        .statement(
                            listOf(
                                RangeStatement.builder()
                                    .upper(
                                        CredentialAttribute.builder()
                                            .value(dateOfBirthUpperBound)
                                            .type(CredentialAttribute.CredentialAttributeType.STRING)
                                            .build()
                                    )
                                    .lower(
                                        CredentialAttribute.builder()
                                            .value("18000101")
                                            .type(CredentialAttribute.CredentialAttributeType.STRING)
                                            .build()
                                    )
                                    .attributeTag("dob")
                                    .build(),
                            )
                        )
                        .build()
                )
            )
            .build()

        val invoice = Invoice(
            id = UUID.randomUUID().toString(),
            createdAt = Instant.now(),
            minAgeYears = minAgeYears,
            proofRequestJson = JsonMapper.INSTANCE.writeValueAsString(proofRequest),
            paymentDetails = Invoice.PaymentDetails.Cis2(
                amount = amount,
                tokenContractIndex = storeTokenContractIndex,
                tokenSymbol = storeTokenSymbol,
                tokenId = storeTokenId,
                tokenContractName = storeTokenContractName,
                tokenDecimals = storeTokenDecimals,
                recipientAccountAddress = storeAccountAddress,
            ),
            status = Invoice.Status.Pending,
        )

        invoiceRepository.addInvoice(invoice)

        log.debug {
            "invoke(): created:" +
                    "\ninvoice=$invoice"
        }

        return invoice
    }

    private companion object {
        private const val TX_HASH_PLACEHOLDER = "payment-transaction-hash"
    }
}
