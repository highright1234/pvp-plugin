package io.github.highright1234.pvpplugin.listener

import io.github.highright1234.pvpplugin.HidingUtil
import io.github.highright1234.pvpplugin.PvpPlugin
import io.github.highright1234.pvpplugin.config.PvpConfig
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

object PlayerConnectionListener: Listener {
    @EventHandler
    fun PlayerJoinEvent.on() {
        HidingUtil.hidingPlayers.forEach { user ->
            user.hidePlayer(PvpPlugin.plugin, player)
            if (PvpConfig.hidingFightingPlayers) {
                player.hidePlayer(PvpPlugin.plugin, user)
            }
        }
    }

}