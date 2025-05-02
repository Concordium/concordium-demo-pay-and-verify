package com.concordium.payandverify.util

import io.javalin.http.Context
import io.javalin.http.RequestLogger
import mu.KLogger
import mu.KotlinLogging

class JavalinResponseStatusLogger(
    private val kLogger: KLogger = KotlinLogging.logger("API")
) : RequestLogger {

    override fun handle(ctx: Context, executionTimeMs: Float) {
        if (executionTimeMs < 1000) {
            kLogger.debug {
                "response: " +
                        "method=${ctx.method()}, " +
                        "uri=${ctx.req().requestURI}, " +
                        "query=${ctx.req().queryString}, " +
                        "status=${ctx.res().status}, " +
                        "timeMs=$executionTimeMs"
            }
        } else {
            kLogger.warn {
                "slow response: " +
                        "method=${ctx.method()}, " +
                        "uri=${ctx.req().requestURI}, " +
                        "query=${ctx.req().queryString}, " +
                        "status=${ctx.res().status}, " +
                        "timeMs=$executionTimeMs"
            }
        }
    }
}
