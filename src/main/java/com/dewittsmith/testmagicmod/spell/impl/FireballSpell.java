package com.dewittsmith.testmagicmod.spell.impl;

import com.dewittsmith.testmagicmod.TestMagicMod;
import com.dewittsmith.testmagicmod.spell.Spell;
import com.dewittsmith.testmagicmod.spell.cost.ManaCost;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class FireballSpell extends Spell {

    private static final int MANA_COST = 20;
    private static final int COOLDOWN_TICKS = 60;
    private static final double PROJECTILE_SPEED = 1.5;

    public FireballSpell() {
        super(
                ResourceLocation.parse(TestMagicMod.MODID + ".fireball"),
                List.of(new ManaCost(MANA_COST)),
                COOLDOWN_TICKS
        );
    }

    @Override
    protected boolean executeSpell(ServerPlayer player) {
        try {
            Vec3 lookVec = player.getLookAngle();
            Vec3 startPos = player.getEyePosition().add(lookVec.scale(1.0));

            SmallFireball fireball = new SmallFireball(player.level(), player,
                    lookVec.x * PROJECTILE_SPEED,
                    lookVec.y * PROJECTILE_SPEED,
                    lookVec.z * PROJECTILE_SPEED);

            fireball.setPos(startPos.x, startPos.y, startPos.z);

            player.level().addFreshEntity(fireball);

            return true;
        } catch (Exception e) {
            TestMagicMod.LOGGER.error("Failed to cast fireball spell", e);
            return false;
        }
    }
}