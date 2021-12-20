package com.github.su23.consul.election

import mu.KLogging

internal class ConsulElectionProcess(
    private val election: IConsulElection,
    private val pretender: IConsulPretender
) : IConsulElectionProcess {
    private companion object : KLogging()

    override fun run() {
        try {
            election.run(pretender)
        } catch (e: Exception) {
            logger.error(e) { "Critical error happened" }
        }
    }
}
