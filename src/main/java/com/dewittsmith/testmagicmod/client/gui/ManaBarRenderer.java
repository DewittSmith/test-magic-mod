package com.dewittsmith.testmagicmod.client.gui;

import com.dewittsmith.testmagicmod.TestMagicMod;
import com.dewittsmith.testmagicmod.capability.ManaCapability;
import com.dewittsmith.testmagicmod.config.SpellConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TestMagicMod.MODID, value = Dist.CLIENT)
public class ManaBarRenderer {

    private static final int MANA_BAR_WIDTH = 81;
    private static final int MANA_BAR_HEIGHT = 9;
    private static final float MANA_TEXT_SCALE = 0.75f;
    private static final float MANA_FILL_RATE = 0.1f;

    private static float displayMana = Float.NEGATIVE_INFINITY;

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() == VanillaGuiOverlay.FOOD_LEVEL.type()) {
            renderManaBar(event.getGuiGraphics());
        }
    }

    private static void renderManaBar(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null ||
                player.isCreative() ||
                player.isSpectator() ||
                !SpellConfig.CLIENT.showManaBar.get()) {
            return;
        }

        player.getCapability(ManaCapability.MANA_CAPABILITY).ifPresent(manaCapability -> {
            float currentMana = manaCapability.getCurrentMana();
            if (displayMana == Float.NEGATIVE_INFINITY) {
                displayMana = currentMana;
            }

            displayMana = (1 - MANA_FILL_RATE) * displayMana + MANA_FILL_RATE * currentMana;

            float maxMana = manaCapability.getMaxMana();

            if (maxMana <= 0) return;

            int screenWidth = minecraft.getWindow().getGuiScaledWidth();
            int screenHeight = minecraft.getWindow().getGuiScaledHeight();

            int baseX = screenWidth / 2 + 10;
            int baseY = screenHeight - 39;

            int offsetX = SpellConfig.CLIENT.manaBarOffsetX.get();
            int offsetY = SpellConfig.CLIENT.manaBarOffsetY.get();

            int x = baseX + offsetX;
            int y = baseY + offsetY;

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            // Gray background.
            guiGraphics.fill(x, y, x + MANA_BAR_WIDTH, y + MANA_BAR_HEIGHT, 0x88000000);

            float manaRatio = displayMana / maxMana;
            int filledWidth = (int) ((MANA_BAR_WIDTH - 2) * manaRatio);

            // Mana fill.
            if (filledWidth > 0) {
                int manaColor = 0xFF4A90E2;
                guiGraphics.fill(x + 1, y + 1, x + 1 + filledWidth, y + MANA_BAR_HEIGHT - 1, manaColor);
            }

            String manaText = String.format("%.0f/%.0f", displayMana, maxMana);
            
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(MANA_TEXT_SCALE, MANA_TEXT_SCALE, 1.0f);
            
            int scaledTextWidth = (int) (minecraft.font.width(manaText) * MANA_TEXT_SCALE);
            int textX = (int) ((x + (MANA_BAR_WIDTH - scaledTextWidth) / 2) / MANA_TEXT_SCALE);
            int textY = (int) ((y + 2) / MANA_TEXT_SCALE);

            // Amount text.
            guiGraphics.drawString(minecraft.font, manaText, textX, textY, 0xFFFFFF, false);
            
            guiGraphics.pose().popPose();

            RenderSystem.disableBlend();
        });
    }
}