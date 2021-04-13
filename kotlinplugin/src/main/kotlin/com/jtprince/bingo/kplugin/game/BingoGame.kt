package com.jtprince.bingo.kplugin.game

import org.bukkit.command.CommandSender

abstract class BingoGame(val creator: CommandSender) {
    enum class State {
        INITIALIZING,
        PREPARING,
        READY,
        RUNNING,
        DONE,
        FAILED,
    }

    lateinit var gameCode: String
        protected set

    var state = State.INITIALIZING
    val playerManager = PlayerManager()

    // @OverridingMethodsMustInvokeSuper
    fun destroy() { }

    abstract fun signalStart()
    abstract fun signalEnd()
}
