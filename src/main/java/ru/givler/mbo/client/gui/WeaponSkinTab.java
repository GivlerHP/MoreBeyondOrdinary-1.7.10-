package ru.givler.mbo.client.gui;

import java.nio.IntBuffer;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.client.render.weaponskin.WeaponSkinRenderer;
import ru.givler.mbo.item.skin.WeaponSkinData;
import ru.givler.mbo.item.skin.WeaponSkinDefinition;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketWeaponSkin;
import ru.givler.mbo.registry.WeaponSkinRegistry;

final class WeaponSkinTab {
    private static final int CELL = 23, GRID_TOP = 51, ROWS = 4;
    private final List<WeaponSkinDefinition> catalog = WeaponSkinRegistry.ALL;
    private final IntBuffer viewport = BufferUtils.createIntBuffer(16);
    private final IntBuffer clip = BufferUtils.createIntBuffer(16);
    private FontRenderer font;
    private ItemStack item, previewStack;
    private EntityOtherPlayerMP mannequin;
    private GuiButton apply, remove;
    private int left, top, previewX, width, columns, selected, scroll;
    private boolean draggingScrollbar;

    void init(FontRenderer font, int left, int top, int previewX, int width) {
        this.font = font;
        this.left = left;
        this.top = top;
        this.previewX = previewX;
        this.width = width;
        int listWidth = previewX - 24;
        columns = Math.max(1, (listWidth - 12) / CELL);
        scroll = Math.min(scroll, maxScroll());
        apply = new GuiButton(3, left + 12, top + 174, (listWidth - 4) / 2, 20, I18n.format("mbo.weapon_skin.apply"));
        remove = new GuiButton(4, left + 16 + (listWidth - 4) / 2, top + 174,
                (listWidth - 4) / 2, 20, I18n.format("mbo.weapon_skin.remove"));
    }

    void load(ItemStack stack) {
        item = stack;
        String installed = WeaponSkinData.id(stack);
        for (int i = 0; i < catalog.size(); i++) {
            if (catalog.get(i).id.equals(installed)) {
                selected = i;
                int row = i / columns;
                if (row < scroll) scroll = row;
                else if (row >= scroll + ROWS) scroll = row - ROWS + 1;
                break;
            }
        }
        refreshPreview();
    }

    private void refreshPreview() {
        previewStack = null;
        if (catalog.isEmpty()) return;
        WeaponSkinDefinition skin = catalog.get(selected);
        if (compatible()) {
            previewStack = item.copy();
        } else {
            for (Object entry : net.minecraft.item.Item.itemRegistry) {
                net.minecraft.item.Item sample = (net.minecraft.item.Item) entry;
                if (skin.supports(sample)) {
                    previewStack = new ItemStack(sample);
                    break;
                }
            }
        }
        if (previewStack != null) WeaponSkinData.set(previewStack, skin.id);
    }

    private boolean compatible() {
        return item != null && !catalog.isEmpty() && catalog.get(selected).supports(item.getItem());
    }

    private void updateButtons() {
        apply.enabled = compatible() && !catalog.get(selected).id.equals(WeaponSkinData.id(item));
        remove.enabled = !WeaponSkinData.id(item).isEmpty();
    }

    private void drawCatalog(int mouseX, int mouseY) {
        int x = left + 12, y = top + GRID_TOP;
        int right = left + previewX - 12;
        DescriptionWorkbenchSkin.panel(x, y - 2, right - x, CELL * ROWS + 4);
        int hovered = cellAt(mouseX, mouseY);
        for (int cell = 0; cell < columns * ROWS; cell++) {
            int index = scroll * columns + cell;
            int cellX = x + 3 + cell % columns * CELL;
            int cellY = y + 2 + cell / columns * CELL;
            DescriptionWorkbenchSkin.inputSlot(cellX + 2, cellY + 2);
            if (index >= catalog.size()) continue;
            if (index == selected || index == hovered) {
                Gui.drawRect(cellX + 2, cellY + 2, cellX + 20, cellY + 20,
                        index == selected ? 0xa07e623e : 0x6049372a);
            }
            WeaponSkinDefinition skin = catalog.get(index);
            WeaponSkinRenderer.INSTANCE.preview(skin, skin.inventory, cellX + 11, cellY + 16, 9);
        }
        if (maxScroll() > 0) {
            int height = CELL * ROWS;
            int thumb = thumbHeight();
            int offset = scroll * (height - thumb) / maxScroll();
            Gui.drawRect(right - 8, y, right - 3, y + height, 0xff221913);
            Gui.drawRect(right - 8, y + offset, right - 3, y + offset + thumb, 0xff927b59);
        }
    }

    private int cellAt(int x, int y) {
        int colX = x - left - 15, rowY = y - top - GRID_TOP - 2;
        if (colX < 0 || rowY < 0 || colX >= columns * CELL || rowY >= ROWS * CELL
                || colX % CELL < 2 || colX % CELL >= 20 || rowY % CELL < 2 || rowY % CELL >= 20) return -1;
        int index = (scroll + rowY / CELL) * columns + colX / CELL;
        return index < catalog.size() ? index : -1;
    }

    String hoveredName(int x, int y) {
        int index = cellAt(x, y);
        return index < 0 ? null : I18n.format(catalog.get(index).titleKey());
    }

    private int maxScroll() {
        return Math.max(0, (catalog.size() + columns - 1) / columns - ROWS);
    }

    private int thumbHeight() {
        return Math.max(10, CELL * ROWS * ROWS / (maxScroll() + ROWS));
    }

    boolean scroll(int x, int y, int wheel) {
        if (wheel == 0 || x < left + 12 || x >= left + previewX - 12
                || y < top + GRID_TOP - 2 || y >= top + GRID_TOP + CELL * ROWS + 2) return false;
        scroll = Math.max(0, Math.min(maxScroll(), scroll - Integer.signum(wheel)));
        return true;
    }

    boolean dragScrollbar(int y) {
        if (!draggingScrollbar) return false;
        int track = CELL * ROWS - thumbHeight();
        int position = Math.max(0, Math.min(track, y - top - GRID_TOP - thumbHeight() / 2));
        scroll = track == 0 ? 0 : Math.round(position * maxScroll() / (float) track);
        return true;
    }

    void releaseScrollbar() {
        draggingScrollbar = false;
    }

    void draw(int mouseX, int mouseY, int screenWidth, int screenHeight) {
        updateButtons();
        Minecraft mc = Minecraft.getMinecraft();
        font.drawString(I18n.format("mbo.weapon_skin.catalog"), left + 12, top + 34, 0xdac99f);
        drawCatalog(mouseX, mouseY);
        if (!compatible()) {
            String hint = item == null ? "mbo.weapon_skin.insert" : "mbo.weapon_skin.incompatible";
            font.drawString(font.trimStringToWidth(I18n.format(hint), previewX - 24),
                    left + 12, top + 158, 0xdac99f);
        }
        if (previewStack != null && mc.thePlayer != null) {
            drawPreviews(mc, screenWidth, screenHeight);
        }
        apply.drawButton(mc, mouseX, mouseY);
        remove.drawButton(mc, mouseX, mouseY);
    }

    private void drawPreviews(Minecraft mc, int screenWidth, int screenHeight) {
        int x = left + previewX + 7, y = top + 66;
        int pane = (width - previewX - 26) / 2;
        int height = 104;
        int thirdX = x + pane + 4;
        DescriptionWorkbenchSkin.panel(x, y, pane, height);
        DescriptionWorkbenchSkin.panel(thirdX, y, pane, height);
        drawFirstPerson(mc, x + 3, y + 3, pane - 6, height - 6, screenWidth, screenHeight);
        if (mannequin == null || mannequin.worldObj != mc.theWorld) {
            mannequin = new EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.getGameProfile());
        }
        // Preview inventory belongs to the mannequin, not the active player.
        mannequin.setCurrentItemOrArmor(0, previewStack);
        int view = Math.max(12, Math.min(38, (pane - 10) * 2 / 3));
        GuiInventory.func_147046_a(thirdX + pane / 2 + 5, y + height - 10, view, -24, -6, mannequin);
        drawCaption("mbo.weapon_skin.view.0", x, y + height + 9, pane);
        drawCaption("mbo.weapon_skin.view.1", thirdX, y + height + 9, pane);
    }

    private void drawCaption(String key, int x, int y, int width) {
        String text = font.trimStringToWidth(I18n.format(key), width);
        font.drawString(text, x + (width - font.getStringWidth(text)) / 2, y, 0xdac99f);
    }

    private void drawFirstPerson(Minecraft mc, int x, int y, int w, int h, int screenWidth, int screenHeight) {
        int px = x * mc.displayWidth / screenWidth;
        int py = mc.displayHeight - (y + h) * mc.displayHeight / screenHeight;
        int pw = w * mc.displayWidth / screenWidth;
        int ph = h * mc.displayHeight / screenHeight;
        if (pw < 1 || ph < 1) return;
        viewport.clear();
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);
        clip.clear();
        GL11.glGetInteger(GL11.GL_SCISSOR_BOX, clip);
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_CURRENT_BIT);
        GL11.glViewport(px, py, pw, ph);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(px, py, pw, ph);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        // Keep the horizontal field of view for a wide preview, including the right-hand offset.
        double halfHeight = 0.075D;
        double halfWidth = halfHeight * Math.max(1.35D, pw / (double) ph);
        // Aim the preview camera toward the hand so the whole grip fits the pane.
        double centerX = 0.027D, centerY = -0.017D;
        GL11.glFrustum(centerX - halfWidth, centerX + halfWidth, centerY - halfHeight,
                centerY + halfHeight, 0.05D, 16D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        try {
            // Vanilla resting hand transform, followed by the actual equipped item render path.
            GL11.glTranslatef(0.56F, -0.52F, -0.72F);
            GL11.glRotatef(45, 0, 1, 0);
            GL11.glScalef(0.4F, 0.4F, 0.4F);
            GL11.glDisable(GL11.GL_LIGHTING);
            mc.entityRenderer.itemRenderer.renderItem(mc.thePlayer, previewStack, 0, ItemRenderType.EQUIPPED_FIRST_PERSON);
        } finally {
            GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glViewport(viewport.get(0), viewport.get(1), viewport.get(2), viewport.get(3));
            GL11.glScissor(clip.get(0), clip.get(1), clip.get(2), clip.get(3));
            GL11.glPopAttrib();
        }
    }

    boolean click(int x, int y, int mouseButton, int window) {
        if (mouseButton != 0) return false;
        updateButtons();
        Minecraft mc = Minecraft.getMinecraft();
        if (x >= left + 12 && x < left + previewX - 12 && y >= top + GRID_TOP
                && y < top + GRID_TOP + CELL * ROWS) {
            if (x >= left + previewX - 22) {
                draggingScrollbar = maxScroll() > 0;
                dragScrollbar(y);
            } else {
                int index = cellAt(x, y);
                if (index >= 0) {
                    selected = index;
                    refreshPreview();
                }
            }
        } else if (apply.mousePressed(mc, x, y)) send(window, catalog.get(selected).id);
        else if (remove.mousePressed(mc, x, y)) send(window, "");
        else return false;
        return true;
    }

    private void send(int window, String id) {
        PacketManager.INSTANCE.sendToServer(new PacketWeaponSkin(window, item, id));
    }
}
