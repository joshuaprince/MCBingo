package com.jtprince.bingo.kplugin.automark

import org.bukkit.event.Event

interface EventTrigger {
    fun receiveEvent(event: Event)
}
