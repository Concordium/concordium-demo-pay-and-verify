package com.concordium.payandverify

import com.concordium.payandverify.util.getNotEmptyProperty
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit

val web3IdVerifierModule = module {

    includes(
        ioModule,
    )

    single {
        get<Retrofit.Builder>()
            .client(get())
            .baseUrl(getNotEmptyProperty("WEB3ID_VERIFIER_URL"))
            .build()
            .create(Web3IdVerifierService::class.java)
    } bind Web3IdVerifierService::class
}
