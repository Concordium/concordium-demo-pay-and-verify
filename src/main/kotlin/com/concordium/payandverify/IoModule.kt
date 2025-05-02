package com.concordium.payandverify

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.OkHttpClient
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

val ioModule = module {

    single {
        jacksonObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    } bind ObjectMapper::class

    single {
        OkHttpClient.Builder()
            // Connect timeout to cut off dead network.
            .connectTimeout(30, TimeUnit.SECONDS)
            // Read and write timeouts are big because PhotoPrism
            // may run really slow on low-power servers.
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
    } bind OkHttpClient.Builder::class

    single {
        get<OkHttpClient.Builder>()
            .build()
    } bind OkHttpClient::class

    factory {
        Retrofit.Builder()
            .addConverterFactory(JacksonConverterFactory.create(get()))
    } bind Retrofit.Builder::class
}
