package com.concordium.payandverify

import com.concordium.payandverify.util.JavalinResponseStatusLogger
import com.concordium.payandverify.util.KLoggerKoinLogger
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.BadRequestResponse
import io.javalin.http.Header
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
                invoiceModule,
                indexModule,
                dashboardModule,
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
                    staticFileConfig.directory = "/frontend/static"
                    staticFileConfig.hostedPath = "/static"
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

                    path("/api/v1/invoices") {
                        get(
                            "{id}",
                            get<InvoiceApiV1Controller>()::getInvoiceById,
                        )
                        post(
                            "{id}/pay",
                            get<InvoiceApiV1Controller>()::payInvoiceById,
                        )
                    }

                    get(
                        "/",
                        get<IndexPageController>()::render,
                    )

                    get(
                        "/dashboard",
                        get<DashboardPageController>()::render,
                    )

                    path("/invoices") {
                        get(
                            "{id}",
                            get<InvoicePageController>()::render,
                        )

                        get(
                            "{id}/details",
                            get<InvoicePageController>()::renderDetails,
                        )

                        get(
                            "{id}/status",
                            get<InvoicePageController>()::renderStatus,
                        )
                    }
                }
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
