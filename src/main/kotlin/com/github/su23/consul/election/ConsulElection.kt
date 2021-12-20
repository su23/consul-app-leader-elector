package com.github.su23.consul.election

import com.ecwid.consul.v1.OperationException
import com.ecwid.consul.v1.kv.KeyValueClient
import com.ecwid.consul.v1.kv.model.PutParams
import com.github.su23.consul.ConsulProperties
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
internal class ConsulElection(
    @Value("\${spring.application.name}")
    serviceName: String,
    private val consulKvClient: KeyValueClient,
    @Value("\${spring.cloud.consul.config.acl-token}")
    private val token: String,
    properties: ConsulProperties,
    private val bulletinEncoder: IBulletinEncoder
) : IConsulElection {
    private companion object : KLogging() {
        fun String.toConsulName() = replace('.', '-')
        const val ELECTED = "Elected"
        const val NOT_ELECTED = "Not Elected"
        const val ERROR = "Error"
    }

    private val consulServiceName = serviceName.toConsulName()

    private val serviceElectionId: String = if (properties.election.category.enabled) {
        String.format(properties.election.envelopeTemplate, "$consulServiceName-${properties.election.category.name}")
    } else {
        String.format(properties.election.envelopeTemplate, consulServiceName)
    }

    override fun run(pretender: IConsulPretender) {
        if (!pretender.isValid) {
            logger.warn { "Consul pretender is not valid. Skipping voting." }
            return
        }

        val params = PutParams().apply { acquireSession = pretender.id }
        val bulletin = createVotingBulletin(pretender)
        val elected = try {
            consulKvClient.setKVValue(serviceElectionId, bulletin, token, params).value
        } catch (e: OperationException) {
            pretender.markAsNonLeader()
            publish(e)
            return
        }

        publish(if (elected) ELECTED else NOT_ELECTED)
        if (elected) {
            pretender.markAsLeader()
        } else {
            pretender.markAsNonLeader()
        }
    }

    private fun createVotingBulletin(pretender: IConsulPretender): String {
        val vote = Vote(pretender.id, consulServiceName)
        return bulletinEncoder.encode(vote)
    }

    private fun leaderLookup(): Vote {
        val response = consulKvClient.getKVValue(serviceElectionId, token).value.decodedValue
        logger.info { "Decoded response - $response" }
        return bulletinEncoder.decode(response)
    }

    private fun publish(status: String) {
        val leader = try {
            leaderLookup()
        } catch (e: OperationException) {
            logger.warn { "Leader lookup failed" }
            null
        }

        logger.info { "$status: leader: $leader" }
    }

    private fun publish(e: Exception) {
        val leader = try {
            leaderLookup()
        } catch (e: OperationException) {
            logger.warn { "Leader lookup failed" }
            null
        }

        logger.info { "$ERROR: leader: $leader error: ${e.message}" }
    }
}