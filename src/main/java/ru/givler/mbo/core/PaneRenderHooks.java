package ru.givler.mbo.core;

import net.minecraft.block.BlockPane;
import net.minecraft.client.renderer.RenderBlocks;

/** Client-side rendering for an unconnected pane's central post. */
public final class PaneRenderHooks {
    private PaneRenderHooks() { }

    public static boolean renderIsolated(RenderBlocks renderer, BlockPane pane, int x, int y, int z) {
        if (!PaneConnectionHooks.isIsolated(pane, renderer.blockAccess, x, y, z)) return false;
        renderer.setRenderBounds(7.0D / 16.0D, 0.0D, 7.0D / 16.0D,
                9.0D / 16.0D, 1.0D, 9.0D / 16.0D);
        return renderer.renderStandardBlock(pane, x, y, z);
    }
}
