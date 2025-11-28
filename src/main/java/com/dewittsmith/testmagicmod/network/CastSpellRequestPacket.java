package com.dewittsmith.testmagicmod.network;

import com.dewittsmith.testmagicmod.TestMagicMod;
import com.dewittsmith.testmagicmod.manager.SpellCastManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.MainThreaded;
import org.zeith.hammerlib.net.PacketContext;

/**
 * Packet sent from client to server to request casting a spell.
 */
@MainThreaded
public class CastSpellRequestPacket implements IPacket {

    private ResourceLocation spellId;

    public CastSpellRequestPacket() {
        // Default constructor for packet registration.
    }

    public CastSpellRequestPacket(ResourceLocation spellId) {
        this.spellId = spellId;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(spellId);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        spellId = buf.readResourceLocation();
    }

    @Override
    public void serverExecute(PacketContext ctx) {
        try {
            SpellCastManager.handleCastRequest(ctx.getSender(), spellId);
        } catch (Exception e) {
            TestMagicMod.LOGGER.error("Error handling spell cast request for {}", spellId, e);
        }
    }
}