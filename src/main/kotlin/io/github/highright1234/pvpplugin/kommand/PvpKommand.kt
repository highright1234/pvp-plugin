package io.github.highright1234.pvpplugin.kommand

import io.github.highright1234.pvpplugin.PlayerStatData
import io.github.highright1234.pvpplugin.PvpPlugin
import io.github.highright1234.pvpplugin.suspendExecutes
import io.github.monun.kommand.PluginKommand
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor

object PvpKommand : KommandClass {
    override fun register(pluginKommand: PluginKommand) {
        pluginKommand.register("pvp") {
            requires { isPlayer }
            then("kd") {
                suspendExecutes {
                    val playerStat = PlayerStatData[player]
                    playerStat.categories
                        .forEach {
                            val kd = it.kills.toDouble() / it.deaths.toDouble()
                            player.sendMessage(
                                text("${it.category.categoryName}: $kd").color(NamedTextColor.GREEN)
                            )
                        }
                }
            }
            then("kit") {
                suspendExecutes {
                    TODO("킷 선택 GUI 보냄")
                }
                then("name" to string()) {
                    suspendExecutes {
                        TODO("대충 킷 선택, name 인수 타입 다르게 받기")
                    }
                }
            }
            then("admin") {
                requires { isOp }
                then("reload") {
                    suspendExecutes {
                        PvpPlugin.plugin.reloadPvpConfig()
                        player.sendMessage(
                            text("Config loaded successfully").color(NamedTextColor.GREEN)
                        )
                    }
                }
            }
        }
    }
}