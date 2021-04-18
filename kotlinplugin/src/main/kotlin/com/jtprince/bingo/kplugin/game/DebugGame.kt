package com.jtprince.bingo.kplugin.game

import com.jtprince.bingo.kplugin.Messages
import com.jtprince.bingo.kplugin.board.SetVariables
import com.jtprince.bingo.kplugin.board.Space
import com.jtprince.bingo.kplugin.player.BingoPlayer
import org.bukkit.entity.Player

/**
 * A "game" that can be used for debugging auto triggers. Does not communicate with the web backend.
 */
class DebugGame(creator: Player,
                players: Collection<BingoPlayer>,
                val goalId: String,
                variables: SetVariables,
) : BingoGame(creator, "DebugGame", players) {
    override var state: State = State.RUNNING

    init {
        /* Special case to make sure World events in the spawn world get properly directed to
         * this bingo player. */
        playerManager.worldPlayerMap[creator.world] = playerManager.bingoPlayer(creator)!!

        val spc = Space(0, goalId, Space.GoalType.DEFAULT, "Debug Goal Text", variables)
        spc.startListening(playerManager, ::receiveAutomark)
        spaces[0] = spc
        Messages.basicAnnounce("Now debugging goal $goalId.")
    }

    override fun destroyGame() { }

    override fun signalStart() {
        TODO("Not yet implemented")
    }

    override fun signalEnd() {
        TODO("Not yet implemented")
    }

    override fun receiveAutomark(bingoPlayer: BingoPlayer, spaceId: Int, satisfied: Boolean) {
        if (satisfied) {
            Messages.basicTell(bingoPlayer, "You have activated $goalId.")
        }
    }
}
