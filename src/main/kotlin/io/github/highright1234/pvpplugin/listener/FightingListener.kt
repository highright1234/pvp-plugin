package io.github.highright1234.pvpplugin.listener

import io.github.highright1234.pvpplugin.FightingProcessor.cutFighting
import io.github.highright1234.pvpplugin.FightingProcessor.damageProcessor
import io.github.highright1234.pvpplugin.FightingProcessor.isFighting
import io.github.highright1234.pvpplugin.FightingProcessor.isGameWorld
import io.github.highright1234.pvpplugin.FightingProcessor.pair
import io.github.highright1234.pvpplugin.config.PvpConfig
import io.github.highright1234.pvpplugin.debug
import io.github.highright1234.pvpplugin.killed
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerQuitEvent

object FightingListener : Listener {

    @EventHandler(ignoreCancelled = true)
    fun EntityDamageByEntityEvent.on() {
        if (entity !is Player || !isGameWorld(entity)) return
        val player = entity as Player

        if (damager !is Player) {
            if (damager !is Projectile) { // 활 때문에
                isCancelled = true
            }
            return
        }
        val whoAttacked = damager as Player
        val action =
            if (whoAttacked.inventory.itemInMainHand.type == Material.AIR) PvpConfig.StartAction.HAND
            else  PvpConfig.StartAction.CLOSE_WEAPON
        isCancelled = damageProcessor(player, whoAttacked, action)
    }

    @EventHandler(ignoreCancelled = true)
    fun EntityDamageEvent.on() {
        if (!isGameWorld(entity) || this is EntityDamageByEntityEvent || entity !is Player) return // 이거는 위에서 처리함
        val player = entity as Player
        if (!player.isFighting) return
        isCancelled = true
    }


    @EventHandler(ignoreCancelled = true)
    fun PlayerDeathEvent.on() {
        val killer = player.killer ?: player.pair
        debug("${player.name} is dead, killer: ${killer?.name}")
        killer ?: return
        cutFighting(player, killer)
        killer killed player
    }

    @EventHandler
    fun PlayerQuitEvent.on() {
        player.pair ?: return
        player.health = 0.0 // 위로 전달됨
    }

    @EventHandler(ignoreCancelled = true)
    fun ProjectileHitEvent.on() {
        if (entity.shooter !is Player || hitEntity == null || hitEntity !is Player) return
        if (!isGameWorld(entity)) return
        val shooter = entity.shooter as Player
        val whoDamaged = hitEntity as Player
        isCancelled = damageProcessor(shooter, whoDamaged, PvpConfig.StartAction.BOW)
    }
}