package com.github.su23.consul.election

import com.ecwid.consul.v1.OperationException
import com.ecwid.consul.v1.session.SessionClient
import com.ecwid.consul.v1.session.model.NewSession
import com.github.su23.consul.ConsulProperties
import mu.KLogging
import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.Executors

internal class SessionKeeper(
    private val sessionClient: SessionClient,
    private val token: String,
    private val properties: ConsulProperties
): ISessionKeeper {
    companion object : KLogging() {
        class ConsulSessionException(message: String) : RuntimeException(message)
        private class CreateSession(
            private val sessionTtl: Duration,
            private val sessionClient: SessionClient,
            private val token: String = ""
        ) : Callable<String> {
            override fun call(): String {
                val consulSession = NewSession().apply { ttl = "${sessionTtl.seconds}s" }
                return sessionClient.sessionCreate(consulSession, null, token).value
            }
        }
    }

    private val onEstablishFailedActions = mutableListOf<() -> Unit>()

    override var sessionId: String = ""
        private set

    override var isValid: Boolean = true
        private set

    override fun addOnEstablishFailed(action: () -> Unit) {
        onEstablishFailedActions.add(action)
    }

    override fun run() {
        try {
            logger.info { "Renew session $sessionId" }
            sessionClient.renewSession(sessionId, null, token)
            setAsEstablished()
            return
        } catch (e: OperationException) {
            logger.warn { "Renew session $sessionId failed!" }
        } catch (e: Exception) {
            logger.error(e) { "Critical error happened." }
        }

        try {
            logger.info { "Try to generate new session" }
            sessionId = createAndGetSessionId()
            logger.info { "New session $sessionId" }
            setAsEstablished()
        } catch (e: ConsulSessionException) {
            logger.warn { "Will try to create session in next iteration" }
            setAsFailed()
        }
    }

    private fun createAndGetSessionId(): String {
        val executor = Executors.newFixedThreadPool(1)
        val sessionId = executor.submit(CreateSession(properties.session.ttl, sessionClient, token))
        executor.shutdown()
        return try {
            val result = sessionId.get()
            setAsEstablished()
            result
        } catch (e: Exception) {
            setAsFailed()
            throw ConsulSessionException("Couldn't create consul session")
        }
    }

    private fun setAsEstablished() {
        isValid = true
    }

    private fun setAsFailed() {
        isValid = false
        onEstablishFailedActions.forEach { it.invoke() }
    }
}
