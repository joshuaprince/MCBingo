package com.jtprince.bingo.kplugin.automark

import org.bukkit.event.Event
import kotlin.reflect.KClass

/**
 * Container for a static mapping from (a goal ID and an event type) to the condition that the
 * event must fulfill for that goal to be achieved.
 */
class EventTriggerDefinition<EventType: Event>(
    val eventType: KClass<EventType>,
    val function: (EventTriggerParameters<out EventType>) -> Boolean
) : TriggerDslDefinition()

/**
 * Container for all information provided to one of these functions when an event happens in game.
 */
class EventTriggerParameters<EventType: Event>(
    val event: EventType,
    trigger: EventTrigger,
) {
    val vars = trigger.variables
}
