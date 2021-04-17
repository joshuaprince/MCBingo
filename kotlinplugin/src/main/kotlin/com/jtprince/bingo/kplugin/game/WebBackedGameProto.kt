package com.jtprince.bingo.kplugin.game

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.jtprince.bingo.kplugin.BingoConfig
import com.jtprince.bingo.kplugin.BingoPlugin
import com.jtprince.bingo.kplugin.Messages
import com.jtprince.bingo.kplugin.board.Space
import com.jtprince.bingo.kplugin.player.BingoPlayer
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.util.logging.Level

/**
 * A Bingo Game that does not yet have a WebSocket connection, because it is still being created.
 */
class WebBackedGameProto(
    creator: CommandSender,
    val settings: WebGameSettings,
) : BingoGame(creator, "CreatingGame", emptySet()) {
    override var state: State = State.BOARD_GENERATING

    private val httpClient = HttpClient {
        install(JsonFeature) {
            serializer = JacksonSerializer()
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class WebGameSettings(
        @JsonProperty("game_code") val gameCode: String? = null,
        val shape: String? = null,
        val seed: String? = null,
        @JsonProperty("forced_goals") val forcedGoals: Collection<String> = emptySet()
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class WebGameResponse(
        @JsonProperty("game_code") val gameCode: String,
    )

    fun generateBoard(whenDone: (gameCode: String) -> Unit) {
        Messages.basicTell(creator, "Test.")
        Bukkit.getScheduler().runTaskAsynchronously(BingoPlugin) { -> runBlocking {
            Messages.basicTell(creator, "Generating a new board.")

            try {
                val response = httpClient.post<WebGameResponse>(BingoConfig.boardCreateUrl()) {
                    contentType(ContentType.Application.Json)
                    body = settings
                    // TODO: Catch "already exists" error
                }

                whenDone(response.gameCode)
            } catch (e: Exception) {
                BingoPlugin.logger.log(Level.SEVERE, "Failed to generate board", e)
                Messages.basicTell(creator, "Board generation failed.")
            }
        }}
    }

    override fun destroy() {
        httpClient.close()
    }

    override fun signalStart() {
        TODO("Not yet implemented")
    }

    override fun signalEnd() {
        TODO("Not yet implemented")
    }

    override fun receiveAutomark(bingoPlayer: BingoPlayer, spaceId: Int, marking: Space.Marking) {
        TODO("Not yet implemented")
    }
}
