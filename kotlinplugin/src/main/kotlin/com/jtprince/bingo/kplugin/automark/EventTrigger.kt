package com.jtprince.bingo.kplugin.automark

import com.jtprince.bingo.kplugin.board.SetVariables
import org.bukkit.event.Event

open class EventTrigger internal constructor(
    goalId: String,
    spaceId: Int,
    variables: SetVariables,
    callback: AutoMarkCallback,
    private val triggerDefinition: TriggerDefinition<*>,
) : AutoMarkTrigger(goalId, spaceId, variables, callback) {

    companion object {
        fun createEventTriggers(goalId: String,
                                spaceId: Int,
                                variables: SetVariables,
                                callback: AutoMarkCallback
        ): Collection<EventTrigger> {
            val ret = HashSet<EventTrigger>()
            TriggerDefinition.registry[goalId]?.forEach {
                ret += EventTrigger(goalId, spaceId, variables, callback, it)
            }
            return ret
        }
    }

    init {
        AutoMarkBukkitListener.register(this, triggerDefinition.eventType)
    }

    open fun destroy() {
        AutoMarkBukkitListener.unregister(this, triggerDefinition.eventType)
    }

    open fun satisfiedBy(event: Event): Boolean {
        val params = TriggerParameters(event,  variables, this)
        return triggerDefinition.function.invoke(params as TriggerParameters<Nothing>) ?: false
    }

    fun receive(event: Event) {
        if (satisfiedBy(event)) {
            callback.invoke(spaceId)
        }
    }
}
