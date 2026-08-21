package ru.givler.mbo.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

/** Routes Thaumcraft's private book font through Minecraft's active renderer. */
public final class ThaumcraftFontHooks {
    private ThaumcraftFontHooks() {}

    public static int drawString(String text, int x, int y, int color, boolean shadow) {
        FontRenderer renderer = Minecraft.getMinecraft().fontRenderer;
        return renderer.drawString(text, x, y, color, shadow);
    }

    public static int getStringWidth(String text) {
        return Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
    }

    public static int getCharWidth(char character) {
        return Minecraft.getMinecraft().fontRenderer.getStringWidth(String.valueOf(character));
    }
}
