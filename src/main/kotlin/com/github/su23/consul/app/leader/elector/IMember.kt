package com.github.su23.consul.app.leader.elector

import kotlinx.coroutines.channels.Channel

interface IMember {
    companion object {
        var ELECTED_FIRST_TIME = "elected.first"
        var ELECTED = "elected"
        var RELEGATION = "relegation"
        var NOT_ELECTED = "notelected"
        var ERROR = "error"
    }

    val isLeader: Boolean
    fun receiveAsFlow(): Channel<ElectionMessage>
}

