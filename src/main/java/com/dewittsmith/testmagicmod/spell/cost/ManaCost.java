package com.dewittsmith.testmagicmod.spell.cost;

import com.dewittsmith.testmagicmod.capability.ManaCapability;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ManaCost extends SpellCost {
    private final float amount;

    public ManaCost(float amount) {
        this.amount = amount;
    }

    @Override
    protected boolean canAffordImpl(Player player) {
        return ManaCapability.getCurrentMana(player) >= amount;
    }

    @Override
    protected boolean deductImpl(ServerPlayer player) {
        return ManaCapability.consumeMana(player, amount);
    }

    public float getAmount() {
        return amount;
    }
}