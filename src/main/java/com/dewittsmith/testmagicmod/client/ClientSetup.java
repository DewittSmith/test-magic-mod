package com.dewittsmith.testmagicmod.client;

import com.dewittsmith.testmagicmod.TestMagicMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side initialization and setup.
 */
@Mod.EventBusSubscriber(modid = TestMagicMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        for (var keyMapping : ModKeyBindings.ALL_SPELL_KEYS) {
            event.register(keyMapping);
        }

        TestMagicMod.LOGGER.info("Registered {} spell key bindings", ModKeyBindings.ALL_SPELL_KEYS.length);
    }
}