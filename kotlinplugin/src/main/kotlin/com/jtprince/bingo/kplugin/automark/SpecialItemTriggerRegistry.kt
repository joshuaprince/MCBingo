package com.jtprince.bingo.kplugin.automark

import org.bukkit.inventory.ItemStack

class SpecialItemTriggerDefinition(
    val function: (SpecialItemTriggerParameters) -> Boolean
)

class SpecialItemTriggerParameters(
    val inventory: Collection<ItemStack>,
    trigger: SpecialItemTrigger,
) {
    val vars = trigger.variables
}

class SpecialItemTriggerRegistry private constructor(
    regs: Map<String, List<SpecialItemTriggerDefinition>>
) : Map<String, List<SpecialItemTriggerDefinition>> by regs {
    internal constructor(defs: SpecialItemTriggerRegistryBuilder.() -> Unit) : this (create(defs))

    companion object {
        fun create(defs: SpecialItemTriggerRegistryBuilder.() -> Unit)
                : Map<String, List<SpecialItemTriggerDefinition>> {
            val builder = SpecialItemTriggerRegistryBuilder()
            builder.defs()
            return builder.build()
        }
    }
}

class SpecialItemTriggerRegistryBuilder {
    private val triggers = mutableMapOf<String, MutableList<SpecialItemTriggerDefinition>>()

    internal fun specialItemTrigger(
        vararg goalIds: String,
        check: SpecialItemTriggerParameters.() -> Boolean
    ) {
        for (goalId in goalIds) {
            val lst = triggers.getOrPut(goalId) { mutableListOf() }
            lst += SpecialItemTriggerDefinition(check)
        }
    }

    fun build(): Map<String, List<SpecialItemTriggerDefinition>> {
        // Have to convert each MutableList to an immutable one
        return triggers.map { (goalId, list) -> goalId to list.toList() }.toMap()
    }
}
