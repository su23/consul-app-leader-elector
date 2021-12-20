package com.github.su23.consul.election

import com.ecwid.consul.v1.ConsulClient
import com.github.su23.consul.ConsulProperties
import mu.KLogging
import org.springframework.stereotype.Component
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

@Component
internal class ConsulPretenderFactory(
    private val consul: ConsulClient,
    private val token: String,
    private val properties: ConsulProperties
) : IConsulPretenderFactory {
    private companion object : KLogging()

    private val executor = ScheduledThreadPoolExecutor(2)

    override fun create(): IConsulPretender {
        val sessionKeeper = SessionKeeper(consul, token, properties)
        logger.info { "Session created ${sessionKeeper.sessionId}" }

        val pretender = ConsulPretender(sessionKeeper)
        executor.scheduleAtFixedRate(sessionKeeper, 0L, properties.session.refresh.seconds, TimeUnit.SECONDS)

        return pretender
    }

}
