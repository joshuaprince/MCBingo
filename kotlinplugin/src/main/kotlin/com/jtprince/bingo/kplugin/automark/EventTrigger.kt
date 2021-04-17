package com.jtprince.bingo.kplugin.automark

import com.jtprince.bingo.kplugin.BingoPlugin
import com.jtprince.bingo.kplugin.board.SetVariables
import com.jtprince.bingo.kplugin.board.Space
import com.jtprince.bingo.kplugin.game.PlayerManager
import com.jtprince.bingo.kplugin.player.BingoPlayer
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.BlockEvent
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.entity.PlayerLeashEntityEvent
import org.bukkit.event.hanging.HangingEvent
import org.bukkit.event.inventory.InventoryEvent
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.vehicle.VehicleEvent
import org.bukkit.event.weather.WeatherEvent
import org.bukkit.event.world.WorldEvent

open class EventTrigger internal constructor(
    goalId: String,
    spaceId: Int,
    variables: SetVariables,
    playerManager: PlayerManager,
    callback: AutoMarkCallback,
    private val triggerDefinition: EventTriggerDefinition<*>,
) : AutoMarkTrigger(goalId, spaceId, variables, callback, playerManager) {

    companion object {
        fun createEventTriggers(goalId: String, spaceId: Int, variables: SetVariables,
                                playerManager: PlayerManager, callback: AutoMarkCallback,
        ): Collection<EventTrigger> {
            val ret = HashSet<EventTrigger>()
            EventTriggerDefinition.registry[goalId]?.forEach {
                ret += EventTrigger(goalId, spaceId, variables, playerManager, callback, it)
            }
            return ret
        }
    }

    private val listenerRegistryId = AutoMarkBukkitListener.register(triggerDefinition.eventType) {
        val player = forWhom(it) ?: return@register

        if (satisfiedBy(it)) {
            callback(player, spaceId, Space.Marking.COMPLETE) // TODO
        }
    }

    override fun destroy() {
        AutoMarkBukkitListener.unregister(listenerRegistryId)
    }

    /**
     * Determine which BingoPlayer an Event is associated with, for determining who to potentially
     * automark for.
     */
    private fun forWhom(event: Event): BingoPlayer? {
        return when (event) {
            is PlayerEvent -> playerManager.bingoPlayer(event.player)
            is WorldEvent -> playerManager.bingoPlayer(event.world)
            is InventoryEvent -> playerManager.bingoPlayer(event.view.player as Player)
            is BlockEvent -> playerManager.bingoPlayer(event.block.world)
            is EntityEvent -> playerManager.bingoPlayer(event.entity.world)
            is HangingEvent -> playerManager.bingoPlayer(event.entity.world)
            is PlayerLeashEntityEvent -> playerManager.bingoPlayer(event.player)
            is VehicleEvent -> playerManager.bingoPlayer(event.vehicle.world)
            is WeatherEvent -> playerManager.bingoPlayer(event.world)
            else -> run {
                BingoPlugin.logger.warning("Received a ${event::class}, but don't know how " +
                        "to assign it to a Bingo Player")
                null
            }
        }
    }

    open fun satisfiedBy(event: Event): Boolean {
        val params = TriggerParameters(event,  this)
        @Suppress("UNCHECKED_CAST")  // TODO figure out if this can be worked around
        return triggerDefinition.function.invoke(params as TriggerParameters<Nothing>)
    }
}
