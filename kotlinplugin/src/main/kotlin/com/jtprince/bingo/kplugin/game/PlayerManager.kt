package com.jtprince.bingo.kplugin.game

import com.jtprince.bingo.kplugin.BingoPlugin
import com.jtprince.bingo.kplugin.WorldManager
import com.jtprince.bingo.kplugin.player.BingoPlayer
import com.jtprince.bingo.kplugin.player.BingoPlayerRemote
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

/**
 * Single-game container for all players in that game and functionality relating to them.
 */
class PlayerManager() {
    private val localPlayersMap = HashMap<OfflinePlayer, BingoPlayer>()
    private val remotePlayers = HashSet<BingoPlayer>()

    private val playerWorldSetMap = HashMap<BingoPlayer, WorldManager.WorldSet>()

    /**
     * A list of BingoPlayers that are participating in this game. This includes all
     * remote players (i.e. that are not logged in to this server).
     */
    val allPlayers: Collection<BingoPlayer>
        get() = localPlayersMap.values + remotePlayers

    /**
     * Return a list of BingoPlayers that are participating in this game and playing on this
     * server (i.e. does not include Remote players)
     */
    val localPlayers: Collection<BingoPlayer>
        get() = localPlayersMap.values

    /**
     * A list of Bukkit Player objects that are participating in this game and connected
     * to the server.
     */
    val bukkitPlayers: Collection<Player>
        get() = localPlayers.map { p -> p.bukkitPlayers }.flatten()

    /**
     * Find which BingoPlayer is associated to a Bukkit Player.
     * @param player The Bukkit Player to check.
     * @return The BingoPlayer object, or null if this Player is not part of this game.
     */
    fun bingoPlayer(player: Player): BingoPlayer? {
        return localPlayersMap[player]
    }

    /**
     * Find a BingoPlayer with a given name. If there is no player with the given name on this
     * server, a BingoPlayerRemote will be returned.
     * @param name String to look for.
     * @param createRemote If true, this function will create a RemoteBingoPlayer if the player was
     *                     not found.
     * @return The BingoPlayer object, or null if the player is not found and createRemote == false.
     */
    fun bingoPlayer(name: String, createRemote: Boolean): BingoPlayer? {
        val player = allPlayers.find { bp -> bp.name == name || bp.slugName == name }
        if (player != null || !createRemote) return player

        /* Player does not exist. Create a new one. */
        val newBingoPlayer = BingoPlayerRemote(name)
        remotePlayers.add(newBingoPlayer)
        return newBingoPlayer
    }

    /**
     * Find the set of worlds this Bingo Player is given to play in for this game.
     * @param player A Local BingoPlayer.
     * @return The player's WorldSet, or null if the player does not have one.
     */
    fun worldSet(player: BingoPlayer) : WorldManager.WorldSet? {
        if (player is BingoPlayerRemote) {
            BingoPlugin.logger.severe("Tried to get WorldSet for remote player ${player.name}")
            return null
        }

        return playerWorldSetMap[player]
    }

    /**
     * Create worlds for a given player.
     * @param player Player to create worlds for.
     */
    fun prepareWorldSet(gameCode: String, player: BingoPlayer) {
        if (player is BingoPlayerRemote) {
            BingoPlugin.logger.severe("Tried to create WorldSet for remote player ${player.name}")
            return
        }

        playerWorldSetMap[player] = WorldManager.createWorlds(
            "${gameCode}_${player.slugName}", gameCode
        )
    }

    /**
     * Unload all worlds the game is being played in.
     */
    fun destroy() {
        for (ws in playerWorldSetMap.values) {
            ws.unloadWorlds()
        }
    }
}
