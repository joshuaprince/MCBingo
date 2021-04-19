package com.jtprince.bingo.kplugin.automark.dsl

import com.jtprince.bingo.kplugin.automark.AutoMarkCallback
import com.jtprince.bingo.kplugin.automark.item.ItemTrigger
import com.jtprince.bingo.kplugin.board.SetVariables
import com.jtprince.bingo.kplugin.game.PlayerManager
import org.bukkit.inventory.ItemStack

class SpecialItemTrigger internal constructor(
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
        return triggerDefinition.function(SpecialItemTriggerDefinition.Parameters(inventory, this))
    }
}
