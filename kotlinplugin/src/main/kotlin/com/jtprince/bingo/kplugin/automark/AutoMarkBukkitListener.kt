package com.jtprince.bingo.kplugin.automark

import com.jtprince.bingo.kplugin.BingoPlugin
import com.jtprince.bingo.kplugin.Messages
import net.kyori.adventure.text.Component
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import java.util.HashMap
import kotlin.reflect.KClass

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

    private fun impulseEvent(event: Event) {
        val triggers = activeEventListenerMap[event.javaClass] ?: return
        for (trigger in triggers) {
            if (trigger.satisfiedBy(event)) {
                Messages.basicAnnounce("Satisfied trigger ${trigger.spaceId}")
            }
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        impulseEvent(event)
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        impulseEvent(event)
    }
}
