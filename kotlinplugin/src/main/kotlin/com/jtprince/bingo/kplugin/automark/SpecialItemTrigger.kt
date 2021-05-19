package com.jtprince.bingo.kplugin.automark

import com.jtprince.bingo.kplugin.BingoPlugin

class SpecialItemTrigger internal constructor(
    space: AutomatedSpace,
    playerMapper: EventPlayerMapper,
    callback: Callback,
    private val triggerDefinition: SpecialItemTriggerDefinition,
) : ItemTrigger(space, playerMapper, BingoPlugin.eventRegistry, callback, null) {

    override val revertible = triggerDefinition.revertible

    override fun satisfiedBy(inventory: BingoInventory): Boolean {
        return triggerDefinition.function(
            SpecialItemTriggerDefinition.Parameters(inventory, this))
    }
}
