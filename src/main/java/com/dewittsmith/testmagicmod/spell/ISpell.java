package com.dewittsmith.testmagicmod.spell;

import com.dewittsmith.testmagicmod.spell.cost.SpellCost;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * Base interface for all spells in the magic mod system.
 */
public interface ISpell {

    ResourceLocation getId();

    default String getTranslationKey() {
        return "spell." + getId().getNamespace() + "." + getId().getPath();
    }

    List<SpellCost> getCosts();

    int getCooldownTicks();

    boolean canCast(Player player);

    boolean cast(ServerPlayer player);

    default boolean isEnabled() {
        return true;
    }
}