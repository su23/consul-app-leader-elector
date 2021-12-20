package com.github.su23.consul.election

import com.github.su23.consul.ConsulProperties
import mu.KLogging
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
internal class RegistrationStatusListener(
    private val properties: ConsulProperties,
    private val consulElection: IConsulElection,
    private val consulElectionProcessFactory: IConsulElectionProcessFactory,
    private val pretenderFactory: IConsulPretenderFactory
) : ApplicationListener<InstanceRegisteredEvent<*>>, IRegistrationStatusListener {
    private companion object : KLogging()

    private val onElectedActions = mutableListOf<() -> Unit>()
    private val onRelegatedActions = mutableListOf<() -> Unit>()
    private var electionProcess: IConsulElectionProcess? = null
    private var pretender: IConsulPretender? = null

    init {
        logger.info { "Consul registration status listener instantiated" }
        logger.info { properties }
    }

    override val isLeadershipEnabled = properties.election.enabled

    override var isRegistered: Boolean = false
        private set

    override val isLeader: Boolean
        get() = if (isLeadershipEnabled) (pretender?.isLeader ?: false) else true

    override fun addOnElected(action: () -> Unit) {
        pretender?.addOnElected(action) ?: onElectedActions.add(action)
    }

    override fun addOnRelegated(action: () -> Unit) {
        pretender?.addOnRelegated(action) ?: onRelegatedActions.add(action)
    }

    override fun onApplicationEvent(event: InstanceRegisteredEvent<*>) {
        logger.info { "Application registered" }
        isRegistered = true

        if (!isLeadershipEnabled) {
            return
        }

        val pretender = pretenderFactory.create().also { x ->
            onElectedActions.forEach { x.addOnElected(it) }
            onRelegatedActions.forEach { x.addOnRelegated(it) }
        }

        this.pretender = pretender
        electionProcess = consulElectionProcessFactory.create(consulElection, pretender)
    }
}