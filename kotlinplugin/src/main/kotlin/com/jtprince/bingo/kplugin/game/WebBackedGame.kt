package com.jtprince.bingo.kplugin.game

import com.jtprince.bingo.kplugin.BingoConfig
import com.jtprince.bingo.kplugin.BingoPlugin
import com.jtprince.bingo.kplugin.Messages
import com.jtprince.bingo.kplugin.board.Space
import com.jtprince.bingo.kplugin.player.BingoPlayer
import com.jtprince.bingo.kplugin.webclient.WebBackedWebsocketClient
import com.jtprince.bingo.kplugin.webclient.WebsocketRxMessage
import com.jtprince.bingo.kplugin.webclient.model.WebModelBoard
import com.jtprince.bingo.kplugin.webclient.model.WebModelPlayerBoard
import org.bukkit.command.CommandSender

class WebBackedGame(
    creator: CommandSender,
    gameCode: String,
    players: Collection<BingoPlayer>
) : BingoGame(creator, gameCode, players) {

    override var state: State = State.PREPARING
    private val websocketClient = WebBackedWebsocketClient(
        gameCode, BingoConfig.websocketUrl(gameCode, players), this::receiveMessage,
        this::receiveFailedConnection)
    private val playerBoardCache = players.associateWith(::PlayerBoardCache)

    init {
        websocketClient.connect()
    }

    override fun destroy() {
        websocketClient.destroy()
    }

    override fun signalStart() {
        websocketClient.sendStartGame()
    }

    override fun signalEnd() {
        websocketClient.sendEndGame()
    }

    override fun receiveAutomark(bingoPlayer: BingoPlayer, spaceId: Int, marking: Space.Marking) {
        /* Not filtered - first must filter to ensure no excessive backend requests. */
        if (playerBoardCache[bingoPlayer]?.canSendMarking(spaceId, marking) != true) {
            return
        }

        websocketClient.sendMarkSpace(bingoPlayer.name, spaceId, marking.value)
    }

    private fun receiveMessage(msg: WebsocketRxMessage) {
        msg.board?.run(this::receiveBoard)
        msg.pboards?.run(this::receivePlayerBoards)
    }

    private fun receiveFailedConnection() {
        state = State.FAILED
        Messages.announceGameFailed()
        // TODO: `/bingo retry` command?
    }

    private fun receiveBoard(board: WebModelBoard) {
        if (spaces.isNotEmpty()) {
            BingoPlugin.logger.warning("Received another board, ignoring it.")  // TODO
            return
        }

        for (webSpace in board.spaces) {
            val newSpace = Space(webSpace.spaceId, webSpace.goalId, webSpace.goalType,
                webSpace.text, webSpace.variables)
            spaces[newSpace.spaceId] = newSpace
            newSpace.startListening(playerManager, this::receiveAutomark)
        }

        val autoSpaces = spaces.values.filter(Space::automarking)
        BingoPlugin.logger.info("Automated goals: " +
                autoSpaces.map(Space::goalId).joinToString(", "))

        val autoMarkMap = HashMap<String, List<Int>>()
        for (player in playerManager.localPlayers) {
            autoMarkMap[player.name] = autoSpaces.map(Space::spaceId)
        }
        websocketClient.sendAutoMarks(autoMarkMap)
    }

    private fun receivePlayerBoards(playerBoards: List<WebModelPlayerBoard>) {
        for (pb in playerBoards) {
            // TODO Do we even need remote players any more?
            val player = playerManager.bingoPlayer(pb.playerName, false) ?: continue
            playerBoardCache[player]?.updateFromWeb(pb)
        }
    }
}
