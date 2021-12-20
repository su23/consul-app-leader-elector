package com.github.su23.consul.election

import mu.KLogging

internal class ConsulPretender(private val sessionKeeper: ISessionKeeper) : IConsulPretender {
    private companion object : KLogging() {
        const val electedFirstTimeMessage = "Elected first time"
        const val relegationMessage = "Relegated"
    }

    private val onElectedActions = mutableListOf({ logger.info { electedFirstTimeMessage }})
    private val onRelegatedActions = mutableListOf({ logger.info { relegationMessage }})
    private var wasLeader = false

    init {
        sessionKeeper.addOnEstablishFailed { markAsNonLeader() }
    }

    override val id: String
        get() = sessionKeeper.sessionId
    override val isValid: Boolean
        get() = sessionKeeper.isValid
    override var isLeader: Boolean = false
        private set

    override fun markAsLeader() {
        isLeader = true
        if (!wasLeader) {
            onElectedActions.forEach { it.invoke() }
            wasLeader = true
        }
    }

    override fun markAsNonLeader() {
        isLeader = false
        if (wasLeader) {
            onRelegatedActions.forEach { it.invoke() }
            wasLeader = false
        }
    }

    override fun addOnElected(action: () -> Unit) {
        onElectedActions.add(action)
    }

    override fun addOnRelegated(action: () -> Unit) {
        onRelegatedActions.add(action)
    }
}
