package com.jtprince.bingo.kplugin.webclient

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.jtprince.bingo.kplugin.BingoPlugin
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import org.bukkit.Bukkit
import java.util.logging.Level

class WebBackedWebsocketClient(
    private val gameCode: String,
    private val url: Url,
    private val onReceive: (WebsocketRxMessage) -> Unit,
    private val onFailure: () -> Unit,
) {
    private val mapper = jacksonObjectMapper()
    private val client = HttpClient {
        install(WebSockets)
    }

    private val txQueue = Channel<WebsocketTxMessage>(32, BufferOverflow.DROP_OLDEST) {
        BingoPlugin.logger.severe("Dropping websocket message ${it.action}")
    }

    private var shouldReconnect = true
    private var connectAttemptsRemaining = 10

    fun destroy() {
        shouldReconnect = false
        client.close()
        BingoPlugin.logger.severe("Websocket closed for game $gameCode")
    }

    fun connect(onSuccess: () -> Unit) {
        Bukkit.getScheduler().runTaskAsynchronously(BingoPlugin) { -> runBlocking {
            var connectedBefore = false
            while (shouldReconnect) {
                BingoPlugin.logger.info((if (connectedBefore) "Reconnecting" else "Connecting") +
                        " to game $gameCode...")
                try {
                    client.ws(url.toString()) {
                        BingoPlugin.logger.info("Successfully connected to game $gameCode.")
                        if (!connectedBefore) {
                            connectedBefore = true
                            onSuccess()
                        }
                        connectAttemptsRemaining = 10
                        val rxJob = launch { rxLoop() }
                        val txJob = launch { txLoop() }

                        joinAll(rxJob, txJob)
                        BingoPlugin.logger.info("Connection to game $gameCode was closed.")
                    }
                } catch (e: IOException) {
                    BingoPlugin.logger.warning(
                        "Error in connection to game $gameCode: ${e.localizedMessage}")
                }

                if (connectAttemptsRemaining-- <= 0) {
                    BingoPlugin.logger.severe("Ran out of connection attempts.")
                    onFailure()
                    break
                }

                // Wait to try reconnecting
                delay(5000)
            }
        }}
    }

    private suspend fun ClientWebSocketSession.rxLoop() {
        while (true) {
            val frame = incoming.receive()
            if (frame !is Frame.Text) {
                println("Unknown message type ${frame::class}")
                continue
            }

            try {
                val msg = mapper.readValue<WebsocketRxMessage>(frame.readText())
                onReceive(msg)
            } catch (e: Exception) {
                BingoPlugin.logger.log(Level.SEVERE, "Could not receive websocket message", e)
            }
        }
    }

    private suspend fun ClientWebSocketSession.txLoop() {
        while (true) {
            val frame = txQueue.receive()
            val text = withContext(Dispatchers.IO) { mapper.writeValueAsString(frame) }
            send(Frame.Text(text))
            println(text)
        }
    }

    fun sendStartGame() {
        /* Does not actually block since channel is set to drop on full */
        txQueue.sendBlocking(TxMessageGameState(true))
    }

    fun sendEndGame() {
        txQueue.sendBlocking(TxMessageGameState(false))
    }

    fun sendRevealBoard() {
        txQueue.sendBlocking(TxMessageRevealBoard())
    }

    fun sendMarkSpace(player: String, spaceId: Int, marking: Int) {
        txQueue.sendBlocking(TxMessageMarkSpace(player, spaceId, marking))
    }

    fun sendAutoMarks(playerSpaceIdsMap: Map<String, Collection<Int>>) {
        txQueue.sendBlocking(TxMessageSetAutoMarks(playerSpaceIdsMap))
    }
}
