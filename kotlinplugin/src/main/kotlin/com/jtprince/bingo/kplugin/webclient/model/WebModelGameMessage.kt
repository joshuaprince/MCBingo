package com.jtprince.bingo.kplugin.webclient.model

import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonProperty

class WebModelGameMessage(
    @JsonProperty("formatted") val formatted: Formatted,
) {
    class Formatted(
        @JsonProperty("key") val key: String,
    ) {
        val params = HashMap<String, Any>()
        @JsonAnySetter
        fun setDetail(key: String, value: Any) {
            params[key] = value
        }
    }
}
