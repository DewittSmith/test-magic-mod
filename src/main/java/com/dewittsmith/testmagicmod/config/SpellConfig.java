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
        context.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
        context.registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
    }

    public static class Client {
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> spellSlots;

        Client(ForgeConfigSpec.Builder builder) {
            builder.comment("Client-side spell configuration").push("spells");

            spellSlots = builder
                    .comment("Spell assignments for slots 1-4 (Z, X, C, V keys by default)")
                    .defineList("spell_slots", Arrays.asList(
                            TestMagicMod.MODID + ":fireball",
                            TestMagicMod.MODID + ":haste",
                            TestMagicMod.MODID + ":mine",
                            TestMagicMod.MODID + ":teleport"
                    ), obj -> obj instanceof String);

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