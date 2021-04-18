package com.jtprince.bingo.kplugin.automark

import com.jtprince.bingo.kplugin.player.BingoPlayer

class OccasionalTriggerDefinition(
    val ticks: Int,
    val function: (OccasionalTriggerParameters) -> Boolean
)

class OccasionalTriggerParameters(
    val player: BingoPlayer,
    trigger: OccasionalTrigger,
) {
    val vars = trigger.variables
}

class OccasionalTriggerRegistry private constructor(
    regs: Map<String, List<OccasionalTriggerDefinition>>
) : Map<String, List<OccasionalTriggerDefinition>> by regs {
    internal constructor(defs: OccasionalTriggerRegistryBuilder.() -> Unit) : this (create(defs))

    companion object {
        fun create(defs: OccasionalTriggerRegistryBuilder.() -> Unit)
                : Map<String, List<OccasionalTriggerDefinition>> {
            val builder = OccasionalTriggerRegistryBuilder()
            builder.defs()
            return builder.build()
        }
    }
}

class OccasionalTriggerRegistryBuilder {
    private val triggers = mutableMapOf<String, MutableList<OccasionalTriggerDefinition>>()

    internal fun occasionalTrigger(
        vararg goalIds: String,
        ticks: Int,
        check: OccasionalTriggerParameters.() -> Boolean
    ) {
        for (goalId in goalIds) {
            val lst = triggers.getOrPut(goalId) { mutableListOf() }
            lst += OccasionalTriggerDefinition(ticks, check)
        }
    }

    fun build(): Map<String, List<OccasionalTriggerDefinition>> {
        // Have to convert each MutableList to an immutable one
        return triggers.map { (goalId, list) -> goalId to list.toList() }.toMap()
    }
}
