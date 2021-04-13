package com.jtprince.bingo.kplugin.webclient

import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object WebsocketMessageParser {
    fun parse(message: String): WebsocketMessage {
        val mapper = jacksonObjectMapper()
        return mapper.readValue<WebsocketMessage>(message)
    }
}
