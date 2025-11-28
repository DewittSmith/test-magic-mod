package com.dewittsmith.testmagicmod.event;

import com.dewittsmith.testmagicmod.TestMagicMod;
import com.dewittsmith.testmagicmod.capability.ManaCapability;
import com.dewittsmith.testmagicmod.capability.ManaCapabilityProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Event handlers for mana capability management.
 */
@Mod.EventBusSubscriber(modid = TestMagicMod.MODID)
public class ManaCapabilityEvents {

    private static final ResourceLocation MANA_CAPABILITY_ID = ResourceLocation.parse(TestMagicMod.MODID + ".mana");

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(ManaCapability.class);
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(MANA_CAPABILITY_ID, new ManaCapabilityProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            long currentTick = event.player.level().getGameTime();
            ManaCapability.tickMana(event.player, currentTick);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().getCapability(ManaCapability.MANA_CAPABILITY).ifPresent(oldMana -> {
            event.getEntity().getCapability(ManaCapability.MANA_CAPABILITY).ifPresent(newMana -> {
                newMana.deserializeNBT(oldMana.serializeNBT());
            });
        });
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            com.dewittsmith.testmagicmod.manager.SpellCastManager.syncPlayerData(serverPlayer);
        }
    }
}