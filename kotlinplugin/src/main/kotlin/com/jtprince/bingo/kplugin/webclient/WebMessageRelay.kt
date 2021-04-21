package com.jtprince.bingo.kplugin.webclient

import com.jtprince.bingo.kplugin.BingoPlugin
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

class WebMessageRelay() : Listener {
    init {
        BingoPlugin.server.pluginManager.registerEvents(this, BingoPlugin)
    }

    fun destroy() {
        HandlerList.unregisterAll(this)
    }

    @EventHandler
    fun onChat(event: AsyncChatEvent) {

    }
}
