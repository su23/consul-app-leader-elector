package com.github.su23.consul.election

internal interface IConsulPretender {
    val id: String
    val isValid: Boolean
    val isLeader: Boolean
    fun markAsLeader()
    fun markAsNonLeader()
    fun addOnElected(action: () -> Unit)
    fun addOnRelegated(action: () -> Unit)
}
