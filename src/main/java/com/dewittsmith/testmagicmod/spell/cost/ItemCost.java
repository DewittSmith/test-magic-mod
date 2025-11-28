package com.dewittsmith.testmagicmod.spell.cost;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemCost extends SpellCost {
    private final Item item;
    private final int count;

    public ItemCost(Item item, int count) {
        this.item = item;
        this.count = count;
    }

    @Override
    protected boolean canAffordImpl(Player player) {
        int found = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == item) {
                found += stack.getCount();
                if (found >= count) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected boolean deductImpl(ServerPlayer player) {
        int remaining = count;
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (stack.getItem() == item) {
                int taken = Math.min(remaining, stack.getCount());
                stack.shrink(taken);
                remaining -= taken;
                if (remaining <= 0) {
                    return true;
                }
            }
        }
        return remaining <= 0;
    }

    public Item getItem() {
        return item;
    }

    public int getCount() {
        return count;
    }
}