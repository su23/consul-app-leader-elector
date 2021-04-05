package com.github.su23.consul.app.leader.elector.sample

import com.github.su23.consul.app.leader.elector.ConsulLeaderController
import com.github.su23.consul.app.leader.elector.config.ClusterConfiguration
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.collect

suspend fun main() {
    val configuration = ClusterConfiguration("SampleApp")
    ConsulLeaderController(configuration).build().receiveAsFlow().consumeEach {
        println(it)
    }
}
