package com.jtprince.bingo.kplugin.automark

import com.jtprince.bingo.kplugin.board.SetVariables
import com.jtprince.bingo.kplugin.game.PlayerManager
import org.bukkit.event.Event
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.min

class ItemTrigger private constructor(
    goalId: String,
    spaceId: Int,
    variables: SetVariables,
    playerManager: PlayerManager,
    callback: AutoMarkCallback,
    private val rootMatchGroup: ItemTriggerYaml.MatchGroup,
) : EventTrigger(goalId, spaceId, variables, playerManager, callback, closeEvent) {
    companion object {
        fun createItemTriggers(goalId: String, spaceId: Int, variables: SetVariables,
                               playerManager: PlayerManager, callback: AutoMarkCallback,
                               yml: ItemTriggerYaml = ItemTriggerYaml.defaultYaml
        ): Collection<ItemTrigger> {
            val matchGroup = yml[goalId] ?: return emptySet()
            return setOf(ItemTrigger(goalId, spaceId, variables, playerManager, callback, matchGroup))
        }

        val closeEvent = EventTriggerDefinition(InventoryCloseEvent::class) {
            it.trigger.satisfiedBy(it.event)
        }
    }

    override fun satisfiedBy(event: Event): Boolean {
        if (event !is InventoryCloseEvent) return false

        val rootUT = effectiveUT(rootMatchGroup, event.player.inventory.contents.filterNotNull())
        return rootUT.u >= rootMatchGroup.unique(variables)
                && rootUT.t >= rootMatchGroup.total(variables)
    }

    internal class UT {
        var u = 0
        var t = 0
    }

    /**
     * The effective U and T values for a Match Group reflect a combination of U and T values from
     * that match group and all of its children. At their simplest, "u" reflects how many unique
     * items in `inventory` match, and "t" reflects the total number of items that match.
     * See README.md for more details.
     */
    private fun effectiveUT(matchGroup: ItemTriggerYaml.MatchGroup,
                            inventory: Collection<ItemStack>): UT {
        val ret = UT()
        val seenItemNames = HashSet<String>()

        for (itemStack in inventory) {
            val namespacedName = itemStack.type.key.asString()
            if (!matchGroup.nameMatches(namespacedName)) {
                continue
            }
            if (!seenItemNames.contains(namespacedName)) {
                if (ret.u < matchGroup.unique(variables)) {
                    ret.u++
                }
                seenItemNames.add(namespacedName)
            }
            ret.t = min(matchGroup.total(variables), ret.t + itemStack.amount)
        }

        for (child in matchGroup.children) {
            val childUT = effectiveUT(child, inventory)
            ret.t += childUT.t
            if (childUT.t >= child.total(variables)) {
                ret.u += childUT.u
            }
        }

        return ret
    }
}
