package com.github.su23.consul.app.leader.elector.config

data class ClusterConfiguration(
    val consul: Consul = Consul(),
    val session: Session = Session(),
    val election: Election = Election()
) {
    companion object {
        data class Consul(val host: String = "localhost", val port: Int = 8500)
        data class Session(val ttl: Long = 15, val refresh: Long = 7)
        data class Election(
            val frequency: Long = 10,
            val decay: Long = 5,
            val envelopeTemplate: String = "services/%s/leader"
        )
    }
}