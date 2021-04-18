package com.jtprince.bingo.kplugin

import com.jtprince.bingo.kplugin.game.BingoGame
import com.jtprince.bingo.kplugin.game.WebBackedGame
import com.jtprince.bingo.kplugin.game.WebBackedGameProto
import com.jtprince.bingo.kplugin.player.BingoPlayer
import com.jtprince.bingo.kplugin.player.BingoPlayerSingle
import com.jtprince.bingo.kplugin.player.BingoPlayerTeam
import com.jtprince.bingo.kplugin.webclient.WebHttpClient
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Team
import java.util.logging.Level

/**
 * Maintains the current game being played on this server. Allows for creating new games and
 * destroying old ones.
 */
object GameManager {
    var currentGame: BingoGame? = null
        private set

    fun destroyCurrentGame() {
        currentGame?.destroy()
        currentGame = null
    }

    fun prepareNewWebGame(creator: CommandSender,
                          settings: WebBackedGameProto.WebGameSettings) {
        destroyCurrentGame()
        val newGame = WebBackedGameProto(creator, settings)
        currentGame = newGame

        WebHttpClient.generateBoard(settings) { gameCode ->
            if (gameCode == null) {
                Messages.basicTell(creator, "Board generation failed.")
                return@generateBoard
            }
            currentGame = WebBackedGame(creator, gameCode, createBingoPlayers())
        }
    }

    private fun createBingoPlayers(): Collection<BingoPlayer> {
        val ret = HashSet<BingoPlayer>()

        // Create a mapping from Player -> Team (or null)
        val playerTeamMap = HashMap<Player, Team?>()
        for (p in Bukkit.getOnlinePlayers()) {
            playerTeamMap[p] = null
            for (team in Bukkit.getScoreboardManager().mainScoreboard.teams) {
                if (team.hasEntry(p.name)) {
                    playerTeamMap[p] = team
                }
            }
        }

        // Reverse the map direction
        val teamPlayerMap = HashMap<Team, MutableSet<OfflinePlayer>>()
        for (p in playerTeamMap.keys) {
            val team = playerTeamMap[p]
            if (team == null) {
                // No team, add the player to a BingoPlayerSingle
                ret.add(BingoPlayerSingle(p))
            } else {
                // Player is on a team. Add to teamPlayerMap
                if (!teamPlayerMap.containsKey(team)) {
                    teamPlayerMap[team] = HashSet()
                }
                teamPlayerMap[team]!!.add(p)
            }
        }

        // Create all BingoPlayerTeams.
        for ((team, players) in teamPlayerMap) {
            val bpt = BingoPlayerTeam(team.displayName(), players)
            ret.add(bpt)
        }

        return ret
    }
}
