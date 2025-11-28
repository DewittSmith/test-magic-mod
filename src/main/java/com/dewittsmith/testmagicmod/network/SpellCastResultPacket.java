package com.dewittsmith.testmagicmod.network;

import com.dewittsmith.testmagicmod.TestMagicMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.MainThreaded;
import org.zeith.hammerlib.net.PacketContext;

import java.nio.charset.Charset;

/**
 * Packet sent from server to client to inform about spell cast results.
 */
@MainThreaded
public class SpellCastResultPacket implements IPacket {

    private ResourceLocation spellId;
    private Result result;
    private CharSequence message;
    public SpellCastResultPacket() {
        // Default constructor for packet registration.
    }

    public SpellCastResultPacket(ResourceLocation spellId, Result result, String message) {
        this.spellId = spellId;
        this.result = result;
        this.message = message;
    }

    public SpellCastResultPacket(ResourceLocation spellId, Result result) {
        this(spellId, result, "");
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(spellId);
        buf.writeEnum(result);
        buf.writeInt(message.length());
        buf.writeCharSequence(message, Charset.defaultCharset());
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        spellId = buf.readResourceLocation();
        result = buf.readEnum(Result.class);
        int messageLength = buf.readInt();
        message = buf.readCharSequence(messageLength, Charset.defaultCharset());
    }

    @Override
    public void clientExecute(PacketContext ctx) {
        if (result != Result.SUCCESS && !message.isEmpty()) {
            // TODO: Display error to player
            TestMagicMod.LOGGER.info("Spell cast failed: " + message);
        }
    }

    public ResourceLocation getSpellId() {
        return spellId;
    }

    public Result getResult() {
        return result;
    }

    public CharSequence getMessage() {
        return message;
    }

    public enum Result {
        SUCCESS,
        INSUFFICIENT_MANA,
        INSUFFICIENT_ITEMS,
        INSUFFICIENT_HEALTH,
        ON_COOLDOWN,
        INVALID_TARGET,
        SPELL_DISABLED,
        UNKNOWN_ERROR
    }
}