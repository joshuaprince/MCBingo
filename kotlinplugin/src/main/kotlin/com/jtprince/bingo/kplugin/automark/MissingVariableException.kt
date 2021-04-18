package com.jtprince.bingo.kplugin.automark

class MissingVariableException(val varname: String?) : Exception() {
    override fun toString(): String {
        return "Missing variable $varname"
    }
}
