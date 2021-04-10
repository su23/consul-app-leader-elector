package com.github.su23.consul.app.leader.elector

import com.ecwid.consul.v1.kv.KeyValueConsulClient
import com.ecwid.consul.v1.session.SessionConsulClient
import com.ecwid.consul.v1.session.model.NewSession
import com.github.su23.consul.app.leader.elector.config.ClusterConfiguration
import mu.KLogging
import java.lang.Exception
import java.util.concurrent.*
import java.util.concurrent.TimeUnit.*

class ConsulLeaderController(private val config: ClusterConfiguration) {
    private companion object : KLogging()

    private val sessionClient = SessionConsulClient(config.consul.host, config.consul.port)
    private val keyValueClient = KeyValueConsulClient(config.consul.host, config.consul.port)
    private val executor = ScheduledThreadPoolExecutor(2)

    fun build(serviceName: String): IMember {
        val sessionId = createAndGetSessionId()
        logger.info { "Session created $sessionId" }
        executor.scheduleAtFixedRate(UpkeepSession(sessionId, sessionClient), 0L, config.session.refresh, SECONDS)

        val gambler = ConsulMember(serviceName, keyValueClient, sessionId, config)
        executor.scheduleAtFixedRate(gambler, config.election.frequency, config.election.frequency, SECONDS)
        logger.info { "Vote frequency setup on ${config.election.frequency} seconds frequency " }
        return gambler
    }

    private fun createAndGetSessionId(): String {
        val executor = Executors.newFixedThreadPool(1)
        val sessionId = executor.submit(CreateSession(config.session.ttl, sessionClient))
        executor.shutdown()
        return try {
            sessionId.get()
        } catch (e: Exception) {
            throw ConsulSessionException("Could not create consul session")
        }
    }

    internal class CreateSession(
        private val sessionTtlSeconds: Long,
        private val sessionClient: SessionConsulClient
    ) : Callable<String> {
        override fun call(): String {
            val consulSession = NewSession().apply { ttl = "${sessionTtlSeconds}s" }
            return sessionClient.sessionCreate(consulSession, null).value
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
