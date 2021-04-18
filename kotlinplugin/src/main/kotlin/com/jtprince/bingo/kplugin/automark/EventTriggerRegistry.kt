package com.jtprince.bingo.kplugin.automark

import org.bukkit.event.Event
import kotlin.reflect.KClass

/**
 * Container for a static mapping from (a goal ID and an event type) to the condition that the
 * event must fulfill for that goal to be achieved.
 */
class EventTriggerDefinition<EventType: Event>(
    val eventType: KClass<EventType>,
    val function: (TriggerParameters<out EventType>) -> Boolean
)

/**
 * Container for all information provided to one of these functions when an event happens in game.
 */
class TriggerParameters<EventType: Event>(
    val event: EventType,
    trigger: EventTrigger,
) {
    val vars = trigger.variables
}

class EventTriggerRegistry private constructor(
    regs: Map<String, List<EventTriggerDefinition<*>>>
) : Map<String, List<EventTriggerDefinition<*>>> by regs {

    internal constructor(defs: EventTriggerRegistryBuilder.() -> Unit) : this (create(defs))

    companion object {
        fun create(defs: EventTriggerRegistryBuilder.() -> Unit)
                : Map<String, List<EventTriggerDefinition<*>>> {
            val builder = EventTriggerRegistryBuilder()
            builder.defs()
            return builder.build()
        }
    }
}

class EventTriggerRegistryBuilder {
    private val triggers = mutableMapOf<String, MutableList<EventTriggerDefinition<*>>>()

    internal inline fun <reified EventType : Event> trigger(
        vararg goalIds: String,
        noinline check: TriggerParameters<out EventType>.() -> Boolean
    ) {
        for (goalId in goalIds) {
            val lst = triggers.getOrPut(goalId) { mutableListOf() }
            lst += EventTriggerDefinition(EventType::class, check)
        }
    }

    fun build(): Map<String, List<EventTriggerDefinition<*>>> {
        return triggers.toMap()
    }
}
