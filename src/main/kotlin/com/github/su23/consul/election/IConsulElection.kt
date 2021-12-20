package com.github.su23.consul.election

internal interface IConsulElection {
    fun run(pretender: IConsulPretender)
}

