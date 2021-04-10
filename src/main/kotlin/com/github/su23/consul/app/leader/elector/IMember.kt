package com.github.su23.consul.app.leader.elector

import kotlinx.coroutines.channels.Channel

interface IMember {
    companion object {
        const val ELECTED_FIRST_TIME = "Elected first time"
        const val ELECTED = "Elected"
        const val RELEGATION = "Relegated"
        const val NOT_ELECTED = "Not elected"
        const val ERROR = "Error"
    }

    val isLeader: Boolean
    val updateChannel: Channel<ElectionMessage>
}

