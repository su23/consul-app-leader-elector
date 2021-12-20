package com.github.su23.consul.election

import com.github.su23.consul.ConsulProperties
import mu.KLogging
import org.springframework.stereotype.Component
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

@Component
internal class ConsulElectionProcessFactory(private val properties: ConsulProperties) : IConsulElectionProcessFactory {
    private companion object : KLogging()

    private val executor = ScheduledThreadPoolExecutor(2)

    override fun create(election: IConsulElection, pretender: IConsulPretender): IConsulElectionProcess? {
        if (!properties.election.enabled) {
            return null
        }

        val process = ConsulElectionProcess(election, pretender)
        val delay = properties.election.delay
        val frequency = properties.election.frequency
        executor.scheduleAtFixedRate(process, delay.seconds, frequency.seconds, TimeUnit.SECONDS)
        logger.info { "Vote frequency is ${frequency.seconds} seconds" }
        return process
    }
}
