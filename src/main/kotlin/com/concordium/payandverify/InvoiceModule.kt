package com.concordium.payandverify

import com.concordium.payandverify.util.getNotEmptyProperty
import org.koin.dsl.bind
import org.koin.dsl.module

val invoiceModule = module {

    includes(
        web3IdVerifierModule,
        walletProxyModule,
    )

    single {
        InvoiceRepository()
    } bind InvoiceRepository::class

    single {
        CreateCis2InvoiceUseCase(
            storeTokenIndex = getNotEmptyProperty("STORE_CIS2_TOKEN_INDEX")
                .toInt(),
            storeTokenDecimals = getNotEmptyProperty("STORE_CIS2_TOKEN_DECIMALS")
                .toInt(),
            storeTokenSymbol = getNotEmptyProperty("STORE_CIS2_TOKEN_SYMBOL"),
            storeTokenContractName = getNotEmptyProperty("STORE_CIS2_TOKEN_CONTRACT_NAME"),
            storeAccountAddress = getNotEmptyProperty("STORE_ACCOUNT_ADDRESS"),
            invoiceRepository = get(),
        )
    } bind CreateCis2InvoiceUseCase::class

    single {
        VerifyPaymentIdProofUseCase(
            web3IdVerifierService = get(),
        )
    } bind VerifyPaymentIdProofUseCase::class

    single {
        SubmitPaymentTransactionUseCase(
            walletProxyService = get(),
        )
    } bind SubmitPaymentTransactionUseCase::class

    single {
        AcceptInvoicePaymentUseCase(
            verifyPaymentIdProofUseCase = get(),
            submitPaymentTransactionUseCase = get(),
            invoiceRepository = get(),
        )
    } bind AcceptInvoicePaymentUseCase::class

    single {
        InvoiceStatusPagePartController(
            invoiceRepository = get(),
        )
    } bind InvoiceStatusPagePartController::class

    single {
        InvoiceApiV1Controller(
            acceptInvoicePaymentUseCase = get(),
            invoiceRepository = get(),
        )
    } bind InvoiceApiV1Controller::class
}
