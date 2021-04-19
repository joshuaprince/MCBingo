package com.jtprince.bingo.kplugin.automark.dsl

import com.jtprince.bingo.kplugin.automark.ActivationHelpers.isCompletedMap
import com.jtprince.bingo.kplugin.automark.MissingVariableException
import org.bukkit.Material
import org.bukkit.entity.Boat
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerLevelChangeEvent
import org.spigotmc.event.entity.EntityMountEvent

val dslRegistry = TriggerDslRegistry {
    occasionalTrigger("jm_complete_map", ticks = 20) {
        player.inventory.any { i -> i.isCompletedMap() }
    }

    specialItemTrigger("jm_enchanted_gold_sword") { inventory.any {
        it.type == Material.GOLDEN_SWORD && it.enchantments.isNotEmpty()
    }}

    // Never use a sword
    eventTrigger<BlockBreakEvent>("jm_never_sword") {
        event.player.inventory.itemInMainHand.type.key.asString().contains("_sword")
    }

    // Never use an axe
    eventTrigger<BlockBreakEvent>("jm_never_axe") {
        event.player.inventory.itemInMainHand.type.key.asString().contains("_axe")
    }

    // Kill an Iron Golem
    eventTrigger<EntityDeathEvent>("jm_kill_golem_iron") {
        event.entityType == EntityType.IRON_GOLEM && event.entity.killer != null
    }

    // Never use (enter) boats
    eventTrigger<EntityMountEvent>("jm_never_boat") {
        event.entity is Player && event.mount is Boat
    }

    // Level <x>
    eventTrigger<PlayerLevelChangeEvent>("jm_level") {
        val required = vars["var"] ?: throw MissingVariableException("var")
        event.newLevel >= required
    }
}
