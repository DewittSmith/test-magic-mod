package com.dewittsmith.testmagicmod.config;

import com.dewittsmith.testmagicmod.TestMagicMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration for spell mappings and settings.
 */
public class SpellConfig {

    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec SERVER_SPEC;

    public static final Client CLIENT;
    public static final Server SERVER;

    static {
        ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();
        CLIENT = new Client(clientBuilder);
        CLIENT_SPEC = clientBuilder.build();

        ForgeConfigSpec.Builder serverBuilder = new ForgeConfigSpec.Builder();
        SERVER = new Server(serverBuilder);
        SERVER_SPEC = serverBuilder.build();
    }

    public static void register(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC, "testmagicmod-client.toml");
        context.registerConfig(ModConfig.Type.SERVER, SERVER_SPEC, "testmagicmod-server.toml");
    }

    public static class Client {
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> spellSlots;
        public final ForgeConfigSpec.BooleanValue showManaBar;
        public final ForgeConfigSpec.IntValue manaBarOffsetX;
        public final ForgeConfigSpec.IntValue manaBarOffsetY;

        Client(ForgeConfigSpec.Builder builder) {
            builder.comment("Client-side spell configuration").push("spells");

            spellSlots = builder
                    .comment("Spell assignments for slots 1-4 (Z, X, C, V keys by default)")
                    .defineList("spell_slots", Arrays.asList(
                            TestMagicMod.MODID + ":spell.fireball",
                            TestMagicMod.MODID + ":spell.haste",
                            TestMagicMod.MODID + ":spell.mine",
                            TestMagicMod.MODID + ":spell.teleport"
                    ), obj -> obj instanceof String);

            builder.pop();

            builder.comment("HUD configuration").push("hud");

            showManaBar = builder
                    .comment("Show the mana bar overlay")
                    .define("show_mana_bar", true);

            manaBarOffsetX = builder
                    .comment("Horizontal offset for mana bar position (0 = default position)")
                    .defineInRange("mana_bar_offset_x", 0, -200, 200);

            manaBarOffsetY = builder
                    .comment("Vertical offset for mana bar position (negative = higher, positive = lower)")
                    .defineInRange("mana_bar_offset_y", -10, -100, 100);

            builder.pop();
        }

        public ResourceLocation getSpellForSlot(int slot) {
            if (slot < 0 || slot >= 4) {
                return null;
            }

            List<? extends String> slots = spellSlots.get();
            if (slot >= slots.size()) {
                return null;
            }

            String spellId = slots.get(slot);
            if (spellId == null || spellId.isEmpty()) {
                return null;
            }

            var spell = ResourceLocation.tryParse(spellId);
            if (spell == null) {
                TestMagicMod.LOGGER.warn("Invalid spell ID in config: {}", spellId);
            }

            return spell;
        }
    }

    public static class Server {
        public final ForgeConfigSpec.IntValue globalCooldownMultiplier;

        Server(ForgeConfigSpec.Builder builder) {
            builder.comment("Server-side spell configuration").push("spells");

            globalCooldownMultiplier = builder
                    .comment("Global multiplier for all spell cooldowns (100 = normal, 200 = double, 50 = half)")
                    .defineInRange("global_cooldown_multiplier", 100, 10, 1000);

            builder.pop();
        }

        public int getAdjustedCooldown(int baseCooldown) {
            return baseCooldown * globalCooldownMultiplier.get() / 100;
        }
    }
}