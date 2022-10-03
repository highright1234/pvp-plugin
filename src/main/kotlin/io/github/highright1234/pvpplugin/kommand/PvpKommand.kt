package io.github.highright1234.pvpplugin.kommand

import io.github.highright1234.pvpplugin.PvpPlugin
import io.github.highright1234.pvpplugin.suspendExecutes
import io.github.monun.kommand.PluginKommand

object PvpKommand : KommandClass {
    override fun register(pluginKommand: PluginKommand) {
        pluginKommand.register("pvp") {
            requires { isPlayer }
            then("kd") {
                suspendExecutes {
                    TODO()
                }
            }
            then("admin") {
                requires { isOp }
                then("reload") {
                    suspendExecutes {
                        PvpPlugin.plugin.reloadPvpConfig()
                        player.sendMessage("Config loaded successfully")
                    }
                }
            }
        }
    }
}