package com.jtprince.bingo.kplugin.board

import com.fasterxml.jackson.annotation.JsonProperty

class WebSpace(@JsonProperty("goal_id") override val goalId: String,
               @JsonProperty("type") override val goalType: String,
               @JsonProperty("text") override val text: String,
               @JsonProperty("space_id") override val spaceId: Int,
               @JsonProperty("variables") override val variables: SetVariables,
) : Space()
