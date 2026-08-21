package ru.givler.mbo.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import ru.givler.mbo.config.TooltipFrameConfig;

import java.util.List;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Vanilla tooltip layout with an unscaled, tiled ornamental frame. */
public final class TooltipFrameHooks {
    private static final int TEX_W = 64;
    private static final int TEX_H = 16;
    private static final ThreadLocal<Integer> ITEM_TOOLTIP_DEPTH = new ThreadLocal<Integer>() {
        @Override protected Integer initialValue() { return 0; }
    };
    private static final Set<List> ITEM_TOOLTIP_LISTS = Collections.newSetFromMap(
            new IdentityHashMap<List, Boolean>());
    private static Field guiLeftField;
    private static Field guiTopField;
    private static boolean containerFieldsResolved;
    private static Method neiStackMouseOver;
    private static boolean neiMethodResolved;

    private TooltipFrameHooks() {}

    public static void beginItemTooltip() {
        ITEM_TOOLTIP_DEPTH.set(ITEM_TOOLTIP_DEPTH.get() + 1);
    }

    public static void endItemTooltip() {
        ITEM_TOOLTIP_DEPTH.set(Math.max(0, ITEM_TOOLTIP_DEPTH.get() - 1));
    }

    /** ItemTooltipEvent supplies the exact List later passed to drawHoveringText. */
    public static void markItemTooltip(List lines) {
        if (lines != null) ITEM_TOOLTIP_LISTS.add(lines);
    }

    public static void drawTooltip(GuiScreen screen, List lines, int mouseX, int mouseY,
                                   FontRenderer font) {
        if (lines == null || lines.isEmpty()) return;

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        int textWidth = 0;
        for (Object line : lines) textWidth = Math.max(textWidth, font.getStringWidth(String.valueOf(line)));

        int x = mouseX + 12;
        int y = mouseY - 12;
        int textHeight = 8;
        if (lines.size() > 1) textHeight += 2 + (lines.size() - 1) * 10;
        if (x + textWidth > screen.width) x -= 28 + textWidth;
        if (y + textHeight + 6 > screen.height) y = screen.height - textHeight - 6;

        int left = x - 4;
        int top = y - 4;
        int right = x + textWidth + 4;
        int bottom = y + textHeight + 4;

        ItemStack hoveredStack = findMouseOverItem(screen, mouseX, mouseY);
        boolean itemTooltip = ITEM_TOOLTIP_DEPTH.get() > 0 || ITEM_TOOLTIP_LISTS.remove(lines)
                || hoveredStack != null;
        Gui.drawRect(left, top, right, bottom, TooltipFrameConfig.getBackground(hoveredStack));
        if (itemTooltip) drawFrame(left - 4, top - 4, right + 4, bottom + 4,
                TooltipFrameConfig.getFrame(hoveredStack));
        else drawVanillaFrame(left, top, right, bottom);

        int lineY = y;
        for (int i = 0; i < lines.size(); i++) {
            font.drawStringWithShadow(String.valueOf(lines.get(i)), x, lineY, -1);
            if (i == 0) lineY += 2;
            lineY += 10;
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();
    }

    private static ItemStack findMouseOverItem(GuiScreen screen, int mouseX, int mouseY) {
        if (!(screen instanceof GuiContainer)) return null;
        resolveContainerFields();
        if (guiLeftField == null || guiTopField == null) return null;
        try {
            GuiContainer container = (GuiContainer) screen;
            int guiLeft = guiLeftField.getInt(container);
            int guiTop = guiTopField.getInt(container);
            for (Object object : container.inventorySlots.inventorySlots) {
                if (!(object instanceof Slot)) continue;
                Slot slot = (Slot) object;
                int sx = guiLeft + slot.xDisplayPosition;
                int sy = guiTop + slot.yDisplayPosition;
                if (mouseX >= sx - 1 && mouseX < sx + 17
                        && mouseY >= sy - 1 && mouseY < sy + 17
                        && slot.getHasStack()) return slot.getStack();
            }
        } catch (IllegalAccessException ignored) {
        }
        return null;
    }

    /** Replacement for CodeChickenLib's GuiDraw.drawTooltipBox. */
    public static void drawNeiTooltipBox(int x, int y, int width, int height) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        ItemStack hoveredStack = findNeiItemUnderMouse(screen);
        boolean itemTooltip = hoveredStack != null;
        int right = x + width;
        int bottom = y + height;
        Gui.drawRect(x, y, right, bottom, TooltipFrameConfig.getBackground(hoveredStack));
        // ramka1 contains transparent padding around its visible pixels. Draw
        // the assembled frame outside the NEI background so the black fill no
        // longer protrudes beyond the visible gold edge.
        if (itemTooltip) drawFrame(x - 4, y - 4, right + 4, bottom + 4,
                TooltipFrameConfig.getFrame(hoveredStack));
        else drawVanillaFrame(x, y, right, bottom);
    }

    private static ItemStack findNeiItemUnderMouse(GuiScreen screen) {
        if (!(screen instanceof GuiContainer)) return null;
        if (!neiMethodResolved) {
            neiMethodResolved = true;
            try {
                Class<?> manager = Class.forName("codechicken.nei.guihook.GuiContainerManager");
                neiStackMouseOver = manager.getMethod("getStackMouseOver", GuiContainer.class);
            } catch (Exception ignored) {
            }
        }
        if (neiStackMouseOver == null) return null;
        try {
            Object result = neiStackMouseOver.invoke(null, screen);
            return result instanceof ItemStack ? (ItemStack) result : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static void resolveContainerFields() {
        if (containerFieldsResolved) return;
        containerFieldsResolved = true;
        guiLeftField = findIntField("guiLeft", "field_147003_i");
        guiTopField = findIntField("guiTop", "field_147009_r");
    }

    private static Field findIntField(String... names) {
        for (String name : names) {
            try {
                Field field = GuiContainer.class.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    private static void drawVanillaFrame(int left, int top, int right, int bottom) {
        int border = 0x505000FF;
        int borderDark = (border & 0xFEFEFE) >> 1 | border & 0xFF000000;
        Gui.drawRect(left, top, right, top + 1, border);
        Gui.drawRect(left, bottom - 1, right, bottom, borderDark);
        Gui.drawRect(left, top + 1, left + 1, bottom - 1, border);
        Gui.drawRect(right - 1, top + 1, right, bottom - 1, borderDark);
    }

    private static void drawFrame(int left, int top, int right, int bottom, ResourceLocation frame) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(frame);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1F, 1F, 1F, 1F);

        // The source is a sprite sheet: four corners and two ornaments of
        // different widths. Keep both ornaments centred independently.
        sprite(left, top, 0, 0, 8, 8);
        sprite(right - 8, top, 56, 0, 8, 8);
        sprite(left, bottom - 8, 0, 8, 8, 8);
        sprite(right - 8, bottom - 8, 56, 8, 8, 8);

        final int topOrnamentWidth = 40;
        final int bottomOrnamentWidth = 26;
        int topCenterX = (left + right - topOrnamentWidth) / 2;
        int bottomCenterX = (left + right - bottomOrnamentWidth) / 2;
        // Draw continuous lines first. The ornaments contain transparent edge
        // pixels, so they are overlaid afterwards without leaving line gaps.
        for (int px = left + 4; px < right - 4; px++) {
            sprite(px, top + 3, 3, 8, 1, 1);
            sprite(px, bottom - 4, 3, 8, 1, 1);
        }
        for (int py = top + 4; py < bottom - 4; py++) {
            sprite(left + 3, py, 3, 8, 1, 1);
            sprite(right - 4, py, 3, 8, 1, 1);
        }

        // Move the special centre pieces together with their corresponding line.
        sprite(topCenterX, top - 3, 12, 0, topOrnamentWidth, 8);
        sprite(bottomCenterX, bottom - 5, 19, 8, bottomOrnamentWidth, 8);
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    private static void sprite(int x, int y, int u, int v, int width, int height) {
        float u0 = u / (float) TEX_W;
        float v0 = v / (float) TEX_H;
        float u1 = (u + width) / (float) TEX_W;
        float v1 = (v + height) / (float) TEX_H;
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.addVertexWithUV(x, y + height, 400D, u0, v1);
        t.addVertexWithUV(x + width, y + height, 400D, u1, v1);
        t.addVertexWithUV(x + width, y, 400D, u1, v0);
        t.addVertexWithUV(x, y, 400D, u0, v0);
        t.draw();
    }
}
