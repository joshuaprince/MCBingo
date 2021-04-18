package com.jtprince.bingo.kplugin.automark

import org.bukkit.Material

val specialItemTriggerRegistry = SpecialItemTriggerRegistry {
    specialItemTrigger("jm_enchanted_gold_sword") { inventory.any {
        it.type == Material.GOLDEN_SWORD && it.enchantments.isNotEmpty()
    }}
}
