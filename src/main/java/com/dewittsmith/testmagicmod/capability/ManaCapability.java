package com.dewittsmith.testmagicmod.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Player capability for mana.
 */
public class ManaCapability implements INBTSerializable<CompoundTag> {

    public static final Capability<ManaCapability> MANA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final String CURRENT_MANA = "CurrentMana";
    public static final String MAX_MANA = "MaxMana";
    public static final String MANA_REGEN = "ManaRegen";
    public static final String LAST_REGEN_TICK = "LastRegenTick";
    private static final float DEFAULT_MAX_MANA = 100;
    private static final float DEFAULT_MANA_REGEN = 1.0f; // per second
    private float currentMana;
    private float maxMana;
    private float manaRegen;
    private long lastRegenTick;

    public ManaCapability() {
        this.currentMana = DEFAULT_MAX_MANA;
        this.maxMana = DEFAULT_MAX_MANA;
        this.manaRegen = DEFAULT_MANA_REGEN;
        this.lastRegenTick = 0;
    }

    // Static helper methods for easy access
    public static float getCurrentMana(Player player) {
        return player.getCapability(MANA_CAPABILITY)
                .map(ManaCapability::getCurrentMana)
                .orElse(0f);
    }

    public static float getMaxMana(Player player) {
        return player.getCapability(MANA_CAPABILITY)
                .map(ManaCapability::getMaxMana)
                .orElse(DEFAULT_MAX_MANA);
    }

    public static boolean consumeMana(Player player, float amount) {
        return player.getCapability(MANA_CAPABILITY)
                .map(cap -> cap.consumeMana(amount))
                .orElse(false);
    }

    public static void restoreMana(Player player, float amount) {
        player.getCapability(MANA_CAPABILITY)
                .ifPresent(cap -> cap.restoreMana(amount));
    }

    public static void tickMana(Player player, long currentTick) {
        player.getCapability(MANA_CAPABILITY)
                .ifPresent(cap -> cap.tick(currentTick));
    }

    public float getCurrentMana() {
        return currentMana;
    }

    public void setCurrentMana(float mana) {
        this.currentMana = Math.max(0f, Math.min(mana, maxMana));
    }

    public float getMaxMana() {
        return maxMana;
    }

    public void setMaxMana(float maxMana) {
        this.maxMana = Math.max(0f, maxMana);
        this.currentMana = Math.min(this.currentMana, this.maxMana);
    }

    public float getManaRegen() {
        return manaRegen;
    }

    public void setManaRegen(float regen) {
        this.manaRegen = Math.max(0f, regen);
    }

    public boolean consumeMana(float amount) {
        if (currentMana >= amount) {
            currentMana -= amount;
            return true;
        }
        return false;
    }

    public void restoreMana(float amount) {
        setCurrentMana(currentMana + amount);
    }

    public void tick(long currentTick) {
        if (lastRegenTick == 0) {
            lastRegenTick = currentTick;
            return;
        }

        long tickDiff = currentTick - lastRegenTick;

        if (tickDiff >= 20 && currentMana < maxMana) { // Regenerate every second (20 ticks)
            float regenAmount = manaRegen * (tickDiff / 20.0f);
            if (regenAmount > 0f) {
                restoreMana(regenAmount);
                lastRegenTick = currentTick;
            }
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat(CURRENT_MANA, currentMana);
        tag.putFloat(MAX_MANA, maxMana);
        tag.putFloat(MANA_REGEN, manaRegen);
        tag.putLong(LAST_REGEN_TICK, lastRegenTick);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.currentMana = tag.getFloat(CURRENT_MANA);
        this.maxMana = tag.getFloat(MAX_MANA);
        this.manaRegen = tag.getFloat(MANA_REGEN);
        this.lastRegenTick = tag.getLong(LAST_REGEN_TICK);
    }
}