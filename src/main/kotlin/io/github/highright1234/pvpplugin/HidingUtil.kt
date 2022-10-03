package io.github.highright1234.pvpplugin

import io.github.highright1234.pvpplugin.config.PvpConfig
import org.bukkit.entity.Player

object HidingUtil {
    val hidingPlayers = mutableListOf<Player>()
    fun hidePlayers(player: Player, except: Player) {
        PvpPlugin.plugin.server.onlinePlayers.minus(except).forEach {
            player.hidePlayer(PvpPlugin.plugin, it)
            if (PvpConfig.hidingFightingPlayers) {
                it.hidePlayer(PvpPlugin.plugin, player)
            }
        }
    }

    fun showPlayers(player: Player) {
        PvpPlugin.plugin.server.onlinePlayers.forEach {
            player.showPlayer(PvpPlugin.plugin, it)
            it.showPlayer(PvpPlugin.plugin, player)
        }
    }
}