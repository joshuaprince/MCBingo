package com.jtprince.bingo.kplugin.automark

import com.jtprince.bingo.kplugin.player.BingoPlayer

class OccasionalTriggerDefinition(
    val ticks: Int,
    val function: (OccasionalTriggerParameters) -> Boolean
) : TriggerDslDefinition()

class OccasionalTriggerParameters(
    val player: BingoPlayer,
    trigger: OccasionalTrigger,
) {
    val vars = trigger.variables
}
