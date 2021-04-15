package com.jtprince.bingo.kplugin.automark

import com.jtprince.bingo.kplugin.Messages
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.spigotmc.event.entity.EntityMountEvent

/**
 * Container for all Bukkit Event Listeners.
 *
 * Rather than each Goal having its own listener that hooks into Bukkit, we register all
 * Bukkit Event Listeners here. Individual goal triggers can register here to be called back
 * whenever that event occurs.
 */
object AutoMarkBukkitListener : Listener {
    private val activeEventListenerMap = HashMap<Class<out Event>, HashSet<EventTrigger>>()

    fun register(eventTrigger: EventTrigger, eventType: Class<out Event>) {
        val list = activeEventListenerMap.getOrPut(eventType) { hashSetOf() }
        list += eventTrigger
    }

    fun unregister(eventTrigger: EventTrigger, eventType: Class<out Event>) {
        val list = activeEventListenerMap.getOrPut(eventType) { hashSetOf() }
        list -= eventTrigger
    }

    private fun <EventType: Event> impulseEvent(event: EventType) {
        val triggers = activeEventListenerMap[event.javaClass] ?: return
        for (trigger in triggers) {
            if (trigger.satisfiedBy(event)) {
                Messages.basicAnnounce("Satisfied trigger ${trigger.spaceId}")
            }
        }
    }

    @EventHandler
    fun on(event: BlockBreakEvent) {
        impulseEvent(event)
    }

    @EventHandler
    fun on(event: EntityDeathEvent) {
        impulseEvent(event)
    }

    @EventHandler
    fun on(event: EntityMountEvent) {
        impulseEvent(event)
    }

    @EventHandler
    fun on(event: InventoryCloseEvent) {
        impulseEvent(event)
    }
}
