package com.concordium.payandverify.util

import mu.KLogger
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE

class KLoggerKoinLogger(
    private val kLogger: KLogger,
    level: Level = Level.ERROR,
) : Logger(level) {
    override fun display(level: Level, msg: MESSAGE) {
        when (level) {
            Level.DEBUG -> kLogger.debug { msg }
            Level.INFO -> kLogger.info { msg }
            Level.WARNING -> kLogger.warn { msg }
            Level.ERROR -> kLogger.error { msg }
            Level.NONE -> {
            }
        }
    }
}
