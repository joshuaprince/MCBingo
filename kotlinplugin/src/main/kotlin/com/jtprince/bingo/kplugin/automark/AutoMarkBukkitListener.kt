package com.jtprince.bingo.kplugin.automark

import com.jtprince.bingo.kplugin.BingoPlugin
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.spigotmc.event.entity.EntityMountEvent

typealias BukkitListenerCallback = (event: Event) -> Unit

/**
 * Container for all Bukkit Event Listeners.
 *
 * Rather than each Goal having its own listener that hooks into Bukkit, we register all
 * Bukkit Event Listeners here. Individual goal triggers can register here to be called back
 * whenever that event occurs.
 */
object AutoMarkBukkitListener : Listener {
    private val activeEventListenerMap = HashMap<Class<out Event>, HashSet<BukkitListenerCallback>>()
    private val identifierMap = HashMap<Int, Pair<Class<out Event>, BukkitListenerCallback>>()

    private var lastId = 0

    fun register(eventType: Class<out Event>, callback: BukkitListenerCallback): Int {
        val list = activeEventListenerMap.getOrPut(eventType) { hashSetOf() }
        list += callback

        /* Give the caller a unique identifier that they can use to unregister */
        val id = lastId++
        identifierMap[id] = Pair(eventType, callback)
        return id
    }

    fun unregister(registryId: Int) {
        val idMapEntry = identifierMap.getOrElse(registryId) {
            BingoPlugin.logger.severe("Tried to unregister unknown listener registry ID $registryId")
            return
        }

        activeEventListenerMap[idMapEntry.first]!! -= idMapEntry.second
        identifierMap.remove(registryId)
    }

    private fun <EventType: Event> impulseEvent(event: EventType) {
        val triggers = activeEventListenerMap[event.javaClass] ?: return
        for (trigger in triggers) {
            trigger.invoke(event)
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
