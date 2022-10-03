package io.github.highright1234.pvpplugin.config

import io.github.monun.tap.config.Config

@Suppress("MagicNumber")
object PvpConfig : ConfigClass {

    @Config
    var pvpContinueTime = 60000L

    @Config
    var isDebug = false


    @Config
    var startAction = StartAction.ALL

    @Config
    var hidingPlayersWhenFighting = false

    @Config
    var hidingFightingPlayers = false

    @Config
    var activationWorld: String = ""

    enum class StartAction {
        ALL,
        HAND,
        BOW,
        CLOSE_WEAPON
    }
}