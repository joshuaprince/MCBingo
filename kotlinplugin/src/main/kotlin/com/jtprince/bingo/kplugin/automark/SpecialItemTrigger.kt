package com.jtprince.bingo.kplugin.automark

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

    override fun satisfiedBy(inventory: Collection<ItemStack>): Boolean {
        return triggerDefinition.function(SpecialItemTriggerDefinition.Parameters(inventory, this))
    }
}
