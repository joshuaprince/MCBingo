package com.jtprince.bingo.kplugin.automark

import com.jtprince.bingo.kplugin.board.SetVariables
import org.bukkit.event.Event
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.inventory.InventoryCloseEvent

class TriggerDefinition<EventType: Event>(
    val eventType: Class<EventType>,
    val function: (TriggerParameters<EventType>) -> Boolean
) {
    companion object {
        val registry = HashMap<String, HashSet<TriggerDefinition<*>>>()
    }
}

class TriggerParameters<out EventType: Event>(
    val event: EventType,
    val vars: SetVariables,
    val trigger: EventTrigger,
)

inline fun <reified EventType : Event> trigger(
    vararg goalIds: String,
    noinline check: TriggerParameters<EventType>.() -> Boolean
) {
    for (goalId in goalIds) {
        TriggerDefinition.registry.getOrPut(goalId) { hashSetOf() } +=
            TriggerDefinition(EventType::class.java, check as (TriggerParameters<EventType>) -> Boolean)
    }
}

val allTriggers = {
    trigger<BlockBreakEvent>("jm_never_sword") {
        event.player.inventory.itemInMainHand.type.key.asString().contains("_sword")
    }
}
