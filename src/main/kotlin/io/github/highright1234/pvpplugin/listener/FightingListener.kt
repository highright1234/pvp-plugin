package io.github.highright1234.pvpplugin.listener

import com.github.shynixn.mccoroutine.bukkit.launch
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import io.github.highright1234.pvpplugin.HidingUtil
import io.github.highright1234.pvpplugin.PvpPlugin
import io.github.highright1234.pvpplugin.config.PvpConfig
import io.github.highright1234.pvpplugin.debug
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerQuitEvent

@Suppress("UnstableApiUsage")
object FightingListener : Listener {

    private var fightingGraph: MutableGraph<Player> = GraphBuilder.undirected().build()

    private val Player.isFighting get() = fightingGraph.nodes().contains(this)
    private val Player.pair : Player? get() = if (isFighting) fightingGraph.adjacentNodes(this).first() else null

    // 둘다 안싸우는지 확인
    private fun isFighting(vararg players : Player) = players.any { fightingGraph.nodes().contains(it) }

    // 둘이서 싸우는지 확인
    private fun isPair(player1: Player, player2: Player) =
        fightingGraph.hasEdgeConnecting(player1, player2)

    private fun isGameWorld(entity: Entity): Boolean =
        if (PvpConfig.activationWorld.isNotEmpty()) PvpConfig.activationWorld == entity.world.name else true

    private val cutterMap = Int2ObjectArrayMap<Job>()
    private fun cutFightingWithDelay(player1: Player, player2: Player) {
        val id = player1.hashCode() / 2 + player2.hashCode() / 2 // Int 크기 관련때매 나눠서 더한거임
        val job = PvpPlugin.plugin.launch {
            debug("${player1.name} & ${player2.name} fighting end delaying is started")
            delay(PvpConfig.pvpContinueTime)
            cutFighting(player1, player2)
        }
        cutterMap[id]?.let { debug("${player1.name} & ${player2.name} fighting's end is delayed") }
        cutterMap[id]?.cancel()
        cutterMap[id] = job
    }

    private fun cutFighting(player1: Player, player2: Player) {
        val id = player1.hashCode() / 2 + player2.hashCode() / 2 // Int 크기 관련때매 나눠서 더한거임
        fightingGraph.removeEdge(player1, player2)
        listOf(player1, player2).forEach {
            fightingGraph.removeNode(it)
            HidingUtil.showPlayers(it)
        }
        cutterMap[id]?.cancel()
        debug("${player1.name} & ${player2.name} fighting is ended")
    }

    /**
     * @return isCancelled
     */
    private fun damageProcessor(whoDamaged: Player, whoAttacked: Player, action: PvpConfig.StartAction): Boolean =
        if (!isFighting(whoDamaged, whoAttacked)) { // 둘다 안싸우고 있을때
            // pvp 시작
            var start = true
            if (PvpConfig.startAction != PvpConfig.StartAction.ALL
                && PvpConfig.startAction != action
                && action != PvpConfig.StartAction.HAND) {

                start = false

            }
            if (start) {
                debug("${whoDamaged.name} & ${whoAttacked.name} is fighting")
                fightingGraph.putEdge(whoDamaged, whoAttacked)
                if (PvpConfig.hidingPlayersWhenFighting) {
                    HidingUtil.hidePlayers(whoDamaged, whoAttacked)
                    HidingUtil.hidePlayers(whoAttacked, whoDamaged)
                }

                cutFightingWithDelay(whoDamaged, whoAttacked)
            }
            !start
        } else if (!isPair(whoDamaged, whoAttacked)) { // 둘다 싸우는데 같은팀 아닐때
            // 이상한 사람 액션 제거
            debug("${whoAttacked.name} attacked ${whoDamaged.name} but cancelled")
            true
        } else {
            // 게임 계속 지속
            cutFightingWithDelay(whoDamaged, whoAttacked)
            false
        }

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