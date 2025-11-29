package com.dewittsmith.testmagicmod.client.gui;

import com.dewittsmith.testmagicmod.TestMagicMod;
import com.dewittsmith.testmagicmod.capability.ManaCapability;
import com.dewittsmith.testmagicmod.client.ModKeyBindings;
import com.dewittsmith.testmagicmod.config.SpellConfig;
import com.dewittsmith.testmagicmod.init.ModSpells;
import com.dewittsmith.testmagicmod.manager.CooldownManager;
import com.dewittsmith.testmagicmod.spell.ISpell;
import com.dewittsmith.testmagicmod.spell.cost.ManaCost;
import com.dewittsmith.testmagicmod.spell.cost.SpellCost;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = TestMagicMod.MODID, value = Dist.CLIENT)
public class SpellSlotsRenderer {

    private static final int SLOT_SIZE = 20;
    private static final int SLOT_SPACING = 2;
    private static final int TOTAL_WIDTH = 4 * SLOT_SIZE + 3 * SLOT_SPACING;
    private static final float COST_TEXT_SCALE = 0.6f;

    private static final Map<Integer, Long> failureFlashTimes = new HashMap<>();
    private static final long FAILURE_FLASH_DURATION = 10; // ticks (0.5 seconds)

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type()) {
            renderSpellSlots(event.getGuiGraphics());
        }
    }

    public static void triggerFailureFlash(int slot) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            failureFlashTimes.put(slot, minecraft.level.getGameTime());
        }
    }

    private static void renderSpellSlots(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null || player.isSpectator()) {
            return;
        }

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        int startX = screenWidth / 2 + 91 + 10;
        int startY = screenHeight - 22;

        player.getCapability(ManaCapability.MANA_CAPABILITY).ifPresent(manaCapability -> {
            for (int i = 0; i < 4; i++) {
                int slotX = startX + i * (SLOT_SIZE + SLOT_SPACING);
                int slotY = startY;

                renderSpellSlot(guiGraphics, minecraft, player, manaCapability, i, slotX, slotY);
            }
        });
    }

    private static void renderSpellSlot(GuiGraphics guiGraphics, Minecraft minecraft, LocalPlayer player,
                                        ManaCapability manaCapability, int slot, int x, int y) {
        ResourceLocation spellId = SpellConfig.CLIENT.getSpellForSlot(slot);
        if (spellId == null) {
            return;
        }

        ISpell spell = ModSpells.getSpell(spellId);
        if (spell == null) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Background.
        int backgroundColor = 0x80000000;

        // Check for failure flash.
        long currentTime = minecraft.level != null ? minecraft.level.getGameTime() : 0;
        Long flashTime = failureFlashTimes.get(slot);
        if (flashTime != null && (currentTime - flashTime) < FAILURE_FLASH_DURATION) {
            backgroundColor = 0x80FF0000;
        } else {
            failureFlashTimes.remove(slot);
        }

        guiGraphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, backgroundColor);

        // Border.
        int borderColor = 0xFF555555;
        guiGraphics.fill(x - 1, y - 1, x + SLOT_SIZE + 1, y, borderColor); // Top
        guiGraphics.fill(x - 1, y + SLOT_SIZE, x + SLOT_SIZE + 1, y + SLOT_SIZE + 1, borderColor); // Bottom
        guiGraphics.fill(x - 1, y - 1, x, y + SLOT_SIZE + 1, borderColor); // Left
        guiGraphics.fill(x + SLOT_SIZE, y - 1, x + SLOT_SIZE + 1, y + SLOT_SIZE + 1, borderColor); // Right

        // Cooldown overlay.
        long remainingCooldown = CooldownManager.getRemainingCooldownClient(spellId);
        if (remainingCooldown > 0) {
            renderCooldownOverlay(guiGraphics, x, y, spell, remainingCooldown);
        }

        // Spell icon (placeholder - using colored square for now).
        int iconColor = getSpellColor(spellId);
        guiGraphics.fill(x + 2, y + 2, x + SLOT_SIZE - 2, y + SLOT_SIZE - 2, iconColor);

        // Keybind indicator.
        String keybind = getKeybindForSlot(slot);
        if (keybind != null) {
            guiGraphics.drawString(minecraft.font, keybind, x + 1, y + 1, 0xFFFFFF, false);
        }

        // Mana cost
        for (SpellCost cost : spell.getCosts()) {
            if (cost instanceof ManaCost manaCost) {
                float amount = manaCost.getAmount();
                if (amount > 0) {
                    renderManaCost(guiGraphics, minecraft, x, y, amount, manaCapability.getCurrentMana() >= amount);
                }
            }
        }

        RenderSystem.disableBlend();
    }

    private static void renderCooldownOverlay(GuiGraphics guiGraphics, int x, int y, ISpell spell, long remainingTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        int totalCooldown = spell.getCooldownTicks();
        if (totalCooldown <= 0) return;

        float progress = 1.0f - ((float) remainingTicks / totalCooldown);
        progress = Mth.clamp(progress, 0.0f, 1.0f);

        // Cooldown fill overlay.
        int overlayColor = 0xA0808080;

        int fillHeight = (int) (SLOT_SIZE * progress);
        guiGraphics.fill(x, y + SLOT_SIZE - fillHeight, x + SLOT_SIZE, y + SLOT_SIZE, overlayColor);

        // Cooldown text.
        long seconds = (remainingTicks + 19) / 20; // Convert ticks to seconds, round up.
        if (seconds > 0) {
            String cooldownText = String.valueOf(seconds);
            int textWidth = minecraft.font.width(cooldownText);
            int textX = x + (SLOT_SIZE - textWidth) / 2;
            int textY = y + (SLOT_SIZE - minecraft.font.lineHeight) / 2;

            guiGraphics.drawString(minecraft.font, cooldownText, textX, textY, 0xFFFFFF, true);
        }
    }

    private static void renderManaCost(GuiGraphics guiGraphics, Minecraft minecraft, int x, int y,
                                       float manaCost, boolean canAfford) {
        String costText = String.valueOf((int) manaCost);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(COST_TEXT_SCALE, COST_TEXT_SCALE, 1.0f);

        int scaledTextWidth = (int) (minecraft.font.width(costText) * COST_TEXT_SCALE);
        int textX = (int) ((x + SLOT_SIZE - scaledTextWidth - 2) / COST_TEXT_SCALE);
        int textY = (int) ((y + SLOT_SIZE - minecraft.font.lineHeight + 2) / COST_TEXT_SCALE);

        int textColor = canAfford ? 0x55AAFF : 0xFF5555; // Blue if affordable, red if not.
        guiGraphics.drawString(minecraft.font, costText, textX, textY, textColor, true);

        guiGraphics.pose().popPose();
    }

    private static int getSpellColor(ResourceLocation spellId) {
        // Simple color coding based on spell name.
        String path = spellId.getPath();
        return switch (path) {
            case "spell.fireball" -> 0xFFFF4444;
            case "spell.haste" -> 0xFF44FF44;
            case "spell.mine" -> 0xFFFFAA44;
            case "spell.teleport" -> 0xFF4444FF;
            default -> 0xFF888888;
        };
    }

    private static String getKeybindForSlot(int slot) {
        if (slot >= 0 && slot < ModKeyBindings.ALL_SPELL_KEYS.length) {
            return ModKeyBindings.ALL_SPELL_KEYS[slot].getTranslatedKeyMessage().getString().toUpperCase();
        }
        return null;
    }
}