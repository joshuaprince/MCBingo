package com.jtprince.bingo.kplugin.automark

import com.jtprince.bingo.kplugin.BingoPlugin
import com.jtprince.bingo.kplugin.board.SetVariables
import com.jtprince.bingo.kplugin.game.PlayerManager
import com.jtprince.bingo.kplugin.player.BingoPlayer
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.min
import kotlin.reflect.KClass

class ItemTrigger private constructor(
    goalId: String,
    spaceId: Int,
    variables: SetVariables,
    playerManager: PlayerManager,
    callback: AutoMarkCallback,
    private val rootMatchGroup: ItemTriggerYaml.MatchGroup,
) : AutoMarkTrigger(goalId, spaceId, variables, callback, playerManager) {
    companion object {
        fun createItemTriggers(goalId: String, spaceId: Int, variables: SetVariables,
                               playerManager: PlayerManager, callback: AutoMarkCallback,
                               yml: ItemTriggerYaml = ItemTriggerYaml.defaultYaml
        ): Collection<ItemTrigger> {
            val matchGroup = yml[goalId] ?: return emptySet()
            return setOf(ItemTrigger(goalId, spaceId, variables, playerManager, callback, matchGroup))
        }
    }

    override val revertible = true

    private val eventTypes: Collection<KClass<out Event>> = setOf(
        InventoryCloseEvent::class, EntityPickupItemEvent::class, InventoryClickEvent::class
    )

    private val listenerRegistryIds = eventTypes.map { AutoMarkBukkitListener.register(it, ::eventRaised) }

    override fun destroy() {
        listenerRegistryIds.forEach(AutoMarkBukkitListener::unregister)
    }

    /**
     * Listener callback that is called EVERY time an event of class in [eventTypes] is called
     * anywhere on the server.
     */
    private fun eventRaised(event: Event) {
        val player = EventTrigger.forWhom(playerManager, event) ?: return

        // Called back one tick later so the inventory change has applied.
        // TODO: Across all ItemTriggers, this is a lot of scheduled tasks going on for every Event
        BingoPlugin.server.scheduler.scheduleSyncDelayedTask(BingoPlugin, {
            scanInventory(player)
        }, 1)
    }

    /**
     * Scan a player's inventory and call back this ItemTrigger's callback with whether the trigger
     * is satisfied.
     */
    private fun scanInventory(player: BingoPlayer) {
        val satisfied = satisfiedBy(player.inventory)
        // Always call callback, since ItemTriggers are always revertible.
        callback(player, spaceId, satisfied)
    }

    /**
     * Returns whether a set of items meets the criteria for this Item Trigger.
     */
    private fun satisfiedBy(inventory: Collection<ItemStack>): Boolean {
        val rootUT = effectiveUT(rootMatchGroup, inventory)
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
