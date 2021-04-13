package com.jtprince.bingo.kplugin

import com.jtprince.bingo.kplugin.automark.AutoMarkBukkitListener
import dev.jorel.commandapi.CommandAPI
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused")
object BingoPlugin : JavaPlugin() {
    override fun onEnable() {
        CommandAPI.onEnable(this)
        saveDefaultConfig()

        server.pluginManager.registerEvents(AutoMarkBukkitListener, this)
    }

    override fun onLoad() {
        val debug =  BingoConfig.debug

        if (debug) {
            logger.info("Debug mode is enabled.")
        }

        CommandAPI.onLoad(true)
    }
}
