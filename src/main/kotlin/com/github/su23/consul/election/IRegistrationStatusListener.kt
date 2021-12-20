package com.github.su23.consul.election

interface IRegistrationStatusListener {
    val isLeadershipEnabled: Boolean
    val isRegistered: Boolean
    val isLeader: Boolean
    fun addOnElected(action: () -> Unit)
    fun addOnRelegated(action: () -> Unit)
}
