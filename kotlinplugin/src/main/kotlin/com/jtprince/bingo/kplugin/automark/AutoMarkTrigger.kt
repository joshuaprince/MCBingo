package com.jtprince.bingo.kplugin.automark

import com.jtprince.bingo.kplugin.board.SetVariables

typealias AutoMarkCallback = (spaceId: Int) -> Unit

abstract class AutoMarkTrigger(
    val goalId: String,
    val spaceId: Int,
    val variables: SetVariables,
    val callback: AutoMarkCallback,
) {
    companion object Creator {
        fun createForGoal(
            goalId: String,
            spaceId: Int,
            variables: SetVariables,
            callback: AutoMarkCallback
        ): Collection<AutoMarkTrigger> {
            return (EventTrigger.createEventTriggers(goalId, spaceId, variables, callback)
                + ItemTrigger.createItemTriggers(goalId, spaceId, variables, callback))
        }
    }
}
