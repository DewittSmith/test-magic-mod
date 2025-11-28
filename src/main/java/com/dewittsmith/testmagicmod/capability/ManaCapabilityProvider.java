package com.dewittsmith.testmagicmod.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Capability provider for mana.
 */
public class ManaCapabilityProvider implements ICapabilitySerializable<CompoundTag> {

    private final ManaCapability manaCapability = new ManaCapability();
    private final LazyOptional<ManaCapability> lazyOptional = LazyOptional.of(() -> manaCapability);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ManaCapability.MANA_CAPABILITY) {
            return lazyOptional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return manaCapability.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        manaCapability.deserializeNBT(nbt);
    }

    public void invalidate() {
        lazyOptional.invalidate();
    }
}