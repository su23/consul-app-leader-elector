package com.github.su23.consul.app.leader.elector.sample

import com.github.su23.consul.app.leader.elector.ConsulLeaderController
import com.github.su23.consul.app.leader.elector.config.ClusterConfiguration
import kotlinx.coroutines.channels.consumeEach

suspend fun main() {
    val configuration = ClusterConfiguration("SampleApp")
    ConsulLeaderController(configuration).build().updateChannel.consumeEach {
        println(it)
    }
}
