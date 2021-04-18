package com.jtprince.bingo.kplugin.automark

import org.bukkit.entity.Boat
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerLevelChangeEvent
import org.spigotmc.event.entity.EntityMountEvent

val eventTriggerRegistry = EventTriggerRegistry {
    // Never use a sword
    trigger<BlockBreakEvent>("jm_never_sword") {
        event.player.inventory.itemInMainHand.type.key.asString().contains("_sword")
    }

    // Never use an axe
    trigger<BlockBreakEvent>("jm_never_axe") {
        event.player.inventory.itemInMainHand.type.key.asString().contains("_axe")
    }

    // Kill an Iron Golem
    trigger<EntityDeathEvent>("jm_kill_golem_iron") {
        event.entityType == EntityType.IRON_GOLEM && event.entity.killer != null
    }

    // Never use (enter) boats
    trigger<EntityMountEvent>("jm_never_boat") {
        event.entity is Player && event.mount is Boat
    }

    // Level <x>
    trigger<PlayerLevelChangeEvent>("jm_level") {
        val required = vars["var"] ?: throw MissingVariableException("var")
        event.newLevel >= required
    }
}
