package com.github.su23.consul

import jdk.jfr.Frequency
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableConfigurationProperties(ConsulProperties::class)
open class ConsulConfiguration

@ConstructorBinding
@ConfigurationProperties("consul")
data class ConsulProperties(val session: Session = Session(), val election: Election = Election()) {
    companion object {
        data class Session(
            val ttl: Duration = Duration.ofSeconds(defaultTtl),
            val refresh: Duration = Duration.ofSeconds(defaultRefresh)
        ) {
            companion object {
                const val defaultTtl = 15L
                const val defaultRefresh = 7L
            }
        }
        data class Election(
            val enabled: Boolean = false,
            val frequency: Duration = Duration.ofSeconds(defaultFrequency),
            val delay: Duration = Duration.ofSeconds(defaultDelay),
            val envelopeTemplate: String = "services/%s/leader",
            val category: Category = Category()
        ) {
            companion object {
                const val defaultFrequency = 10L
                const val defaultDelay = 5L
                data class Category(val enabled: Boolean = false, val name: String = "")
            }
        }
    }
}