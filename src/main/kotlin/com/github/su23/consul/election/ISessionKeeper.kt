package com.github.su23.consul.election

internal interface ISessionKeeper : Runnable {
    val sessionId: String
    val isValid: Boolean
    fun addOnEstablishFailed(action: () -> Unit)
}
