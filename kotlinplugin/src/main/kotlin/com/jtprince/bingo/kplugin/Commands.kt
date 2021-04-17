package com.jtprince.bingo.kplugin

import com.jtprince.bingo.kplugin.game.BingoGame
import com.jtprince.bingo.kplugin.game.WebBackedGame
import com.jtprince.bingo.kplugin.game.WebBackedGameProto
import com.jtprince.bingo.kplugin.player.BingoPlayer
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.*
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentException
import dev.jorel.commandapi.arguments.CustomArgument.MessageBuilder
import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import net.kyori.adventure.text.Component
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object Commands {
    fun registerCommands() {
        val shapeArg = MultiLiteralArgument("square", "hexagon")
        val forcedArg = GreedyStringArgument("forcedGoals")

        // All-default form
        val prepareCmd = CommandAPICommand("prepare")
            .withAliases("p")
            .executes(CommandExecutor { sender: CommandSender, _: Array<Any> ->
                commandPrepare(sender, WebBackedGameProto.WebGameSettings())
            })
        // Game code form
        val prepareCmdGameCode = CommandAPICommand("prepare")
            .withAliases("p")
            .withArguments(StringArgument("gameCode"))
            .executes(CommandExecutor { sender: CommandSender, args: Array<Any> ->
                val settings = WebBackedGameProto.WebGameSettings(gameCode = args[0] as String)
                commandPrepare(sender, settings)
            })
        // Shape-only form
        val prepareCmdShaped = CommandAPICommand("prepare")
            .withAliases("p")
            .withArguments(shapeArg)
            .executes(CommandExecutor { sender: CommandSender, args: Array<Any> ->
                val settings = WebBackedGameProto.WebGameSettings(shape = args[0] as String)
                commandPrepare(sender, settings)
            })
        // +Forced goals
        val prepareCmdShapedDiffForced = CommandAPICommand("prepare")
            .withAliases("p")
            .withArguments(shapeArg, forcedArg)
            .executes(CommandExecutor { sender: CommandSender, args: Array<Any> ->
                val settings = WebBackedGameProto.WebGameSettings(
                    gameCode = args[0] as String,
                    forcedGoals = (args[1] as String).split(" "))
                commandPrepare(sender, settings)
            })

        val startCmd = CommandAPICommand("start")
            .withAliases("s")
            .executes(CommandExecutor { sender: CommandSender, _: Array<Any> ->
                commandStart(sender)
            })

        val endCmd = CommandAPICommand("end")
            .executes(CommandExecutor { sender: CommandSender, _: Array<Any> ->
                commandEnd(sender)
            })

        val goSpawnCmd = CommandAPICommand("go")
            .withArguments(LiteralArgument("spawn"))
            .executesPlayer(PlayerCommandExecutor { sender: Player, _: Array<Any> ->
                commandGo(sender,null)
            })
        val goCmd = CommandAPICommand("go")
            .withArguments(CustomArgument("player") { input: String ->
                val currentGame = GameManager.currentGame
                    ?: throw CustomArgumentException(
                        MessageBuilder("No games running.")
                    )
                val player = currentGame.playerManager.bingoPlayer(input, false)
                if (player == null) {
                    throw CustomArgumentException(
                        MessageBuilder("Unknown BingoPlayer: ").appendArgInput()
                    )
                } else {
                    return@CustomArgument player
                }
            }.overrideSuggestions { _ ->
                val currentGame = GameManager.currentGame
                return@overrideSuggestions currentGame?.playerManager?.localPlayers?.
                    map(BingoPlayer::slugName)?.toTypedArray() ?: arrayOf()
            })
            .executesPlayer(PlayerCommandExecutor { sender: Player, args: Array<Any> ->
                commandGo(sender, args[0] as BingoPlayer)
            })

        val debugCmd = CommandAPICommand("debug")
            .executesPlayer(PlayerCommandExecutor { sender: Player, args: Array<Any> ->
                commandDebug(sender, args.map{a -> a as String}.toTypedArray())
            })

        val root = CommandAPICommand("bingo")
        root.withSubcommand(prepareCmd)
        root.withSubcommand(prepareCmdGameCode)
        root.withSubcommand(prepareCmdShaped)
        root.withSubcommand(prepareCmdShapedDiffForced)
        root.withSubcommand(startCmd)
        root.withSubcommand(endCmd)
        root.withSubcommand(goCmd)
        root.withSubcommand(goSpawnCmd)
        if (BingoConfig.debug) {
            root.withSubcommand(debugCmd)
        }
        root.register()
    }

    private fun commandPrepare(sender: CommandSender, settings: WebBackedGameProto.WebGameSettings) {
        GameManager.prepareNewWebGame(sender, settings)
    }

    private fun commandStart(sender: CommandSender) {
        val game = GameManager.currentGame ?: run {
            Messages.basicTell(sender,
                "No game is prepared! Use /bingo prepare <gameCode>")
            return
        }

        if (game.state != BingoGame.State.READY) {
            Messages.basicTell(sender, "Game is not ready to be started!")
            return
        }

        GameManager.startGame()
    }

    private fun commandEnd(sender: CommandSender) {
        if (GameManager.currentGame == null) {
            Messages.basicTell(sender, "No game is running!")
            return
        }

        GameManager.destroyCurrentGame()
    }

    private fun commandGo(sender: Player, destination: BingoPlayer?) {
        if (destination == null) {
            sender.teleport(WorldManager.spawnWorld.spawnLocation)
        } else {
            val loc = GameManager.currentGame?.playerManager?.worldSet(destination)?.
                    world(World.Environment.NORMAL)?.spawnLocation

            if (loc == null) {
                Messages.basicTell(sender, "Couldn't send you to that player's world.")
            } else {
                sender.teleport(loc)
            }
        }
    }

    private fun commandDebug(sender: Player, args: Array<String>) {
        GameManager.currentGame?.signalStart()
    }
}
