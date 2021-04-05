package com.github.su23.consul.app.leader.elector.config

data class ClusterConfiguration(
    val serviceName: String,
    val serviceId: String = "node-1",
    val consul: Consul = Consul(),
    val session: Session = Session(),
    val election: Election = Election()
) {
    companion object {
        data class Consul(val host: String = "localhost", val port: Int = 8500)
        data class Session(val ttl: Int = 15, val refresh: Int = 7)
        data class Election(
            val frequency: Int = 10,
            val decay: Int = 5,
            val envelopeTemplate: String = "services/%s/leader"
        )
    }
}