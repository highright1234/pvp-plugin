package io.github.highright1234.pvpplugin

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties

infix fun Player.killed(player: Player) = PlayerStatData.killed(this, player)

object PlayerStatData {
    lateinit var data : YamlConfiguration
    fun killed(killer: Player, player: Player) {
        val killerData = PlayerStat(killer).valueOf(killer.kit!!)
        val playerData = PlayerStat(player).valueOf(player.kit!!)
        killerData.apply {
            kills += 1
            straightKills += 1
            if (maxStraightKills < straightKills) {
                maxStraightKills = straightKills
            }
        }
        playerData.apply {
            deaths += 1
            straightKills = 0
        }
    }

    operator fun get(player: Player) = PlayerStat(player)
    class PlayerStat(private val player: Player) {

        val categories = PlayerStat::class
            .memberProperties
            .map { it.get(this) as PlayerStat.CategoryData }

        val onlySword = CategoryData(Category.ONLY_SWORD)
        val classic = CategoryData(Category.CLASSIC)
        val cpvp = CategoryData(Category.CPVP)

        fun valueOf(category: Category): CategoryData =
            when (category) {
                Category.CLASSIC ->  classic
                Category.ONLY_SWORD -> onlySword
                Category.CPVP -> cpvp
            }

        inner class CategoryData(val category: Category) {

            private val config = PlayerStatValue()
            var kills by config
            var deaths by config
            var straightKills by config
            var maxStraightKills by config

            private inner class PlayerStatValue {
                operator fun getValue(thisRef: Any?, property: KProperty<*>) : Int =
                    data.getInt("${category.categoryName}.${property.yamlName}.${player.uniqueId}")

                operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
                    data.set("${category.categoryName}.${property.yamlName}.${player.uniqueId}", value)
                }

                val KProperty<*>.yamlName: String get() {
                    var out = name
                    var index = 0
                    while (index < name.length) {
                        val char = out[index]
                        if (char.isUpperCase()) {
                            val front = out.substring(0 until index)
                            val end = out.substring(index+1 until out.length)
                            out = front + '-' + char.lowercase() + end
                        }
                        index++
                    }
                    return out
                }
            }
        }
    }
}