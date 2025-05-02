package com.concordium.payandverify

import com.concordium.payandverify.util.JavalinResponseStatusLogger
import com.concordium.payandverify.util.KLoggerKoinLogger
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.http.BadRequestResponse
import io.javalin.http.Header
import io.javalin.http.HttpStatus
import io.javalin.json.JavalinJackson
import io.javalin.rendering.template.JavalinThymeleaf
import io.javalin.router.exception.HttpResponseExceptionMapper
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.environmentProperties
import org.thymeleaf.TemplateEngine
import org.thymeleaf.messageresolver.StandardMessageResolver
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import sun.misc.Signal
import java.util.*

object Application : KoinComponent {

    private val apiLog = KotlinLogging.logger("API")

    @JvmStatic
    fun main(args: Array<String>) {

        startKoin {
            logger(
                KLoggerKoinLogger(
                    kLogger = KotlinLogging.logger("Koin"),
                    level = Level.DEBUG,
                )
            )

            environmentProperties()

            modules(
                ioModule,
            )
        }

        Javalin
            .create { config ->
                config.showJavalinBanner = false
                config.requestLogger.http(
                    JavalinResponseStatusLogger(
                        kLogger = apiLog,
                    )
                )
                config.http.defaultContentType = "text/plain; charset=utf-8"
                config.jsonMapper(JavalinJackson(get()))

                config.staticFiles.add("/frontend")
                config.staticFiles.add { staticFileConfig ->
                    staticFileConfig.directory = "/frontend/css"
                    staticFileConfig.hostedPath = "/css"
                }
                config.staticFiles.add { staticFileConfig ->
                    staticFileConfig.directory = "/frontend/js"
                    staticFileConfig.hostedPath = "/js"
                }
                config.staticFiles.add { staticFileConfig ->
                    staticFileConfig.directory = "/frontend/img"
                    staticFileConfig.hostedPath = "/img"
                }

                config.fileRenderer(JavalinThymeleaf(
                    TemplateEngine().apply {
                        setTemplateResolver(ClassLoaderTemplateResolver().apply {
                            templateMode = TemplateMode.HTML
                            prefix = "/frontend/"
                            characterEncoding = "UTF-8"
                        })
                        setMessageResolver(StandardMessageResolver())
                    }
                ))

                config.router.apiBuilder {
                    path("/api/v1/") {
                        get { ctx ->
                            ctx.status(HttpStatus.OK)
                            ctx.json(
                                mapOf(
                                    "status" to "ok",
                                )
                            )
                        }
                    }
                }
            }
            .get("/") { ctx ->
                ctx.render(
                    "/index.html",
                    mapOf(
                        "time" to Date().toString(),
                    )
                )
            }
            .after { ctx ->
                ctx.header(Header.SERVER, "concordium-demo-pay-and-verify")
            }
            .exception(BadRequestResponse::class.java) { e, ctx ->
                apiLog.debug(e) {
                    "bad request"
                }
                HttpResponseExceptionMapper.handle(e, ctx)
            }
            .start(getKoin().getProperty("PORT", "8241").toInt())
            .apply {
                // Gracefully stop on SIGINT and SIGTERM.
                listOf("INT", "TERM").forEach {
                    Signal.handle(Signal(it)) { stop() }
                }
            }
    }
}
