package com.dewittsmith.testmagicmod.spell.impl;

import com.dewittsmith.testmagicmod.TestMagicMod;
import com.dewittsmith.testmagicmod.spell.Spell;
import com.dewittsmith.testmagicmod.spell.cost.ManaCost;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public class MineSpell extends Spell {

    private static final int MANA_COST = 25;
    private static final int COOLDOWN_TICKS = 40;
    private static final double MAX_REACH_DISTANCE = 8.0;

    public MineSpell() {
        super(
                TestMagicMod.MODID, "spell.mine",
                List.of(new ManaCost(MANA_COST)),
                COOLDOWN_TICKS
        );
    }

    @Override
    protected boolean canCastSpecific(Player player) {
        HitResult hitResult = player.pick(MAX_REACH_DISTANCE, 0.0F, false);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        BlockHitResult blockHit = (BlockHitResult) hitResult;
        BlockPos pos = blockHit.getBlockPos();
        BlockState state = player.level().getBlockState(pos);

        return !state.isAir() &&
                state.getDestroySpeed(player.level(), pos) >= 0 &&
                state.getBlock() != Blocks.BEDROCK;
    }

    @Override
    protected boolean executeSpell(ServerPlayer player) {
        try {
            HitResult hitResult = player.pick(MAX_REACH_DISTANCE, 0.0F, false);
            if (hitResult.getType() != HitResult.Type.BLOCK) {
                return false;
            }

            BlockHitResult blockHit = (BlockHitResult) hitResult;
            BlockPos pos = blockHit.getBlockPos();
            ServerLevel level = player.serverLevel();
            BlockState state = level.getBlockState(pos);

            if (state.isAir() ||
                    state.getDestroySpeed(level, pos) < 0 ||
                    state.getBlock() == Blocks.BEDROCK) {
                return false;
            }

            level.destroyBlock(pos, true, player);

            return true;
        } catch (Exception e) {
            TestMagicMod.LOGGER.error("Failed to cast mine spell", e);
            return false;
        }
    }
}