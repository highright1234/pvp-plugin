package io.github.highright1234.harasample.kommand

import io.github.highright1234.harasample.config.SampleConfig
import io.github.highright1234.harasample.suspendExecutes
import io.github.monun.kommand.PluginKommand

object SampleKommand : KommandClass {
    override fun register(pluginKommand: PluginKommand) {
        pluginKommand.register("sample") {
            requires { isPlayer }
            suspendExecutes {
                sender.sendMessage(SampleConfig.message)
            }
        }
    }
}