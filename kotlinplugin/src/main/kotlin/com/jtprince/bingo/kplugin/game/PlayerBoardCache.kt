package com.jtprince.bingo.kplugin.game

import com.jtprince.bingo.kplugin.board.Space
import com.jtprince.bingo.kplugin.player.BingoPlayer
import com.jtprince.bingo.kplugin.webclient.model.WebModelPlayerBoard

/**
 * Class to maintain the Plugin-side latest known markings on a single Player's board, and decide
 * whether we can send a marking to the web.
 *
 * The data maintained here is not authoritative; the web backend maintains the authoritative
 * version.
 */
class PlayerBoardCache(val owner: BingoPlayer) {
    private val knownMarkings = HashMap<Int, Space.Marking>()

    fun updateFromWeb(webPlayerBoard: WebModelPlayerBoard) {
        for (marking in webPlayerBoard.markings) {
            knownMarkings[marking.spaceId] = Space.Marking.valueOf(marking.color)
        }
    }

    fun canSendMarking(spaceId: Int, marking: Space.Marking): Boolean {
        return knownMarkings.containsKey(spaceId) && knownMarkings[spaceId] != marking
    }
}
