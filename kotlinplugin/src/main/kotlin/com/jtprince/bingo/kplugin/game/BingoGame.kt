package com.jtprince.bingo.kplugin.game

import com.jtprince.bingo.kplugin.board.Space
import com.jtprince.bingo.kplugin.player.BingoPlayer
import org.bukkit.command.CommandSender

abstract class BingoGame(
    val creator: CommandSender,
    val gameCode: String,
    players: Collection<BingoPlayer>
) {
    enum class State {
        BOARD_GENERATING,
        PREPARING,
        READY,
        RUNNING,
        DONE,
        FAILED,
    }

    abstract var state: State
        protected set
    val playerManager = PlayerManager(players)
    val spaces = HashMap<Int, Space>()

    open fun destroy() { }

    abstract fun signalStart()
    abstract fun signalEnd()

    /**
     * Called when a space in [spaces] should be marked a certain way. Not filtered, meaning
     * that this might be called several times with the same inputs.
     */
    protected abstract fun receiveAutomark(bingoPlayer: BingoPlayer, spaceId: Int,
                                           marking: Space.Marking)
}
