package com.dewittsmith.testmagicmod.manager;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages spell cooldowns for players.
 */
public class CooldownManager {

    private static final Map<UUID, Map<ResourceLocation, Long>> serverCooldowns = new ConcurrentHashMap<>();

    private static final Map<ResourceLocation, Long> clientCooldowns = new HashMap<>();

    public static void setCooldown(Player player, ResourceLocation spellId, int cooldownTicks) {
        UUID playerId = player.getUUID();
        long endTime = getCurrentTick() + cooldownTicks;

        serverCooldowns.computeIfAbsent(playerId, k -> new HashMap<>()).put(spellId, endTime);
    }

    public static boolean isOnCooldown(Player player, ResourceLocation spellId) {
        UUID playerId = player.getUUID();
        Map<ResourceLocation, Long> playerCooldowns = serverCooldowns.get(playerId);

        if (playerCooldowns == null) {
            return false;
        }

        Long endTime = playerCooldowns.get(spellId);
        if (endTime == null) {
            return false;
        }

        long currentTime = getCurrentTick();
        if (currentTime >= endTime) {
            playerCooldowns.remove(spellId);
            return false;
        }

        return true;
    }

    public static long getRemainingCooldown(Player player, ResourceLocation spellId) {
        UUID playerId = player.getUUID();
        Map<ResourceLocation, Long> playerCooldowns = serverCooldowns.get(playerId);

        if (playerCooldowns == null) {
            return 0;
        }

        Long endTime = playerCooldowns.get(spellId);
        if (endTime == null) {
            return 0;
        }

        long currentTime = getCurrentTick();
        return Math.max(0, endTime - currentTime);
    }

    public static Map<ResourceLocation, Long> getAllCooldowns(Player player) {
        UUID playerId = player.getUUID();
        Map<ResourceLocation, Long> playerCooldowns = serverCooldowns.get(playerId);

        if (playerCooldowns == null) {
            return new HashMap<>();
        }

        long currentTime = getCurrentTick();
        Map<ResourceLocation, Long> activeCooldowns = new HashMap<>();

        playerCooldowns.entrySet().removeIf(entry -> {
            if (entry.getValue() <= currentTime) {
                return true;
            } else {
                activeCooldowns.put(entry.getKey(), entry.getValue());
                return false;
            }
        });

        return activeCooldowns;
    }

    public static void clearCooldowns(Player player) {
        serverCooldowns.remove(player.getUUID());
    }

    public static void updateClientCooldowns(Player player, Map<ResourceLocation, Long> cooldowns) {
        clientCooldowns.clear();
        clientCooldowns.putAll(cooldowns);
    }

    public static boolean isOnCooldownClient(ResourceLocation spellId) {
        Long endTime = clientCooldowns.get(spellId);
        if (endTime == null) {
            return false;
        }

        long currentTime = getCurrentTick();
        if (currentTime >= endTime) {
            clientCooldowns.remove(spellId);
            return false;
        }

        return true;
    }

    public static long getRemainingCooldownClient(ResourceLocation spellId) {
        Long endTime = clientCooldowns.get(spellId);
        if (endTime == null) {
            return 0;
        }

        long currentTime = getCurrentTick();
        return Math.max(0, endTime - currentTime);
    }

    private static long getCurrentTick() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server.getTickCount();
    }
}