package com.jtprince.bingo.kplugin.automark

import com.jtprince.bingo.kplugin.BingoPlugin
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
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
    private val registeredEventTypes = HashSet<EventClass>()
    private val activeEventListenerMap = HashMap<EventClass, HashSet<BukkitListenerCallback>>()
    private val identifierMap = HashMap<Int, Pair<EventClass, BukkitListenerCallback>>()

    private var lastId = 0

    /**
     * Listen for an Event on this server, causing a callback to be executed whenever that event
     * happens.
     * @return A unique "registry ID" that can be used to unregister the created event-callback
     *         mapping.
     */
    fun register(eventType: EventClass, callback: BukkitListenerCallback): Int {
        if (!registeredEventTypes.contains(eventType)) {
            Bukkit.getServer().pluginManager.registerEvent(
                eventType.java, this, EventPriority.MONITOR, this, BingoPlugin
            )
            registeredEventTypes += eventType
        }

        val list = activeEventListenerMap.getOrPut(eventType) { hashSetOf() }
        list += callback

        /* Give the caller a unique identifier that they can use to unregister */
        val id = lastId++
        identifierMap[id] = eventType to callback
        return id
    }

    /**
     * Stop passing an event to a callback.
     * @param registryId A registry ID provided by a [register] call.
     */
    fun unregister(registryId: Int) {
        val idMapEntry = identifierMap.getOrElse(registryId) {
            BingoPlugin.logger.severe("Tried to unregister unknown listener registry ID $registryId")
            return
        }

        activeEventListenerMap[idMapEntry.first]!! -= idMapEntry.second
        identifierMap -= registryId
    }

    /**
     * Bukkit hook for receiving an Event.
     */
    override fun execute(listener: Listener, event: Event) {
        val triggers = activeEventListenerMap[event::class] ?: return

        for (trigger in triggers) {
            trigger.invoke(event)
        }
    }
}
