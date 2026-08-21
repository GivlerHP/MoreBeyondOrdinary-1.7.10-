package ru.givler.mbo.client.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;

/** Installs the 1.21-style JSON font loader as Minecraft's global font renderer. */
public final class ModernFontSupport {
    private ModernFontSupport() {}

    public static void install() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.fontRenderer instanceof ModernFontRenderer) return;
        boolean unicode = mc.fontRenderer.getUnicodeFlag();
        ModernFontRenderer renderer = new ModernFontRenderer(mc.gameSettings, mc.renderEngine);
        // If no modern font JSON is supplied, all calls fall back to the
        // superclass. Preserve the current vanilla Unicode mode as well.
        renderer.setUnicodeFlag(unicode);
        mc.fontRenderer = renderer;
        if (mc.getResourceManager() instanceof IReloadableResourceManager) {
            ((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(renderer);
        }
    }
}
