package com.github.su23.consul.election

internal interface IConsulElectionProcessFactory {
    fun create(election: IConsulElection, pretender: IConsulPretender): IConsulElectionProcess?
}
