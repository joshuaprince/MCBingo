package com.jtprince.bingo.kplugin.automark

import org.bukkit.entity.Boat
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.spigotmc.event.entity.EntityMountEvent
import kotlin.reflect.KClass

/**
 * Container for a static mapping from (a goal ID and an event type) to the condition that the
 * event must fulfill for that goal to be achieved.
 */
class EventTriggerDefinition<EventType: Event>(
    val eventType: KClass<EventType>,
    val function: (TriggerParameters<out EventType>) -> Boolean
) {
    companion object {
        val registry = HashMap<String, HashSet<EventTriggerDefinition<*>>>()
    }
}

/**
 * Container for all information provided to one of these functions when an event happens in game.
 */
class TriggerParameters<EventType: Event>(
    val event: EventType,
    val trigger: EventTrigger,
) {
    val vars = trigger.variables
}

/**
 * Creates the nice syntax for the rest of this file, allowing trigger definitions to register
 * themselves with the EventTriggerDefinition registry at plugin enable.
 */
internal inline fun <reified EventType : Event> trigger(
    vararg goalIds: String,
    noinline check: TriggerParameters<out EventType>.() -> Boolean
) {
    for (goalId in goalIds) {
        EventTriggerDefinition.registry.getOrPut(goalId) { hashSetOf() } +=
            EventTriggerDefinition(EventType::class, check)
    }
}

val loadEventTriggers = {
    // Never use a sword
    trigger<BlockBreakEvent>("jm_never_sword") {
        event.player.inventory.itemInMainHand.type.key.asString().contains("_sword")
    }

    // Never use an axe
    trigger<BlockBreakEvent>("jm_never_axe") {
        event.player.inventory.itemInMainHand.type.key.asString().contains("_axe")
    }

    // Kill an Iron Golem
    trigger<EntityDeathEvent>("jm_kill_golem_iron") {
        event.entityType == EntityType.IRON_GOLEM && event.entity.killer != null
    }

    // Never use (enter) boats
    trigger<EntityMountEvent>("jm_never_boat") {
        event.entity is Player && event.mount is Boat
    }
}
