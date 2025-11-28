package com.dewittsmith.testmagicmod.client;

import com.dewittsmith.testmagicmod.TestMagicMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/**
 * Client-side key bindings for spell casting.
 */
public class ModKeyBindings {

    public static final String SPELL_CATEGORY = "key.categories." + TestMagicMod.MODID + ".spells";

    public static final KeyMapping CAST_SPELL_1 = new KeyMapping(
            "key." + TestMagicMod.MODID + ".cast_spell_1",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            SPELL_CATEGORY
    );

    public static final KeyMapping CAST_SPELL_2 = new KeyMapping(
            "key." + TestMagicMod.MODID + ".cast_spell_2",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            SPELL_CATEGORY
    );

    public static final KeyMapping CAST_SPELL_3 = new KeyMapping(
            "key." + TestMagicMod.MODID + ".cast_spell_3",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            SPELL_CATEGORY
    );

    public static final KeyMapping CAST_SPELL_4 = new KeyMapping(
            "key." + TestMagicMod.MODID + ".cast_spell_4",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            SPELL_CATEGORY
    );

    public static final KeyMapping[] ALL_SPELL_KEYS = {
            CAST_SPELL_1, CAST_SPELL_2, CAST_SPELL_3, CAST_SPELL_4
    };
}