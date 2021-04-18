package com.jtprince.bingo.kplugin.automark

import com.jtprince.bingo.kplugin.BingoPlugin
import com.jtprince.bingo.kplugin.board.SetVariables
import com.jtprince.bingo.kplugin.game.PlayerManager

class OccasionalTrigger(
    goalId: String,
    spaceId: Int,
    variables: SetVariables,
    playerManager: PlayerManager,
    callback: AutoMarkCallback,
    private val triggerDefinition: OccasionalTriggerDefinition,
) : AutoMarkTrigger(goalId, spaceId, variables, playerManager, callback) {

    companion object {
        fun createOccasionalTriggers(goalId: String, spaceId: Int, variables: SetVariables,
                                     playerManager: PlayerManager, callback: AutoMarkCallback
        ): Collection<OccasionalTrigger> {
            val triggerDefs = occasionalTriggerRegistry[goalId] ?: return emptySet()
            return triggerDefs.map { OccasionalTrigger(goalId, spaceId, variables, playerManager, callback, it) }
        }

        val allAutomatedGoals = occasionalTriggerRegistry.keys
    }

    override val revertible = false
    private val taskId = BingoPlugin.server.scheduler.scheduleSyncRepeatingTask(
            BingoPlugin, this::invoke, triggerDefinition.ticks.toLong(), triggerDefinition.ticks.toLong())

    override fun destroy() {
        BingoPlugin.server.scheduler.cancelTask(taskId)
    }

    private fun invoke() {
        playerManager.localPlayers.forEach {
            if (triggerDefinition.function(OccasionalTriggerParameters(it, this))) {
                callback(it, spaceId, true)
            }
        }
    }
}
