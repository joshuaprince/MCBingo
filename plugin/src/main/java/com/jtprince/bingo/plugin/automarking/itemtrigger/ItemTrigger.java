package com.jtprince.bingo.plugin.automarking.itemtrigger;

import com.jtprince.bingo.plugin.MCBingoPlugin;
import com.jtprince.bingo.plugin.automarking.AutoMarkTrigger;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Defines an item or set of items that the user can collect to automatically mark a Space as
 * completed.
 */
public class ItemTrigger extends AutoMarkTrigger {
    private final ItemTriggerYaml.MatchGroup rootMatchGroup;

    public static Collection<ItemTrigger> createTriggers(String goalId, ItemTriggerYaml yml) {
        ItemTriggerYaml.MatchGroup mg = yml.get(goalId);
        if (mg == null) {
            return Collections.emptySet();
        }
        return Set.of(new ItemTrigger(mg));
    }

    public static Collection<ItemTrigger> createTriggers(String goalId) {
        return createTriggers(goalId, ItemTriggerYaml.defaultYaml());
    }

    @Override
    public void destroy() {
        MCBingoPlugin.instance().autoMarkListener.unregister(this);
    }

    /**
     * Determine whether the given inventory contains some items that can allow a goal to be
     * considered completed.
     */
    public boolean isSatisfiedBy(Collection<@NotNull ItemStack> inventory) {
        UT rootUT = effectiveUT(rootMatchGroup, inventory);
        return rootUT.u >= rootMatchGroup.unique && rootUT.t >= rootMatchGroup.total;
    }

    private ItemTrigger(ItemTriggerYaml.MatchGroup rootMatchGroup) {
        super();
        this.rootMatchGroup = rootMatchGroup;
    }

    /**
     * Simple container class for a "u" and "t" value. See README.md for description of these
     * values.
     */
    private static class UT {
        int u = 0;
        int t = 0;
    }

    /**
     * The effective U and T values for a Match Group reflect a combination of U and T values from
     * that match group and all of its children. At their simplest, "u" reflects how many unique
     * items in `inventory` match, and "t" reflects the total number of items that match.
     * See README.md for more details.
     */
    private static UT effectiveUT(ItemTriggerYaml.MatchGroup matchGroup,
                                  Collection<ItemStack> inventory) {
        UT ret = new UT();
        HashSet<String> seenItemNames = new HashSet<>();

        for (ItemStack itemStack : inventory) {
            if (itemStack == null) {
                continue;
            }

            String namespacedName = namespacedName(itemStack);
            if (!matchGroup.nameMatches(namespacedName)) {
                continue;
            }

            if (!seenItemNames.contains(namespacedName)) {
                if (ret.u < matchGroup.unique) {
                    ret.u++;
                }
                seenItemNames.add(namespacedName);
            }

            ret.t = Math.min(matchGroup.total, ret.t + itemStack.getAmount());
        }

        for (ItemTriggerYaml.MatchGroup child : matchGroup.children) {
            UT childUT = effectiveUT(child, inventory);
            ret.t += childUT.t;
            if (childUT.t >= child.total) {
                ret.u += childUT.u;
            }
        }

        return ret;
    }

    /**
     * Example: "minecraft:cobblestone"
     */
    private static String namespacedName(@NotNull ItemStack itemStack) {
        return itemStack.getType().getKey().toString();
    }
}
