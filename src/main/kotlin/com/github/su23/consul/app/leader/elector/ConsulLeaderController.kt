package com.github.su23.consul.app.leader.elector

import com.ecwid.consul.v1.kv.KeyValueConsulClient
import com.ecwid.consul.v1.session.SessionConsulClient
import com.ecwid.consul.v1.session.model.NewSession
import com.github.su23.consul.app.leader.elector.config.ClusterConfiguration
import mu.KLogging
import java.lang.Exception
import java.util.concurrent.*

class ConsulLeaderController(private val configuration: ClusterConfiguration) {
    private companion object : KLogging()

    private val sessionConsulClient = SessionConsulClient(
        configuration.consul.host,
        configuration.consul.port
    )

    private val keyValueConsulClient = KeyValueConsulClient(
        configuration.consul.host,
        configuration.consul.port
    )
    private val executor = ScheduledThreadPoolExecutor(2)
    private var sessionId: String? = null

    fun build(): IMember {
        val sessionId = createAndGetSessionId()
        logger.info { "Session created $sessionId" }
        executor.scheduleAtFixedRate(
            UpkeepSession(sessionId, sessionConsulClient), 0L,
            configuration.session.refresh.toLong(), TimeUnit.SECONDS
        )

        val gambler = ConsulMember(keyValueConsulClient, sessionId, configuration)
        executor.scheduleAtFixedRate(
            gambler,
            configuration.election.frequency.toLong(),
            configuration.election.frequency.toLong(),
            TimeUnit.SECONDS
        )

        logger.info { "Vote frequency setup on ${configuration.election.frequency} seconds frequency " }
        return gambler
    }

    private fun createAndGetSessionId(): String {
        if (sessionId != null) {
            return sessionId!!
        }

        val executor = Executors.newFixedThreadPool(1)
        val sessionId = executor.submit(CreateSession(configuration.session.ttl, sessionConsulClient))
        executor.shutdown()
        return try {
            sessionId.get()
        } catch (e: Exception) {
            throw ConsulSessionException("Could not create consul session")
        }
    }

    internal class CreateSession(
        private val sessionTtlSeconds: Int,
        private val sessionConsulClient: SessionConsulClient
    ) : Callable<String> {
        override fun call(): String {
            val consulSession = NewSession().apply { ttl = "${sessionTtlSeconds}s" }
            return sessionConsulClient.sessionCreate(consulSession, null).value
        }
    }

    class ConsulSessionException(message: String) : RuntimeException(message)

    internal class UpkeepSession(
        private val sessionId: String,
        private val sessionConsulClient: SessionConsulClient
    ) : Runnable {
        private companion object : KLogging()
        override fun run() {
            logger.info { "Renew leader session $sessionId" }
            sessionConsulClient.renewSession(sessionId, null)
        }
    }
}
