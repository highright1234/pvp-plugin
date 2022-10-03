package io.github.highright1234.pvpplugin

import io.github.highright1234.pvpplugin.config.PvpConfig
import io.github.monun.tap.fake.PlayerInfoAction
import io.github.monun.tap.protocol.PacketSupport
import io.github.monun.tap.protocol.sendPacket
import org.bukkit.entity.Player

object HidingUtil {
    val hidingPlayers = mutableListOf<Player>()
    fun hidePlayers(player: Player, except: Player) {
        PvpPlugin.plugin.server.onlinePlayers.minus(except).forEach {
            hide(it to player)
            if (PvpConfig.hidingFightingPlayers) {
                hide(player to it)
            }
        }
    }

    fun showPlayers(player: Player) {
        PvpPlugin.plugin.server.onlinePlayers.forEach {
            show(it to player)
            show(player to it)
        }
    }

    fun hide(pair: Pair<Player, Player>) {
        val target = pair.second
        val player = pair.first
        target.hidePlayer(PvpPlugin.plugin, player)
        val packet = PacketSupport.playerInfoAction(PlayerInfoAction.ADD, player) // tab 제거
        target.sendPacket(packet)
    }

    fun show(pair: Pair<Player, Player>) {
        val target = pair.second
        val player = pair.first
        target.showPlayer(PvpPlugin.plugin, player)
        val packet = PacketSupport.playerInfoAction(PlayerInfoAction.ADD, player) // tab 추가
        target.sendPacket(packet)
    }
}