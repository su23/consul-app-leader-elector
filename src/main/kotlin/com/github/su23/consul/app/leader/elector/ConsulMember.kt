package com.github.su23.consul.app.leader.elector

import com.ecwid.consul.v1.kv.KeyValueClient
import com.ecwid.consul.v1.kv.model.PutParams
import com.github.su23.consul.app.leader.elector.config.ClusterConfiguration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.lang.Exception

class ConsulMember(
    private val consulKvClient: KeyValueClient,
    private val sessionId: String,
    private val clusterConfiguration: ClusterConfiguration
) : Runnable, IMember {
    private var wasLeader = false
    private val channel = Channel<ElectionMessage>(Channel.BUFFERED)
    private val key: String = String.format(
        clusterConfiguration.election.envelopeTemplate,
        clusterConfiguration.serviceName
    )

    override var isLeader: Boolean = false
        private set

    override fun receiveAsFlow() = channel

    override fun run() {
        try {
            isLeader = vote()
        } catch (e: Exception) {
            isLeader = false
            publish(e)
            return
        }

        if (!isLeader) {
            publish(IMember.NOT_ELECTED)
            if (wasLeader) {
                publish(IMember.RELEGATION)
                wasLeader = false
            }

            return
        }

        publish(IMember.ELECTED)
        if (!wasLeader) {
            publish(IMember.ELECTED_FIRST_TIME)
            wasLeader = true
        }
    }

    private fun vote(): Boolean {
        val params = PutParams().apply { acquireSession = sessionId }
        val envelope = createVoteEnvelope()
        return consulKvClient.setKVValue(key, envelope, params).value
    }

    private fun createVoteEnvelope(): String {
        val vote = Vote(sessionId, clusterConfiguration.serviceName, clusterConfiguration.serviceId)
        return Json.encodeToString(vote)
    }

    private fun leaderLookup(): Vote {
        val response: String = consulKvClient.getKVValue(key).value.decodedValue
        return Json.decodeFromString(response)
    }

    private fun publish(status: String) {
        val leader = try {
            leaderLookup()
        } catch (e: IOException) {
            null
        }

        val electionMessage = ElectionMessage(status, leader, "")
        channel.sendBlocking(electionMessage)
    }

    private fun publish(e: Exception) {
        val leaderVote = try {
            leaderLookup()
        } catch (ioe: IOException) {
            null
        }

        val electionMessage = ElectionMessage(IMember.ERROR, leaderVote, e.message ?: "")
        channel.sendBlocking(electionMessage)
    }
}
