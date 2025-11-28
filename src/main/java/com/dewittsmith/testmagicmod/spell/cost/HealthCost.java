package com.dewittsmith.testmagicmod.spell.cost;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class HealthCost extends SpellCost {
    private final float amount;

    public HealthCost(float amount) {
        this.amount = amount;
    }

    @Override
    protected boolean canAffordImpl(Player player) {
        // Prevent killing the player with health costs.
        return player.getHealth() > amount + 1.0f;
    }

    @Override
    protected boolean deductImpl(ServerPlayer player) {
        if (canAffordImpl(player)) {
            player.setHealth(player.getHealth() - amount);
            return true;
        }
        return false;
    }

    public float getAmount() {
        return amount;
    }
}