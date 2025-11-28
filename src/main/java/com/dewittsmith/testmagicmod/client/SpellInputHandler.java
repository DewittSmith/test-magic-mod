package com.dewittsmith.testmagicmod.client;

import com.dewittsmith.testmagicmod.TestMagicMod;
import com.dewittsmith.testmagicmod.config.SpellConfig;
import com.dewittsmith.testmagicmod.init.ModSpells;
import com.dewittsmith.testmagicmod.manager.CooldownManager;
import com.dewittsmith.testmagicmod.network.CastSpellRequestPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.zeith.hammerlib.net.Network;

/**
 * Handles client-side key inputs for spell casting.
 */
@Mod.EventBusSubscriber(modid = TestMagicMod.MODID, value = Dist.CLIENT)
public class SpellInputHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        // Skip input if player not found or GUI is opened.
        if (player == null || minecraft.screen != null) {
            return;
        }

        for (int i = 0; i < ModKeyBindings.ALL_SPELL_KEYS.length; i++) {
            if (ModKeyBindings.ALL_SPELL_KEYS[i].consumeClick()) {
                handleSpellCast(player, i);
                break;
            }
        }
    }

    private static void handleSpellCast(LocalPlayer player, int spellSlot) {
        try {
            ResourceLocation spellId = SpellConfig.CLIENT.getSpellForSlot(spellSlot);
            if (spellId == null) {
                return;
            }

            if (!canCastClientSide(player, spellId)) {
                return;
            }

            Network.sendToServer(new CastSpellRequestPacket(spellId));

            TestMagicMod.LOGGER.debug("Sent spell cast request for {} in slot {}", spellId, spellSlot);

        } catch (Exception e) {
            TestMagicMod.LOGGER.error("Error handling spell cast for slot {}", spellSlot, e);
        }
    }

    private static boolean canCastClientSide(LocalPlayer player, ResourceLocation spellId) {
        if (CooldownManager.isOnCooldownClient(spellId)) {
            return false;
        }

        var spell = ModSpells.getSpell(spellId);
        assert spell != null;

        return spell.canCast(player);
    }
}