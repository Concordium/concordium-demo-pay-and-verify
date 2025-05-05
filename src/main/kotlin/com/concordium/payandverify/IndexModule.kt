package com.concordium.payandverify

import org.koin.dsl.bind
import org.koin.dsl.module

val indexModule = module {

    includes(
        invoiceModule,
    )

    single {
        IndexPageController(
            createCis2InvoiceUseCase = get(),
        )
    } bind IndexPageController::class
}
