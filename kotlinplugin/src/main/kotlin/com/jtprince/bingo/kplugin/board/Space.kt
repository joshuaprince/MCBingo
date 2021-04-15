package com.jtprince.bingo.kplugin.board

import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize(`as` = WebSpace::class)  // TODO: It would be nice to have this out of this file
abstract class Space {
    abstract val spaceId: Int
    abstract val goalId: String
    abstract val goalType: String
    abstract val text: String
    abstract val variables: SetVariables
}
