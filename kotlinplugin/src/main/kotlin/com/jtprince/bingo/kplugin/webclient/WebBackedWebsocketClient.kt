package com.jtprince.bingo.kplugin.webclient

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jtprince.bingo.kplugin.BingoPlugin
import com.jtprince.bingo.kplugin.board.Board
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.runBlocking
import java.lang.Exception
import java.net.URI

class WebBackedWebsocketClient(val gameCode: String, private val url: URI) {
    private val client = HttpClient {
        install(WebSockets)

    }

    fun connect() {
//        kotlin.run {
//            client.ws(url.toString()) {
//                when (val frame = incoming.receive()) {
//                    is Frame.Text -> println(frame)
//                    else -> println("Unknown message type ${frame::class}")
//                }
//            }
//        }
    }

    fun consume(message: WebsocketMessage) {

    }
}
