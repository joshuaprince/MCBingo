package com.jtprince.bingo.kplugin

import com.jtprince.bingo.kplugin.automark.AutoMarkBukkitListener
import com.jtprince.bingo.kplugin.automark.EventTrigger
import com.jtprince.bingo.kplugin.automark.ItemTrigger
import com.jtprince.bingo.kplugin.automark.loadEventTriggers
import dev.jorel.commandapi.CommandAPI
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

        loadEventTriggers()
        ItemTrigger.createItemTriggers("jm_mushroom_stew", 3, mapOf("var" to 4), {
            Messages.basicAnnounce("space $it")
        })
        EventTrigger.createEventTriggers("jm_never_boat", 2222, mapOf()) {
            Messages.basicAnnounce("space $it")
        }
    }

    override fun onLoad() {
        val debug =  BingoConfig.debug

        if (debug) {
            logger.info("Debug mode is enabled.")
        }

        CommandAPI.onLoad(true)
    }
}
