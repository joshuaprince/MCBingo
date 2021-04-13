package com.jtprince.bingo.kplugin.board

import com.fasterxml.jackson.annotation.JsonSubTypes

class Board(val obscured: Boolean, val shape: String, spaces: List<Space>) {
    val spaces: Map<Int, Space> = spaces.associateBy(Space::spaceId)
}
