package com.concordium.payandverify

import com.concordium.payandverify.util.getNotEmptyProperty
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.koin.dsl.bind
import org.koin.dsl.module

val dashboardModule = module {

    includes(
        invoiceModule,
    )

    single {
        DashboardPageController(
            ccdExplorerRootUrl = getNotEmptyProperty("CCD_EXPLORER_URL")
                .toHttpUrl(),
            invoiceRepository = get(),
        )
    } bind DashboardPageController::class
}
