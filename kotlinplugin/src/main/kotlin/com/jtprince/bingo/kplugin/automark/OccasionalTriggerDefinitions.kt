package com.jtprince.bingo.kplugin.automark

import com.jtprince.bingo.kplugin.automark.ActivationHelpers.isCompletedMap

val occasionalTriggerRegistry = OccasionalTriggerRegistry {
    occasionalTrigger("jm_complete_map", ticks = 20) {
        player.inventory.any { i -> i.isCompletedMap() }
    }
}
