package com.jtprince.bingo.kplugin.automark

import com.jtprince.bingo.kplugin.board.SetVariables
import org.bukkit.event.Event

open class EventTrigger internal constructor(
    goalId: String,
    spaceId: Int,
    variables: SetVariables,
    callback: AutoMarkCallback,
    private val triggerDefinition: EventTriggerDefinition<*>,
) : AutoMarkTrigger(goalId, spaceId, variables, callback) {

    companion object {
        fun createEventTriggers(goalId: String,
                                spaceId: Int,
                                variables: SetVariables,
                                callback: AutoMarkCallback
        ): Collection<EventTrigger> {
            val ret = HashSet<EventTrigger>()
            EventTriggerDefinition.registry[goalId]?.forEach {
                ret += EventTrigger(goalId, spaceId, variables, callback, it)
            }
            return ret
        }
    }

    private val listenerRegistryId = AutoMarkBukkitListener.register(triggerDefinition.eventType) {
        if (satisfiedBy(it)) {
            callback(spaceId)
        }
    }

    fun destroy() {
        AutoMarkBukkitListener.unregister(listenerRegistryId)
    }

    open fun <EventType: Event> satisfiedBy(event: EventType): Boolean {
        val params = TriggerParameters(event,  this)
        @Suppress("UNCHECKED_CAST")  // TODO figure out if this can be worked around
        return triggerDefinition.function.invoke(params as TriggerParameters<Nothing>)
    }
}
