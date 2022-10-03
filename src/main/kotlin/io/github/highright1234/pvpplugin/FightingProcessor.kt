package io.github.highright1234.pvpplugin

import com.github.shynixn.mccoroutine.bukkit.launch
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import io.github.highright1234.pvpplugin.config.PvpConfig
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.entity.Entity
import org.bukkit.entity.Player


val Player.kit: Category? get() = FightingProcessor.kitMap[this]

@Suppress("UnstableApiUsage")
object FightingProcessor {
    val kitMap = mutableMapOf<Player, Category>()
    private var fightingGraph: MutableGraph<Player> = GraphBuilder.undirected().build()

    val Player.isFighting get() = fightingGraph.nodes().contains(this)
    val Player.pair : Player? get() = if (isFighting) fightingGraph.adjacentNodes(this).first() else null

    // 둘다 안싸우는지 확인
    fun isFighting(vararg players : Player) = players.any { fightingGraph.nodes().contains(it) }

    // 둘이서 싸우는지 확인
    private fun isPair(player1: Player, player2: Player) =
        fightingGraph.hasEdgeConnecting(player1, player2)

    fun isGameWorld(entity: Entity): Boolean =
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

    fun cutFighting(player1: Player, player2: Player) {
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
    fun damageProcessor(whoDamaged: Player, whoAttacked: Player, action: PvpConfig.StartAction): Boolean =
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
}