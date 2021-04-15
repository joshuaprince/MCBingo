package com.jtprince.bingo.kplugin.automark

import com.jtprince.bingo.kplugin.board.SetVariables
import org.bukkit.event.Event
import org.bukkit.event.block.BlockBreakEvent
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions

@Suppress("FunctionName", "UNCHECKED_CAST")
open class EventTrigger internal constructor(
    goalId: String,
    spaceId: Int,
    variables: SetVariables,
    callback: AutoMarkCallback,
    private val methodHandle: KFunction<Boolean>,
) : AutoMarkTrigger(goalId, spaceId, variables, callback) {
    companion object {
        fun createEventTriggers(goalId: String,
                                spaceId: Int,
                                variables: SetVariables,
                                callback: AutoMarkCallback
        ): Collection<EventTrigger> {
            val ret = HashSet<EventTrigger>()
            for (function in EventTrigger::class.functions) {
                val anno = function.findAnnotation<EventTriggerListener>() ?: continue
                if (function.name != goalId) {
                    continue
                }
                ret += EventTrigger(goalId, spaceId, variables, callback,
                                    function as KFunction<Boolean>)
            }
            return ret
        }
    }

    private val eventClass = (methodHandle.parameters[1].type.classifier as KClass<out Event>).java
    init {
        AutoMarkBukkitListener.register(this, eventClass)
    }

    open fun destroy() {
        AutoMarkBukkitListener.unregister(this, eventClass)
    }

    open fun satisfiedBy(event: Event): Boolean {
        return methodHandle.call(this, event)
    }

    fun receive(event: Event) {
        if (satisfiedBy(event)) {
            callback.invoke(spaceId)
        }
    }

    @Target(AnnotationTarget.FUNCTION)
    annotation class EventTriggerListener

    @EventTriggerListener
    internal fun jm_never_sword(event: BlockBreakEvent): Boolean {
        return event.player.inventory.itemInMainHand.type.key.asString().contains("_sword")
    }
}
