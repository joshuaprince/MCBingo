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
        WAITING_FOR_WEBSOCKET,
        WORLDS_GENERATING,
        READY,
        COUNTING_DOWN,
        RUNNING,
        DONE,
        FAILED,
    }

    abstract var state: State
        protected set
    val playerManager = PlayerManager(players)
    val spaces = HashMap<Int, Space>()

    fun destroy() {
        playerManager.destroy()
        spaces.values.forEach(Space::destroy)
        destroyGame()
    }

    protected abstract fun destroyGame()
    abstract fun signalStart()
    abstract fun signalEnd()

    /**
     * Called when a space in [spaces] should be marked a certain way. Not filtered, meaning
     * that this might be called several times with the same inputs.
     */
    protected abstract fun receiveAutomark(bingoPlayer: BingoPlayer, spaceId: Int,
                                           satisfied: Boolean)
}
