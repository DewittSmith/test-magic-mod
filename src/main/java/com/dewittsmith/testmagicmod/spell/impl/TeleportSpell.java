package com.dewittsmith.testmagicmod.spell.impl;

import com.dewittsmith.testmagicmod.TestMagicMod;
import com.dewittsmith.testmagicmod.spell.Spell;
import com.dewittsmith.testmagicmod.spell.cost.ManaCost;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TeleportSpell extends Spell {

    private static final int MANA_COST = 30;
    private static final int COOLDOWN_TICKS = 80;
    private static final double MAX_TELEPORT_DISTANCE = 16.0;

    public TeleportSpell() {
        super(
                ResourceLocation.parse(TestMagicMod.MODID + ".teleport"),
                List.of(new ManaCost(MANA_COST)),
                COOLDOWN_TICKS
        );
    }

    @Override
    protected boolean canCastSpecific(Player player) {
        Vec3 targetPos = findTeleportDestination(player);
        return targetPos != null;
    }

    @Override
    protected boolean executeSpell(ServerPlayer player) {
        try {
            Vec3 targetPos = findTeleportDestination(player);
            if (targetPos == null) {
                return false;
            }

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

            player.teleportTo(targetPos.x, targetPos.y, targetPos.z);

            player.level().playSound(null, targetPos.x, targetPos.y, targetPos.z,
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

            return true;
        } catch (Exception e) {
            TestMagicMod.LOGGER.error("Failed to cast teleport spell", e);
            return false;
        }
    }

    private Vec3 findTeleportDestination(Player player) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 startPos = player.getEyePosition();
        Vec3 endPos = startPos.add(lookVec.scale(MAX_TELEPORT_DISTANCE));

        BlockHitResult hitResult = player.level().clip(new ClipContext(
                startPos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

        Vec3 targetPos;
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            targetPos = hitResult.getLocation();
            if (hitResult.getDirection().getStepY() > 0) {
                targetPos = targetPos.add(0, 1, 0);
            }
        } else {
            targetPos = endPos;
        }

        BlockPos landingPos = new BlockPos((int) targetPos.x, (int) Math.floor(targetPos.y), (int) targetPos.z);

        for (int y = landingPos.getY(); y >= player.level().getMinBuildHeight(); y--) {
            BlockPos checkPos = new BlockPos(landingPos.getX(), y, landingPos.getZ());
            BlockState state = player.level().getBlockState(checkPos);

            if (!state.isAir() && state.isSolidRender(player.level(), checkPos)) {
                BlockPos abovePos = checkPos.above();
                BlockPos abovePos2 = checkPos.above(2);

                if (player.level().getBlockState(abovePos).isAir() &&
                        player.level().getBlockState(abovePos2).isAir()) {
                    return new Vec3(targetPos.x, y + 1.0, targetPos.z);
                }
            }
        }

        return null;
    }
}