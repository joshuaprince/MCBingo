package com.jtprince.bingo.kplugin.automark

import com.jtprince.bingo.kplugin.board.SetVariables
import com.jtprince.bingo.kplugin.board.Space
import com.jtprince.bingo.kplugin.game.PlayerManager
import com.jtprince.bingo.kplugin.player.BingoPlayer

typealias AutoMarkCallback = (BingoPlayer, spaceId: Int, fulfilled: Boolean) -> Unit

abstract class AutoMarkTrigger(
    val goalId: String,
    val spaceId: Int,
    val variables: SetVariables,
    val playerManager: PlayerManager,
    val callback: AutoMarkCallback,
) {
    companion object {
        fun createForGoal(goalId: String, spaceId: Int, variables: SetVariables,
                          playerManager: PlayerManager, callback: AutoMarkCallback
        ): Collection<AutoMarkTrigger> {
            return (EventTrigger.createEventTriggers(goalId, spaceId, variables, playerManager, callback)
                + ItemTrigger.createItemTriggers(goalId, spaceId, variables, playerManager, callback)
                + SpecialItemTrigger.createSpecialItemTriggers(goalId, spaceId, variables, playerManager, callback))
        }

        val allAutomatedGoals
            get() = EventTrigger.allAutomatedGoals + ItemTrigger.allAutomatedGoals +
                    SpecialItemTrigger.allAutomatedGoals
    }

    /**
     * If (and only if) revertible is true, the AutoMarkCallback may be called with
     * fulfilled == false. In this case, the callback should put the goal into a "reverted" state.
     */
    abstract val revertible: Boolean

    abstract fun destroy()
}
