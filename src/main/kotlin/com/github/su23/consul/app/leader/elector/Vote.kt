package com.github.su23.consul.app.leader.elector

import kotlinx.serialization.Serializable

@Serializable
data class Vote(val sessionId: String, val serviceName: String)
