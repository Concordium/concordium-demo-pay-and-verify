package com.concordium.payandverify

import com.concordium.payandverify.util.getNotEmptyProperty
import org.koin.dsl.bind
import org.koin.dsl.module

val invoiceModule = module {

    includes(
        ioModule,
        web3IdVerifierModule,
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
}
