package io.github.highright1234.harasample

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import io.github.highright1234.harasample.config.ConfigClass
import io.github.highright1234.harasample.config.SampleConfig
import io.github.highright1234.harasample.kommand.SampleKommand
import io.github.highright1234.harasample.listener.SampleListener
import io.github.monun.kommand.KommandContext
import io.github.monun.kommand.KommandSource
import io.github.monun.kommand.kommand
import io.github.monun.kommand.node.KommandNode
import java.io.File

class HaraSample : SuspendingJavaPlugin() {

    private val listeners = listOf(
        SampleListener
    )

    private val kommands = listOf(
        SampleKommand
    )

    private fun file(name: String) = File(dataFolder, name)

    private val configClasses : List<Pair<ConfigClass, File>> by lazy {
        listOf(
            SampleConfig to file("config.yml")
        )
    }

    companion object {
        lateinit var plugin: HaraSample
    }
    override suspend fun onEnableAsync() {
        plugin = this
        configClasses.forEach { it.first.load(it.second) }
        kommand {
            kommands.forEach { it.register(this) }
        }
        listeners.forEach {
            server.pluginManager.registerSuspendingEvents(it, this)
        }
    }
}

fun KommandNode.suspendExecutes(executes: suspend KommandSource.(KommandContext) -> Unit) {
    executes { kommandContext ->
        HaraSample.plugin.launch {
            executes.invoke(this@executes, kommandContext)
        }
    }
}