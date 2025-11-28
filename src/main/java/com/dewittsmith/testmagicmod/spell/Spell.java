package com.dewittsmith.testmagicmod.spell;

import com.dewittsmith.testmagicmod.spell.cost.SpellCost;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public abstract class Spell implements ISpell {

    private final ResourceLocation id;
    private final List<SpellCost> costs;
    private final int cooldownTicks;

    protected Spell(ResourceLocation id, List<SpellCost> costs, int cooldownTicks) {
        this.id = id;
        this.costs = costs;
        this.cooldownTicks = cooldownTicks;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<SpellCost> getCosts() {
        return costs;
    }

    @Override
    public int getCooldownTicks() {
        return com.dewittsmith.testmagicmod.config.SpellConfig.SERVER.getAdjustedCooldown(cooldownTicks);
    }

    @Override
    public boolean canCast(Player player) {
        if (player.isCreative()) {
            return canCastSpecific(player);
        }

        for (SpellCost cost : costs) {
            if (!cost.canAfford(player)) {
                return false;
            }
        }

        return canCastSpecific(player);
    }

    @Override
    public boolean cast(ServerPlayer player) {
        if (!canCast(player)) {
            return false;
        }

        if (!player.isCreative()) {
            for (SpellCost cost : costs) {
                if (!cost.deduct(player)) {
                    // If any cost deduction fails, we should ideally restore previous costs.
                    // For now, just fail the cast.
                    return false;
                }
            }
        }

        return executeSpell(player);
    }

    protected boolean canCastSpecific(Player player) {
        return true;
    }

    protected abstract boolean executeSpell(ServerPlayer player);
}