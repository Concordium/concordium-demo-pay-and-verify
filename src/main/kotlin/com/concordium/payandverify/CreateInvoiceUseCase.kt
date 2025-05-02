package com.concordium.payandverify

import com.concordium.sdk.crypto.wallet.web3Id.CredentialAttribute
import com.concordium.sdk.crypto.wallet.web3Id.Statement.IdentityQualifier
import com.concordium.sdk.crypto.wallet.web3Id.Statement.RangeStatement
import com.concordium.sdk.crypto.wallet.web3Id.Statement.StatementType
import com.concordium.sdk.crypto.wallet.web3Id.Statement.UnqualifiedRequestStatement
import com.concordium.sdk.crypto.wallet.web3Id.UnqualifiedRequest
import com.concordium.sdk.responses.accountinfo.credential.AttributeType
import com.concordium.sdk.serializing.JsonMapper
import mu.KotlinLogging
import java.math.BigInteger
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class CreateInvoiceUseCase(
    private val storeTokenIndex: Int,
    private val storeAccountAddress: String,
    private val invoiceRepository: InvoiceRepository,
) {

    private val log = KotlinLogging.logger("CreateInvoiceUC")

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
            "createInvoice(): creating:" +
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
                                    .attributeTag(JsonMapper.INSTANCE.writeValueAsString(AttributeType.DOB))
                                    .build(),
                            )
                        )
                        .build()
                )
            )
            .build()

        val invoice = Invoice(
            id = UUID.randomUUID().toString(),
            amount = amount,
            tokenIndex = storeTokenIndex,
            recipientAccountAddress = storeAccountAddress,
            proofRequestJson = JsonMapper.INSTANCE.writeValueAsString(proofRequest),
            status = Invoice.Status.Pending,
        )

        invoiceRepository.addInvoice(invoice)

        log.debug {
            "creteInvoice(): created:" +
                    "\ninvoice=$invoice"
        }

        return invoice
    }

    private companion object {
        private const val TX_HASH_PLACEHOLDER = "payment-transaction-hash"
    }
}
