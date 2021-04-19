package com.jtprince.bingo.kplugin.automark

import com.jtprince.bingo.kplugin.board.SetVariables
import com.jtprince.bingo.kplugin.game.PlayerManager
import org.bukkit.inventory.ItemStack

class SpecialItemTrigger(
    goalId: String,
    spaceId: Int,
    variables: SetVariables,
    playerManager: PlayerManager,
    callback: AutoMarkCallback,
    private val triggerDefinition: SpecialItemTriggerDefinition,
) : ItemTrigger(goalId, spaceId, variables, playerManager, callback, null) {

    companion object {
        fun createSpecialItemTriggers(goalId: String, spaceId: Int, variables: SetVariables,
                               playerManager: PlayerManager, callback: AutoMarkCallback
        ): Collection<ItemTrigger> {
            val triggerDefs = dslRegistry[goalId] ?: return emptySet()
            return triggerDefs.filterIsInstance<SpecialItemTriggerDefinition>().map {
                SpecialItemTrigger(goalId, spaceId, variables, playerManager, callback, it)
            }
        }
    }

    override fun satisfiedBy(inventory: Collection<ItemStack>): Boolean {
        return triggerDefinition.function(SpecialItemTriggerParameters(inventory, this))
    }
}
