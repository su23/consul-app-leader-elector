package com.github.su23.consul.election

internal interface IBulletinEncoder {
    fun encode(vote: Vote): String
    fun decode(value: String): Vote
}

