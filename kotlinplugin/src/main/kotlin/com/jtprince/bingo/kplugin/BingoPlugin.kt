package com.jtprince.bingo.kplugin

import com.jtprince.bingo.kplugin.automark.AutoMarkBukkitListener
import com.jtprince.bingo.kplugin.automark.EventTrigger
import com.jtprince.bingo.kplugin.automark.ItemTrigger
import dev.jorel.commandapi.CommandAPI
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin

val BingoPlugin: BingoPluginClass
    get() = pluginInstance
private lateinit var pluginInstance: BingoPluginClass

class BingoPluginClass : JavaPlugin() {
    init {
        pluginInstance = this
    }

    override fun onEnable() {
        CommandAPI.onEnable(this)
        Commands.registerCommands()
        saveDefaultConfig()

//        server.pluginManager.registerEvent(Event::class.java, AutoMarkBukkitListener, EventPriority.MONITOR, this)
        server.pluginManager.registerEvents(AutoMarkBukkitListener, this)

        ItemTrigger.createItemTriggers("jm_mushroom_stew", 3, mapOf("var" to 4))
    }

    override fun onLoad() {
        val debug =  BingoConfig.debug

        if (debug) {
            logger.info("Debug mode is enabled.")
        }

        CommandAPI.onLoad(true)
    }
}
