package com.jtprince.bingo.kplugin.automark

import net.kyori.adventure.text.Component
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import java.util.HashMap

object AutoMarkBukkitListener : Listener {
    private val activeEventListenerMap = HashMap<Class<out Event>, HashSet<EventTrigger>>()

    fun register(eventTrigger: EventTrigger, eventType: Class<out Event>) {
        val list = activeEventListenerMap.getOrPut(eventType) { hashSetOf() }
        list += eventTrigger
    }

    fun unregister(eventTrigger: EventTrigger, eventType: Class<out Event>) {
        val list = activeEventListenerMap.getOrPut(eventType) { hashSetOf() }
        list -= eventTrigger
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        event.player.sendMessage(Component.text(Thread.currentThread().name))
    }
}
