package com.jtprince.bingo.kplugin.automark

import org.bukkit.inventory.ItemStack

class SpecialItemTriggerDefinition(
    val function: (SpecialItemTriggerParameters) -> Boolean
) : TriggerDslDefinition()

class SpecialItemTriggerParameters(
    val inventory: Collection<ItemStack>,
    trigger: SpecialItemTrigger,
) {
    val vars = trigger.variables
}
