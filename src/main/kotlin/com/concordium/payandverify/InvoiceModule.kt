package com.concordium.payandverify

import com.concordium.payandverify.util.getNotEmptyProperty
import okhttp3.HttpUrl.Companion.toHttpUrl
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
        CreateInvoiceUseCase(
            storeTokenIndex = getNotEmptyProperty("STORE_TOKEN_INDEX")
                .toInt(),
            storeAccountAddress = getNotEmptyProperty("STORE_ACCOUNT_ADDRESS"),
            invoiceRepository = get(),
        )
    } bind CreateInvoiceUseCase::class

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
            publicRootUrl = getNotEmptyProperty("PUBLIC_URL")
                .toHttpUrl(),
            invoiceRepository = get(),
        )
    } bind InvoiceStatusPagePartController::class
}
