package com.github.su23.consul.election

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Component

@Component
internal class BulletinEncoder : IBulletinEncoder {
    private companion object {
        val mapper = jacksonObjectMapper()
    }

    override fun encode(vote: Vote) = mapper.writeValueAsString(vote)
    override fun decode(value: String): Vote = mapper.readValue(value)
}
