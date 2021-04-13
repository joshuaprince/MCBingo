package com.jtprince.bingo.kplugin.webclient

import org.junit.jupiter.api.Test

class TestWebsocketClient {
    @Test
    fun testParseBoard() {
        val json = """{"board": {"obscured": false, "shape": "MYshape", "spaces": [
            |{"space_id": 334, "text": "hello", "goal_id": "my_goal", "variables": {}, "type": "yey"}]}}""".trimMargin()

        WebsocketMessageParser.parse(json)
    }
}
