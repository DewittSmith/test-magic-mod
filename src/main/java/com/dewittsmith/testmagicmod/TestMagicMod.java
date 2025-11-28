package com.dewittsmith.testmagicmod;

import com.dewittsmith.testmagicmod.config.SpellConfig;
import com.dewittsmith.testmagicmod.init.ModSpells;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.zeith.hammerlib.core.adapter.LanguageAdapter;

@Mod(TestMagicMod.MODID)
public class TestMagicMod {
    public static final String MODID = "testmagicmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TestMagicMod(FMLJavaModLoadingContext context) {
        LanguageAdapter.registerMod(MODID);

        IEventBus modEventBus = context.getModEventBus();

        SpellConfig.register(context);
        ModSpells.register(modEventBus);
        modEventBus.addListener(this::commonSetup);

        LOGGER.info("TestMagicMod initialized with spell system, networking, and configuration");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // TODO
    }
}
