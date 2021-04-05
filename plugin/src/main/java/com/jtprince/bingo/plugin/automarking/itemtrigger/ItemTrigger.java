package com.jtprince.bingo.plugin.automarking.itemtrigger;

import com.jtprince.bingo.plugin.MCBingoPlugin;
import com.jtprince.bingo.plugin.automarking.AutoMarkTrigger;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Defines an item or set of items that the user can collect to automatically mark a Space as
 * completed.
 */
public class ItemTrigger extends AutoMarkTrigger {
    private static ItemTriggerYaml defaultYaml;

    private final ItemTriggerYaml.MatchGroup rootMatchGroup;

    public static Collection<ItemTrigger> createTriggers(String goalId, ItemTriggerYaml yml) {
        ItemTriggerYaml.MatchGroup mg = yml.get(goalId);
        if (mg == null) {
            return Collections.emptySet();
        }
        return Set.of(new ItemTrigger(mg));
    }

    public static Collection<ItemTrigger> createTriggers(String goalId) {
        if (defaultYaml == null) {
            try {
                 defaultYaml = ItemTriggerYaml.fromFile(
                    ItemTrigger.class.getResourceAsStream("/item_triggers.yml"));
            } catch (IOException e) {
                MCBingoPlugin.logger().log(Level.SEVERE, "Could not parse item triggers yaml", e);
                return Collections.emptySet();
            }
        }

        return createTriggers(goalId, defaultYaml);
    }

    @Override
    public void destroy() {
        MCBingoPlugin.instance().autoMarkListener.unregister(this);
    }

    private ItemTrigger(ItemTriggerYaml.MatchGroup rootMatchGroup) {
        super();
        this.rootMatchGroup = rootMatchGroup;
    }

    /**
     * Determine whether the given inventory contains some items that can allow a goal to be
     * considered completed.
     */
    public boolean isSatisfied(Collection<Inventory> inventories) {
        ItemMatchGroupInstance rootInstance = createMatchGroupInstance(rootMatchGroup);
        for (Inventory inv : inventories) {
            for (ItemStack itemStack : inv.getContents()) {
                rootInstance.scan(itemStack);
            }
        }
        return rootInstance.isSatisfied();
    }

    private ItemMatchGroupInstance createMatchGroupInstance(ItemTriggerYaml.MatchGroup backing) {
        ItemMatchGroupInstance instance = new ItemMatchGroupInstance(backing);
        for (ItemTriggerYaml.MatchGroup child : backing.children) {
            instance.children.add(createMatchGroupInstance(child));
        }
        return instance;
    }

    private static class ItemMatchGroupInstance {
        private final ItemTriggerYaml.MatchGroup backing;
        private final Collection<String> seenNames = new HashSet<>();
        private int u = 0;
        private int t = 0;
        private final Collection<ItemMatchGroupInstance> children = new HashSet<>();

        ItemMatchGroupInstance(ItemTriggerYaml.MatchGroup backing) {
            this.backing = backing;
        }

        void scan(@Nullable ItemStack itemStack) {
            if (itemStack == null) {
                return;
            }

            String namespacedName = namespacedName(itemStack);
            if (backing.nameMatches(namespacedName)) {
                return;
            }

            if (!seenNames.contains(namespacedName)) {
                if (u < backing.unique) {
                    u++;
                }
                seenNames.add(namespacedName);
            }

            t = Math.min(backing.total, t + itemStack.getAmount());

            for (ItemMatchGroupInstance child : children) {
                child.scan(itemStack);
            }
        }

        boolean isSatisfied() {
            return effectiveU() >= backing.unique && effectiveT() >= backing.total;
        }

        private int effectiveU() {
            int sumU = u;
            for (ItemMatchGroupInstance child : children) {
                sumU += child.effectiveU();
            }
            return sumU;
        }

        private int effectiveT() {
            int sumT = t;
            for (ItemMatchGroupInstance child : children) {
                sumT += child.effectiveT();
            }
            return sumT;
        }
    }

    /**
     * Example: "minecraft:cobblestone"
     */
    private static String namespacedName(@NotNull ItemStack itemStack) {
        return itemStack.getType().getKey().toString();
    }
}
