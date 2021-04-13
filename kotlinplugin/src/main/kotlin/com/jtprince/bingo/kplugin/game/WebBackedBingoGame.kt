package com.jtprince.bingo.kplugin.game

import com.jtprince.bingo.kplugin.player.BingoPlayer
import com.jtprince.bingo.kplugin.webclient.WebBackedWebsocketClient
import org.bukkit.command.CommandSender

class WebBackedBingoGame(creator: CommandSender, val settings: GameSettings,
                         val players: Collection<BingoPlayer>) : BingoGame(creator) {
    lateinit var websocketClient: WebBackedWebsocketClient

    data class GameSettings(val gameCode: String? = null,
                            val shape: String? = null,
                            val seed: String? = null,
                            val forcedGoals: Collection<String> = emptySet())

    fun generateBoard() {

    }

    override fun signalStart() {
        TODO("Not yet implemented")
    }

    override fun signalEnd() {
        TODO("Not yet implemented")
    }
}
