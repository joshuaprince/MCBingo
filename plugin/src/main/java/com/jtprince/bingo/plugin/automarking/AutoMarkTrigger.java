package com.jtprince.bingo.plugin.automarking;

import com.jtprince.bingo.plugin.Space;
import com.jtprince.bingo.plugin.automarking.itemtrigger.ItemTrigger;

import java.util.Collection;
import java.util.HashSet;

public abstract class AutoMarkTrigger {
    private final Space space;

    protected AutoMarkTrigger(Space space) {
        this.space = space;
    }

    protected AutoMarkTrigger() {
        this.space = null;
    }

    public static Collection<AutoMarkTrigger> createAllTriggers(Space space) {
        HashSet<AutoMarkTrigger> set = new HashSet<>();

        set.addAll(ItemTrigger.createTriggers(space.goalId));
        set.addAll(EventTrigger.createTriggers(space));
        set.addAll(OccasionalTrigger.createTriggers(space));

        return set;
    }

    public Space getSpace() {
        return this.space;
    }

    public abstract void destroy();
}
