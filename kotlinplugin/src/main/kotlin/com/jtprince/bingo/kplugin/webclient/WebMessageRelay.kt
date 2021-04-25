package com.jtprince.bingo.kplugin.webclient

import com.jtprince.bingo.kplugin.BingoPlugin
import com.jtprince.bingo.kplugin.Messages
import com.jtprince.bingo.kplugin.webclient.model.WebModelGameMessage
import com.jtprince.bingo.kplugin.webclient.model.WebModelMessageRelay
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import java.util.*

class WebMessageRelay(
    private val client: WebBackedWebsocketClient
) : Listener {
    init {
        BingoPlugin.server.pluginManager.registerEvents(this, BingoPlugin)
    }

    fun destroy() {
        HandlerList.unregisterAll(this)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onChat(event: AsyncChatEvent) {
        val msg = event.composer().composeChat(event.player, event.player.displayName(), event.message())
        client.sendMessage(GsonComponentSerializer.gson().serialize(msg))
    }

    // @EventHandler(priority = EventPriority.MONITOR)  // TODO
    fun onAdvancement(event: PlayerAdvancementDoneEvent) {
        if (event.advancement.key.key.contains("recipes/")) return
        client.sendMessage(
            GsonComponentSerializer.gson().serialize(Component.translatable(event.advancement.key.toString()))
        )
    }

    // @EventHandler(priority = EventPriority.MONITOR)  // TODO
    fun onDeath(event: PlayerDeathEvent) {
        val msg = event.deathMessage() ?: return
        Messages.bingoAnnounce("msg: " + GlobalTranslator.render(msg, Locale.getDefault()))
    }

    fun receive(message: WebModelMessageRelay) {
        if (message.sender == client.clientId) {
            // Ignore our own messages (which get sent back to us)
            return
        }

        val msg = GsonComponentSerializer.gson().deserialize(message.json)
            .hoverEvent(HoverEvent.showText(Component.text("Bingo relayed from ${message.sender}")))
        BingoPlugin.server.sendMessage(msg)
    }
}
