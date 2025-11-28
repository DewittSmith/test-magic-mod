package com.dewittsmith.testmagicmod.spell.impl;

import com.dewittsmith.testmagicmod.TestMagicMod;
import com.dewittsmith.testmagicmod.spell.Spell;
import com.dewittsmith.testmagicmod.spell.cost.ManaCost;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.List;

public class HasteSpell extends Spell {

    private static final int MANA_COST = 15;
    private static final int COOLDOWN_TICKS = 100;
    private static final int EFFECT_DURATION_TICKS = 300;
    private static final int SPEED_AMPLIFIER = 1;

    public HasteSpell() {
        super(
                ResourceLocation.parse(TestMagicMod.MODID + ".haste"),
                List.of(new ManaCost(MANA_COST)),
                COOLDOWN_TICKS
        );
    }

    @Override
    protected boolean executeSpell(ServerPlayer player) {
        try {
            MobEffectInstance speedEffect = new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED,
                    EFFECT_DURATION_TICKS,
                    SPEED_AMPLIFIER,
                    false,
                    true,
                    true
            );

            player.addEffect(speedEffect);

            return true;
        } catch (Exception e) {
            TestMagicMod.LOGGER.error("Failed to cast haste spell", e);
            return false;
        }
    }
}