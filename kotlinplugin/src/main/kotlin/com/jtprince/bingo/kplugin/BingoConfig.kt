package com.jtprince.bingo.kplugin

import com.jtprince.bingo.kplugin.player.BingoPlayer
import io.ktor.http.*
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.util.logging.Level

object BingoConfig {
    val debug: Boolean
        get() = BingoPlugin.config.getBoolean("debug", false)

    fun gameUrl(gameCode: String, forPlayer: BingoPlayer): Url? {
        val template: String? = BingoPlugin.config.getString("web_url")
        if (template == null) {
            BingoPlugin.logger.severe("No web_url is configured!")
            return null
        }

        try {
            val builder = URLBuilder(template)
            builder.path("game", gameCode)
            builder.parameters["name"] = forPlayer.name
            return builder.build()
        } catch (e: URISyntaxException) {
            BingoPlugin.logger.log(Level.SEVERE, "Misconfigured web_url", e)
        } catch (e: MalformedURLException) {
            BingoPlugin.logger.log(Level.SEVERE, "Misconfigured web_url", e)
        }

        return null
    }

    val saveWorlds: Boolean
        get() = BingoPlugin.config.getBoolean("save_worlds", true)
/*
    fun websocketUrl(gameCode: String, players: Collection<BingoPlayer>): URI? {
        val template: String? = BingoPlugin.config.getString("web_url")
        if (template == null) {
            BingoPlugin.logger.severe("No web_url is configured!")
            return null
        }

        try {
            val builder = URIBuilder(template)
            when {
                builder.scheme.equals("https", ignoreCase = true) -> {
                    builder.scheme = "wss"
                }
                builder.scheme.equals("http", ignoreCase = true) -> {
                    builder.scheme = "ws"
                }
                else -> {
                    throw URISyntaxException(template, "Scheme must be http or https")
                }
            }

            val clientId = "Plugin:" + java.lang.String.join(",", players.map(BingoPlayer::slugName))
            builder.setPathSegments("ws", "board-plugin", gameCode, clientId)
            return builder.build()
        } catch (e: URISyntaxException) {
            BingoPlugin.logger.log(Level.SEVERE, "Misconfigured web_url", e)
            return null
        }
    }*/
}
