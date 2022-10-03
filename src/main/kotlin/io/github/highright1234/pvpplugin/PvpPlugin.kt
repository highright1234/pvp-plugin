package io.github.highright1234.pvpplugin

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import io.github.highright1234.pvpplugin.config.ConfigClass
import io.github.highright1234.pvpplugin.config.PvpConfig
import io.github.highright1234.pvpplugin.kommand.PvpKommand
import io.github.highright1234.pvpplugin.listener.FightingListener
import io.github.highright1234.pvpplugin.listener.PlayerConnectionListener
import io.github.monun.kommand.KommandContext
import io.github.monun.kommand.KommandSource
import io.github.monun.kommand.kommand
import io.github.monun.kommand.node.KommandNode
import io.github.monun.tap.config.ConfigSupport
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class PvpPlugin : SuspendingJavaPlugin() {

    private val listeners = listOf(
        FightingListener, PlayerConnectionListener
    )

    private val kommands = listOf(
        PvpKommand
    )

    private fun file(name: String) = File(dataFolder, name)

    val configClasses : List<Pair<ConfigClass, File>> by lazy {
        listOf(
            PvpConfig to file("config.yml")
        )
    }

    companion object {
        lateinit var plugin: PvpPlugin
    }

    fun reloadPvpConfig() {
        configClasses.forEach { ConfigSupport.compute(it.first, it.second) }
        PvpConfig.isDebug = loadBoolean("is-debug")
        PvpConfig.hidingPlayersWhenFighting = loadBoolean("hiding-players-when-fighting")
        PvpConfig.hidingFightingPlayers = loadBoolean("hiding-fighting-players")

        PvpConfig.activationWorld.let(server::getWorld)
            ?: logger.info("Unknown world ${PvpConfig.activationWorld}")
    }

    override suspend fun onEnableAsync() {
        plugin = this
        reloadPvpConfig()
        logger.info("Start-action: ${PvpConfig.startAction}")
        logger.info("hiding: ${PvpConfig.hidingPlayersWhenFighting}")
//        @Suppress("MagicNumber")
        logger.info("pvp-continue-time: ${PvpConfig.pvpContinueTime/1000L}s")
        kommand {
            kommands.forEach { it.register(this) }
        }
        listeners.forEach {
            server.pluginManager.registerSuspendingEvents(it, this)
        }

        debug("Debug activated")

    }

    // for Tap boolean bug
    private fun loadBoolean(path: String) =
        YamlConfiguration.loadConfiguration(file("config.yml")).getBoolean(path)
}

fun debug(code: () -> Unit) {
    if (PvpConfig.isDebug) code()
}

fun debug(message: String) = debug {
    PvpPlugin.plugin.server.onlinePlayers.filter { it.isOp }.forEach {
        it.sendMessage("Debug-message: $message")
    }
    PvpPlugin.plugin.logger.info("Debug-message: $message")
}

fun KommandNode.suspendExecutes(executes: suspend KommandSource.(KommandContext) -> Unit) {
    executes { kommandContext ->
        PvpPlugin.plugin.launch {
            executes.invoke(this@executes, kommandContext)
        }
    }
}