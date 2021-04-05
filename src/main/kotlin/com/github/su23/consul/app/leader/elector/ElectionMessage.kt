package com.github.su23.consul.app.leader.elector

data class ElectionMessage(val status: String, val vote: Vote? = null, val error: String)
