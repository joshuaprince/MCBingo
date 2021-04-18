package com.jtprince.bingo.kplugin.automark

import com.jtprince.bingo.kplugin.BingoPlugin
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.plugin.EventExecutor
import kotlin.reflect.KClass

typealias BukkitListenerCallback = (event: Event) -> Unit
typealias EventClass = KClass<out Event>

/**
 * Container for all Bukkit Event Listeners.
 *
 * Rather than each Goal having its own listener that hooks into Bukkit, we register all
 * Bukkit Event Listeners here. Individual goal triggers can register here to be called back
 * whenever that event occurs.
 */
object AutoMarkBukkitListener : Listener, EventExecutor {
    /** Event Classes that we already have a Bukkit Listener for. */
    private val registeredEventTypes = HashSet<EventClass>()

    /** Maps each Event Class to all of the listeners we have registered for that event. */
    private val activeEventListenerMap = HashMap<EventClass, HashSet<BukkitListenerCallback>>()

    /** Maps the ID given in [register] to a callback so we can unregister it. */
    private val regEventListeners = HashMap<Int, Pair<EventClass, BukkitListenerCallback>>()

    /** Maps the ID given in [registerInventoryChange] to a callback so we can unregister. */
    private val regInvListeners = HashMap<Int, BukkitListenerCallback>()

    private var lastId = 0
    private val inventoryChangeEventClasses = setOf(
        InventoryCloseEvent::class, EntityPickupItemEvent::class, InventoryClickEvent::class,
        PlayerDropItemEvent::class
    )

    /**
     * Listen for an Event on this server, causing a callback to be executed whenever that event
     * happens.
     * @return A unique "registry ID" that can be used to unregister this callback.
     */
    fun register(eventType: EventClass, callback: BukkitListenerCallback): Int {
        listenToEvent(eventType)

        val list = activeEventListenerMap.getOrPut(eventType) { hashSetOf() }
        list += callback

        /* Give the caller a unique identifier that they can use to unregister */
        val id = lastId++
        regEventListeners[id] = eventType to callback
        return id
    }

    /**
     * Listen for any event that changes any Player's inventory, causing a callback to be executed
     * whenever an inventory changes.
     * @return A unique "registry ID" that can be used to unregister this callback.
     */
    fun registerInventoryChange(callback: BukkitListenerCallback): Int {
        inventoryChangeEventClasses.forEach(::listenToEvent)

        val id = lastId++
        regInvListeners[id] = callback
        return id
    }

    /**
     * Stop passing an event to a callback.
     * @param registryId A registry ID provided by a [register] call.
     */
    fun unregister(registryId: Int) {
        val eventMapEntry = regEventListeners[registryId]
        val invMapEntry = regInvListeners[registryId]

        when {
            eventMapEntry != null -> {
                activeEventListenerMap[eventMapEntry.first]!! -= eventMapEntry.second
                regEventListeners -= registryId
            }
            invMapEntry != null -> {
                regInvListeners -= registryId
            }
            else -> {
                BingoPlugin.logger.severe("Tried to unregister unknown listener registry ID $registryId")
                return
            }
        }
    }

    /**
     * Bukkit hook for receiving an Event.
     */
    override fun execute(listener: Listener, event: Event) {
        for (trigger in activeEventListenerMap[event::class] ?: emptySet()) {
            trigger.invoke(event)
        }

        if (event::class in inventoryChangeEventClasses) {
            receiveInventoryChange(event)
        }
    }

    /**
     * Ensure that we have a Bukkit listener for this event class.
     */
    private fun listenToEvent(eventType: EventClass) {
        if (!registeredEventTypes.contains(eventType)) {
            Bukkit.getServer().pluginManager.registerEvent(
                eventType.java, this, EventPriority.MONITOR, this, BingoPlugin
            )
            registeredEventTypes += eventType
        }
    }

    /**
     * Intermediate callback for inventory change events. Delay them by 1 tick so that the inventory
     * is updated when any callbacks receive the event.
     */
    private fun receiveInventoryChange(event: Event) {
        BingoPlugin.server.scheduler.scheduleSyncDelayedTask(BingoPlugin, {
            for (callback in regInvListeners.values) {
                callback(event)
            }
        }, 1)
    }
}
