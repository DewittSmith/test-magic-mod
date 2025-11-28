package com.dewittsmith.testmagicmod.network;

import com.dewittsmith.testmagicmod.capability.ManaCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.MainThreaded;
import org.zeith.hammerlib.net.PacketContext;

/**
 * Packet sent from server to client to sync mana data.
 */
@MainThreaded
public class ManaSyncPacket implements IPacket {

    private float currentMana;
    private float maxMana;
    private float manaRegen;

    public ManaSyncPacket() {
        // Default constructor for packet registration.
    }

    public ManaSyncPacket(float currentMana, float maxMana, float manaRegen) {
        this.currentMana = currentMana;
        this.maxMana = maxMana;
        this.manaRegen = manaRegen;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeFloat(currentMana);
        buf.writeFloat(maxMana);
        buf.writeFloat(manaRegen);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        currentMana = buf.readFloat();
        maxMana = buf.readFloat();
        manaRegen = buf.readFloat();
    }

    @Override
    public void clientExecute(PacketContext ctx) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            player.getCapability(ManaCapability.MANA_CAPABILITY).ifPresent(manaCapability -> {
                manaCapability.setCurrentMana(currentMana);
                manaCapability.setMaxMana(maxMana);
                manaCapability.setManaRegen(manaRegen);
            });
        }
    }

    public float getCurrentMana() {
        return currentMana;
    }

    public float getMaxMana() {
        return maxMana;
    }

    public float getManaRegen() {
        return manaRegen;
    }
}