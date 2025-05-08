package com.concordium.payandverify

import com.concordium.payandverify.util.getNotEmptyProperty
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.koin.dsl.bind
import org.koin.dsl.module

val indexModule = module {

    includes(
        invoiceModule,
    )

    single {
        IndexPageController(
            publicRootUrl = getNotEmptyProperty("PUBLIC_URL")
                .toHttpUrl(),
            storeTokenDecimals = getNotEmptyProperty("STORE_CIS2_TOKEN_DECIMALS")
                .toInt(),
            createCis2InvoiceUseCase = get(),
        )
    } bind IndexPageController::class
}
