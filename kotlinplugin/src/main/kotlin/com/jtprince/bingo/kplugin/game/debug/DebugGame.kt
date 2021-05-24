package com.jtprince.bingo.kplugin.game.debug

import com.jtprince.bingo.kplugin.Messages
import com.jtprince.bingo.kplugin.Messages.bingoTell
import com.jtprince.bingo.kplugin.Messages.bingoTellError
import com.jtprince.bingo.kplugin.automark.AutomatedSpace
import com.jtprince.bingo.kplugin.automark.EventPlayerMapper
import com.jtprince.bingo.kplugin.board.SetVariables
import com.jtprince.bingo.kplugin.game.BingoGame
import com.jtprince.bingo.kplugin.player.BingoPlayer
import com.jtprince.bukkit.worldset.WorldSet
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.Event

/**
 * A "game" that can be used for debugging a single automated trigger.
 * Does not communicate with the web backend.
 */
class DebugGame(
    creator: Player,
    players: Collection<BingoPlayer>,
    val goalId: String,
    variables: SetVariables,
) : BingoGame(creator, "DebugGame", players), EventPlayerMapper {
    override var state: State = State.RUNNING

    val space = DebugSpace(this, goalId, variables)

    init {
        Messages.bingoAnnounce("Now debugging goal $goalId.")
    }

    override fun signalStart(sender: CommandSender?) {
        sender?.bingoTell("Debug game is active; no need to start it.")
    }

    override fun signalEnd(sender: CommandSender?) {
        signalDestroy(sender)
    }

    override fun signalDestroy(sender: CommandSender?) {
        space.destroy()
        sender?.bingoTell("Debug Game destroyed.")
    }

    override fun receiveAutoMark(player: BingoPlayer, space: AutomatedSpace, fulfilled: Boolean) {
        if (fulfilled) {
            player.bingoTell("You have activated ${space.text}.")
        } else {
            player.bingoTellError("You have reverted ${space.text}.")
        }
    }

    override fun mapEvent(event: Event): BingoPlayer? =
        players.find { p -> creator in p.bukkitPlayers }

    override fun worldSet(player: BingoPlayer): WorldSet = WorldSet.defaultWorldSet
}
