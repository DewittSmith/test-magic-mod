package com.dewittsmith.testmagicmod.network;

import com.dewittsmith.testmagicmod.manager.CooldownManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.MainThreaded;
import org.zeith.hammerlib.net.PacketContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Packet sent from server to client to sync spell cooldown data.
 */
@MainThreaded
public class CooldownSyncPacket implements IPacket {

    private Map<ResourceLocation, Long> cooldowns;

    public CooldownSyncPacket() {
        this.cooldowns = new HashMap<>();
    }

    public CooldownSyncPacket(Map<ResourceLocation, Long> cooldowns) {
        this.cooldowns = cooldowns;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(cooldowns.size());
        for (Map.Entry<ResourceLocation, Long> entry : cooldowns.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            buf.writeLong(entry.getValue());
        }
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        int size = buf.readInt();
        cooldowns = new HashMap<>();
        for (int i = 0; i < size; i++) {
            ResourceLocation spellId = buf.readResourceLocation();
            long cooldownEnd = buf.readLong();
            cooldowns.put(spellId, cooldownEnd);
        }
    }

    @Override
    public void clientExecute(PacketContext ctx) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            CooldownManager.updateClientCooldowns(player, cooldowns);
        }
    }

    public Map<ResourceLocation, Long> getCooldowns() {
        return cooldowns;
    }
}