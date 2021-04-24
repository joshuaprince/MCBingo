package com.jtprince.bingo.kplugin.webclient.model

import com.fasterxml.jackson.annotation.JsonProperty

class WebModelMessage(
    @JsonProperty("message_id") val messageId: Int,
    @JsonProperty("sender") val sender: String,
    @JsonProperty("mc_tellraw") val content_tellraw: String?,
    @JsonProperty("mc_minimessage") val content_minimessage: String?,
)
