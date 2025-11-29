package com.dewittsmith.testmagicmod.init;

import com.dewittsmith.testmagicmod.TestMagicMod;
import com.dewittsmith.testmagicmod.spell.ISpell;
import com.dewittsmith.testmagicmod.spell.impl.FireballSpell;
import com.dewittsmith.testmagicmod.spell.impl.HasteSpell;
import com.dewittsmith.testmagicmod.spell.impl.MineSpell;
import com.dewittsmith.testmagicmod.spell.impl.TeleportSpell;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.*;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModSpells {

    public static final ResourceLocation SPELL_REGISTRY_NAME = ResourceLocation.fromNamespaceAndPath(TestMagicMod.MODID, "spells");

    public static final DeferredRegister<ISpell> SPELLS = DeferredRegister.create(SPELL_REGISTRY_NAME, TestMagicMod.MODID);

    public static final RegistryObject<ISpell> FIREBALL = SPELLS.register("spell.fireball", FireballSpell::new);
    public static final RegistryObject<ISpell> HASTE = SPELLS.register("spell.haste", HasteSpell::new);
    public static final RegistryObject<ISpell> MINE = SPELLS.register("spell.mine", MineSpell::new);
    public static final RegistryObject<ISpell> TELEPORT = SPELLS.register("spell.teleport", TeleportSpell::new);

    public static void register(IEventBus eventBus) {
        SPELLS.register(eventBus);
    }

    @SubscribeEvent
    public static void onNewRegistry(NewRegistryEvent event) {
        event.create(new RegistryBuilder<ISpell>().setName(SPELL_REGISTRY_NAME));
    }

    @Nullable
    public static ISpell getSpell(String name) {
        return getSpell(ResourceLocation.fromNamespaceAndPath(TestMagicMod.MODID, name));
    }

    @SuppressWarnings("UnstableApiUsage")
    @Nullable
    public static ISpell getSpell(ResourceLocation id) {
        ForgeRegistry<ISpell> registry = RegistryManager.ACTIVE.getRegistry(SPELL_REGISTRY_NAME);
        return registry.getValue(id);
    }
}