package com.dewittsmith.testmagicmod.spell.cost;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public abstract class SpellCost {

    public final boolean canAfford(Player player) {
        if (player.isCreative()) {
            return true;
        }
        return canAffordImpl(player);
    }

    public final boolean deduct(ServerPlayer player) {
        if (player.isCreative()) {
            return true;
        }
        return deductImpl(player);
    }

    protected abstract boolean canAffordImpl(Player player);

    protected abstract boolean deductImpl(ServerPlayer player);
}