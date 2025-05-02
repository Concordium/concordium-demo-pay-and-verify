package com.concordium.payandverify

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.koin.dsl.bind
import org.koin.dsl.module

val ioModule = module {

    single {
        jacksonObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    } bind ObjectMapper::class
}
