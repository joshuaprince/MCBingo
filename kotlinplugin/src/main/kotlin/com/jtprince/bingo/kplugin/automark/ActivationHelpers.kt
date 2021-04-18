package com.jtprince.bingo.kplugin.automark

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.StructureType
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView

object ActivationHelpers {
    fun Location.inVillage(): Boolean {
        val nearestVillage = world.locateNearestStructure(
            this, StructureType.VILLAGE, 8, false
        ) ?: return false

        // locateNearestStructure returns Y=0. Only calculate horizontal distance
        nearestVillage.y = y
        return distance(nearestVillage) < 100
    }

    /**
     * Returns whether an ItemStack is a fully explored Map, which is defined as 99.5% or more of
     * the pixels on the map being filled.
     *
     * NOTE: This function may incorrectly return false the first time it is called on a new
     * MapView. The map must be rendered for the player so the pixels can be counted before it
     * is accurate.
     *
     * @return True if the item is a "completed" map. False if it is not a map, the map is not
     * completed, or if the map has not been rendered yet.
     */
    fun ItemStack.isCompletedMap(): Boolean {
        if (type != Material.FILLED_MAP) return false
        val meta = itemMeta as? MapMeta ?: return false
        val view = meta.mapView ?: return false
        if (view.renderers.stream().noneMatch { r: MapRenderer? -> r is MapCompletionRenderer }) {
            view.addRenderer(MapCompletionRenderer())
        }
        val renderer = view.renderers.stream()
            .filter { r: MapRenderer? -> r is MapCompletionRenderer }.findFirst().orElseThrow() as MapCompletionRenderer
        return renderer.completedPercent > 0.995
    }

    /**
     * Determine whether an Inventory contains x or more total items.
     */
    fun Inventory.containsQuantity(quantity: Int): Boolean {
        var q = 0
        for (i in contents) {
            if (i != null) {
                q += i.amount
            }
        }
        return q >= quantity
    }

    private class MapCompletionRenderer : MapRenderer() {
        var completedPercent = 0.0
        override fun render(map: MapView, canvas: MapCanvas, player: Player) {
            var totalPixels = 0
            var mappedPixels = 0
            for (x in 0..127) {
                for (y in 0..127) {
                    totalPixels++
                    if (canvas.getBasePixel(x, y).toInt() != 0) {
                        mappedPixels++
                    }
                }
            }
            completedPercent = mappedPixels.toDouble() / totalPixels
        }
    }
}
