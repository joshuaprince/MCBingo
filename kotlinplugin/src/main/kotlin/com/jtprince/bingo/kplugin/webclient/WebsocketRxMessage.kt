package com.jtprince.bingo.kplugin.webclient

import com.fasterxml.jackson.annotation.JsonProperty
import com.jtprince.bingo.kplugin.webclient.model.WebModelBoard
import com.jtprince.bingo.kplugin.webclient.model.WebModelGameMessage
import com.jtprince.bingo.kplugin.webclient.model.WebModelMessageRelay
import com.jtprince.bingo.kplugin.webclient.model.WebModelPlayerBoard

class WebsocketRxMessage(
    @JsonProperty("board") val board: WebModelBoard?,
    @JsonProperty("pboards") val pboards: List<WebModelPlayerBoard>?,
    @JsonProperty("game_state") val gameState: String?,
    @JsonProperty("game_message") val gameMessage: WebModelGameMessage?,
    @JsonProperty("message_relay") val messageRelay: WebModelMessageRelay?,
)
