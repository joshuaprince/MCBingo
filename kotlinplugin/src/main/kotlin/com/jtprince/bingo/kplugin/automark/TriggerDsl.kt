package com.jtprince.bingo.kplugin.automark

import org.bukkit.event.Event

abstract class TriggerDslDefinition

class TriggerDslRegistry private constructor(
    private val regs: Map<String, List<TriggerDslDefinition>>
) : Map<String, List<TriggerDslDefinition>> by regs {
    internal constructor(defs: TriggerDslRegistryBuilder.() -> Unit) : this (create(defs))

    companion object {
        fun create(defs: TriggerDslRegistryBuilder.() -> Unit)
                : Map<String, List<TriggerDslDefinition>> {
            val builder = TriggerDslRegistryBuilder()
            builder.defs()
            return builder.build()
        }
    }

    val allAutomatedGoals
        get() = regs.keys
}

class TriggerDslRegistryBuilder {
    private val triggers = mutableMapOf<String, MutableList<TriggerDslDefinition>>()

    internal inline fun <reified EventType : Event> eventTrigger(
        vararg goalIds: String,
        noinline check: EventTriggerParameters<out EventType>.() -> Boolean
    ) {
        for (goalId in goalIds) {
            val lst = triggers.getOrPut(goalId) { mutableListOf() }
            lst += EventTriggerDefinition(EventType::class, check)
        }
    }

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

    internal fun specialItemTrigger(
        vararg goalIds: String,
        check: SpecialItemTriggerParameters.() -> Boolean
    ) {
        for (goalId in goalIds) {
            val lst = triggers.getOrPut(goalId) { mutableListOf() }
            lst += SpecialItemTriggerDefinition(check)
        }
    }

    fun build(): Map<String, List<TriggerDslDefinition>> {
        // Have to convert each MutableList to an immutable one
        return triggers.map { (goalId, list) -> goalId to list.toList() }.toMap()
    }
}
