package com.dewittsmith.testmagicmod.manager;

import com.dewittsmith.testmagicmod.TestMagicMod;
import com.dewittsmith.testmagicmod.capability.ManaCapability;
import com.dewittsmith.testmagicmod.init.ModSpells;
import com.dewittsmith.testmagicmod.network.CooldownSyncPacket;
import com.dewittsmith.testmagicmod.network.ManaSyncPacket;
import com.dewittsmith.testmagicmod.network.SpellCastResultPacket;
import com.dewittsmith.testmagicmod.spell.ISpell;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.zeith.hammerlib.net.Network;

/**
 * Manages spell casting requests and validation on the server side.
 */
public class SpellCastManager {

    public static void handleCastRequest(ServerPlayer player, ResourceLocation spellId) {
        try {
            ISpell spell = ModSpells.getSpell(spellId);
            if (spell == null) {
                sendCastResult(player, spellId, SpellCastResultPacket.Result.UNKNOWN_ERROR, "Unknown spell: " + spellId);
                return;
            }

            if (!spell.isEnabled()) {
                sendCastResult(player, spellId, SpellCastResultPacket.Result.SPELL_DISABLED, "Spell is disabled");
                return;
            }

            if (CooldownManager.isOnCooldown(player, spellId)) {
                long remainingTicks = CooldownManager.getRemainingCooldown(player, spellId);
                sendCastResult(player, spellId, SpellCastResultPacket.Result.ON_COOLDOWN,
                        "Spell on cooldown for " + (remainingTicks / 20f) + " seconds");
                return;
            }

            if (!spell.canCast(player)) {
                SpellCastResultPacket.Result failureReason = determineFailureReason(player, spell);
                sendCastResult(player, spellId, failureReason, getFailureMessage(failureReason));
                return;
            }

            boolean success = spell.cast(player);

            if (success) {
                CooldownManager.setCooldown(player, spellId, spell.getCooldownTicks());

                sendCastResult(player, spellId, SpellCastResultPacket.Result.SUCCESS, "");

                syncPlayerData(player);

                TestMagicMod.LOGGER.debug("Player {} successfully cast spell {}", player.getName().getString(), spellId);
            } else {
                sendCastResult(player, spellId, SpellCastResultPacket.Result.UNKNOWN_ERROR, "Spell execution failed");
            }

        } catch (Exception e) {
            TestMagicMod.LOGGER.error("Error handling spell cast request from {}", player.getName().getString(), e);
            sendCastResult(player, spellId, SpellCastResultPacket.Result.UNKNOWN_ERROR, "Internal error");
        }
    }

    private static SpellCastResultPacket.Result determineFailureReason(ServerPlayer player, ISpell spell) {
        if (player.getAbilities().instabuild) {
            return SpellCastResultPacket.Result.INVALID_TARGET;
        }

        float currentMana = ManaCapability.getCurrentMana(player);
        float requiredMana = spell.getCosts().stream()
                .filter(cost -> cost instanceof com.dewittsmith.testmagicmod.spell.cost.ManaCost)
                .map(cost -> ((com.dewittsmith.testmagicmod.spell.cost.ManaCost) cost).getAmount())
                .reduce(0f, Float::sum);

        if (currentMana < requiredMana) {
            return SpellCastResultPacket.Result.INSUFFICIENT_MANA;
        }

        for (var cost : spell.getCosts()) {
            if (!cost.canAfford(player)) {
                if (cost instanceof com.dewittsmith.testmagicmod.spell.cost.ItemCost) {
                    return SpellCastResultPacket.Result.INSUFFICIENT_ITEMS;
                } else if (cost instanceof com.dewittsmith.testmagicmod.spell.cost.HealthCost) {
                    return SpellCastResultPacket.Result.INSUFFICIENT_HEALTH;
                }
            }
        }

        return SpellCastResultPacket.Result.INVALID_TARGET;
    }

    private static String getFailureMessage(SpellCastResultPacket.Result result) {
        return switch (result) {
            case INSUFFICIENT_MANA -> "Not enough mana";
            case INSUFFICIENT_ITEMS -> "Missing required items";
            case INSUFFICIENT_HEALTH -> "Not enough health";
            case INVALID_TARGET -> "Invalid target or conditions";
            default -> "Cannot cast spell";
        };
    }

    private static void sendCastResult(ServerPlayer player, ResourceLocation spellId,
                                       SpellCastResultPacket.Result result, String message) {
        Network.sendTo(new SpellCastResultPacket(spellId, result, message), player);
    }

    public static void syncPlayerData(ServerPlayer player) {
        player.getCapability(ManaCapability.MANA_CAPABILITY).ifPresent(manaCapability -> {
            ManaSyncPacket manaPacket = new ManaSyncPacket(
                    manaCapability.getCurrentMana(),
                    manaCapability.getMaxMana(),
                    manaCapability.getManaRegen()
            );
            Network.sendTo(manaPacket, player);
        });

        CooldownSyncPacket cooldownPacket = new CooldownSyncPacket(CooldownManager.getAllCooldowns(player));
        Network.sendTo(cooldownPacket, player);
    }
}