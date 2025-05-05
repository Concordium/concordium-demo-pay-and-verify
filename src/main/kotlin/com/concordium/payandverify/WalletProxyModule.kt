package com.concordium.payandverify

import com.concordium.payandverify.util.getNotEmptyProperty
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit

val walletProxyModule = module {

    includes(
        ioModule,
    )

    single {
        get<Retrofit.Builder>()
            .baseUrl(getNotEmptyProperty("WALLET_PROXY_URL"))
            .build()
            .create(WalletProxyService::class.java)
    } bind WalletProxyService::class
}
