# Item Trigger Specification Format

Automated item triggers allow the Bukkit plugin to automatically determine 
whether a player has item(s) in their inventory that satisfy a space on the 
board. Item collection goals can take many forms. Therefore, it is important 
that the format for specifying exactly what the player needs is flexible.

## The item_triggers.yml file

All item triggers are defined in the item_triggers.yml file, located at
```
plugin/src/main/java/com/jtprince/bingo/plugin/automarking/item_triggers.yml
```

At the file's root is a dictionary called `item_triggers`. Keys in this
dictionary match goal IDs. The value of each key describes what items a 
player needs to satisfy that goal.

## Format by Example

98% of item collection goals should resemble one of the following formats.

### One item

The simplest possible item collection, the player needs one single item in 
their inventory. The `name` key must match a Minecraft namespaced item ID.

```yaml
  jtp_book_quill:
    # Goal: "Book and Quill"
    name: minecraft:writable_book
```

### One item of multiple possible types

The player has choices as to exactly which item they collect.

```yaml
  jtp_chicken:
    # Goal: "Bucket of Fish"
    name:
      - minecraft:cod_bucket
      - minecraft:salmon_bucket
      - minecraft:pufferfish_bucket
      - minecraft:tropical_fish_bucket
```

### Multiple items of the same type

The player must collect a specific number (more than 1) of a single item.

```yaml
  jtp_cobblestone_stack:
    # Goal: "64 Cobblestone"
    total: 64
    name: minecraft:cobblestone
```

This works with multiple possible types as well. In the below example, 8 
regular and 2 sticky pistons will satisfy the requirement. 

```yaml
  jtp_pistons:
    # Goal: "10 Pistons"
    total: 10
    name: 
      - minecraft:piston
      - minecraft:sticky_piston
```

### Multiple items of different types

The player must collect many unique items, one of each. If the player 
collects more than 1 of a type of item, it only counts once.

```yaml
  jtp_diamond_armor:
    # Goal: "Full Diamond Armor"
    unique: 4
    name:
      - minecraft:diamond_helmet
      - minecraft:diamond_chestplate
      - minecraft:diamond_leggings
      - minecraft:diamond_boots
```

In the above example, the player cannot collect 3 pairs of boots and a 
helmet, or any other combination. There must be one of each piece of armor 
in their inventory.

### Variable numbers of items

Variables may be used for any value that accepts a number. The variable must 
be defined and set in the backend with matching `var` tags.

```yaml
  jtp_ender_pearls:
    # Goal: "$var Ender Pearls"
    total: $var
    name: minecraft:ender_pearl
```

### Regular Expression names

Often, the namespaced IDs of similar items will follow a pattern. The `name`
field in item triggers allows for regular expression matching. Different item
names that match the regular expression count once **each** towards `unique` 
requirements.

```yaml
  jtp_saplings:
    # Goal: "$var Different Saplings"
    unique: $var
    name: minecraft:.*_sapling
```

### Item Groups

Sometimes we want a more complex relationship that involves multiple items of
different types. For instance, if we want the player to have to collect "5
unique foods", they should not be able to count "cooked chicken" and "raw
chicken" as 2 foods. Item groups are an advanced mechanism for implementing 
such a goal.

```yaml
  jtp_different_foods:
    # Goal: "5 Unique Foods"
    unique: 5
    name:
      - minecraft:apple
      - minecraft:beetroot
      # ... all the rest of the foods ...
      - minecraft:tropical_fish
    groups:
      - name:
          - minecraft:baked_potato
          - minecraft:potato
      - name:
          - minecraft:cooked_beef
          - minecraft:beef
      - name:
          - minecraft:chicken
          - minecraft:cooked_chicken
      # ... all the rest of the cooked/raw food pairs ...
```

With the above configuration, any item that is listed under the first `name` 
key will count once towards the requirement of 5 foods, regardless of how 
many of that food the player has. Under the `groups` key, any set of items 
under a single `name` key can only count once towards those 5. For example, 
a player that has an apple, beetroot, tropical fish, potato, and baked 
potato will not activate the goal. If the player drops the potato and picks 
up a chicken, the goal will activate.

### More than one each of multiple items

The [Multiple items of different types](#Multiple items of different types) tag
only describes how to add items where only 1 each is needed. Item groups can 
allow us to expand this. This takes advantage of the way the `unique` and 
`total` tags work together - within an item group, if a `total` key is 
specified, that item group will only count towards the outer `unique` if the 
player has `total` items.

```yaml
  jm_roses_dandelions:
    # Goal: "10 Roses and 15 Dandelions"
    unique: 2
    groups:
      - name: minecraft:rose
        total: 10
      - name: minecraft:dandelion
        total: 15
```

### Item attributes (enchantments, potion effects, durability)

No support yet - it's coming.

## Internal Mechanism

If none of the above examples allow you to implement the item trigger you 
are looking for, it may help to understand exactly how inventories are 
scanned when given an item trigger definition.

### Item group tree

The internal representation of every item trigger is a tree of ItemMatchGroup 
objects. An ItemMatchGroup object consists of the following fields:

```
    +---------------+
    | name: Regex[] |
    | unique: int   |
    | total:  int   |
    | children: []  |
    +---------------+
```

Every item trigger specified in the YAML has a "root" ItemMatchGroup node that
is derived from the keys directly under the goal ID key. When `groups` is
specified, each value in the `groups` list corresponds to another ItemMatchGroup
that is placed in the enclosing key's `children` list.

All keys are optional in each ItemMatchGroup. When not specified, `name`
and `children` (`groups`) default to an empty list, `unique` and `total` 
default to 1.

The satisfaction of an item trigger is determined on the basis of an entire
inventory. The inventory is scanned one item stack at a time, during which
period each ItemMatchGroup maintains running counters `u` and `t`. These
counters correspond to the `unique` and `total` properties respectively, and
obey the following rules:

- If the namespaced ID of an item stack does not match any regular expressions
  in `name`, then this item stack cannot increment either `u` or `t`.
- `t` is incremented as many times as there are items in this stack.
- `t` may not be incremented past `total`.
- `u` is incremented once for each unique namespaced ID that is found in the 
  inventory.
- `u` may not be incremented past `unique`.

The root ItemMatchGroup is considered satisfied if and only if `t >= total` 
and `u >= unique`.
