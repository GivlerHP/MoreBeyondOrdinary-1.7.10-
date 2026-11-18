package ru.givler.mbo.client.gui;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.client.render.weaponskin.WeaponSkinCapture;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
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
    private FontRenderer font;
    private ItemStack item, previewStack;
    private EntityOtherPlayerMP mannequin;
    private GuiButton apply, remove, capture;
    private DynamicTexture snapshot;
    private ResourceLocation snapshotLocation;
    private boolean waitingForCapture, captureFailed, hasSnapshot;
    private int left, top, previewX, width, columns, selected, scroll;
    private boolean draggingScrollbar, rotatingPreview;
    private int lastDragX, lastDragY;
    private float previewYaw = 25F, previewPitch = -8F;

    void init(FontRenderer font, int left, int top, int previewX, int width) {
        this.font = font;
        this.left = left;
        this.top = top;
        this.previewX = previewX;
        this.width = width;
        int listWidth = previewX - 24;
        capture = new GuiButton(5, left + previewX + 7, top + 174, paneWidth(), 20, "");
        columns = Math.max(1, (listWidth - 21) / CELL);
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
        hasSnapshot = false;
        captureFailed = false;
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
        capture.enabled = previewStack != null && !waitingForCapture;
        capture.displayString = font.trimStringToWidth(I18n.format("mbo.weapon_skin.capture"), paneWidth() - 6);
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
        int trackX = right - 16;
        DescriptionWorkbenchSkin.scrollTrack(trackX, y, CELL * ROWS);
        int offset = maxScroll() == 0 ? 0 : scroll * (CELL * ROWS - 15) / maxScroll();
        DescriptionWorkbenchSkin.scrollThumb(trackX + 1, y + offset, draggingScrollbar);
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

    boolean scroll(int x, int y, int wheel) {
        if (wheel == 0 || x < left + 12 || x >= left + previewX - 12
                || y < top + GRID_TOP - 2 || y >= top + GRID_TOP + CELL * ROWS + 2) return false;
        scroll = Math.max(0, Math.min(maxScroll(), scroll - Integer.signum(wheel)));
        return true;
    }

    boolean drag(int x, int y) {
        if (rotatingPreview) {
            previewYaw = (previewYaw + (x - lastDragX) * 2.5F) % 360F;
            previewPitch = (previewPitch + (y - lastDragY) * 2.5F) % 360F;
            lastDragX = x;
            lastDragY = y;
            return true;
        }
        if (!draggingScrollbar) return false;
        int track = CELL * ROWS - 15;
        int position = Math.max(0, Math.min(track, y - top - GRID_TOP - 7));
        scroll = Math.round(position * maxScroll() / (float) track);
        return true;
    }

    void releaseDrag() {
        draggingScrollbar = false;
        rotatingPreview = false;
    }

    private int paneWidth() {
        return (width - previewX - 26) / 2;
    }

    private int thirdPaneX() {
        return left + previewX + 11 + paneWidth();
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
        capture.drawButton(mc, mouseX, mouseY);
    }

    private void drawPreviews(Minecraft mc, int screenWidth, int screenHeight) {
        int x = left + previewX + 7, y = top + 66;
        int pane = paneWidth();
        int height = 104;
        int thirdX = thirdPaneX();
        DescriptionWorkbenchSkin.panel(x, y, pane, height);
        DescriptionWorkbenchSkin.panel(thirdX, y, pane, height);
        drawSnapshot(mc, x, y, pane, height);
        if (mannequin == null || mannequin.worldObj != mc.theWorld) {
            mannequin = new EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.getGameProfile());
        }
        // Preview inventory belongs to the mannequin, not the active player.
        mannequin.setCurrentItemOrArmor(0, previewStack);
        int view = Math.max(12, Math.min(38, (pane - 10) * 2 / 3));
        drawThirdPerson(mc, thirdX, y, pane, height, view, screenWidth, screenHeight);
        drawCaption("mbo.weapon_skin.view.0", x, y - 12, pane);
        drawCaption("mbo.weapon_skin.view.1", thirdX, y - 12, pane);
    }

    private void drawThirdPerson(Minecraft mc, int x, int y, int w, int h, int scale,
                                 int screenWidth, int screenHeight) {
        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT | GL11.GL_ENABLE_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            int leftPixel = (x + 2) * mc.displayWidth / screenWidth;
            int bottomPixel = mc.displayHeight - (y + h - 2) * mc.displayHeight / screenHeight;
            GL11.glScissor(leftPixel, bottomPixel, (w - 4) * mc.displayWidth / screenWidth,
                    (h - 4) * mc.displayHeight / screenHeight);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
            int centerX = x + w / 2;
            int centerY = y + h / 2;
            GL11.glTranslatef(centerX, centerY, 50);
            GL11.glRotatef(previewPitch, 1, 0, 0);
            GL11.glRotatef(previewYaw, 0, 1, 0);
            GL11.glTranslatef(-centerX, -centerY, -50);
            GuiInventory.func_147046_a(centerX, centerY + scale, scale, 0, 0, mannequin);
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private void drawCaption(String key, int x, int y, int width) {
        String text = font.trimStringToWidth(I18n.format(key), width);
        font.drawString(text, x + (width - font.getStringWidth(text)) / 2, y, 0xdac99f);
    }

    private void drawSnapshot(Minecraft mc, int x, int y, int w, int h) {
        if (hasSnapshot) {
            int side = Math.min(w, h) - 6;
            mc.getTextureManager().bindTexture(snapshotLocation);
            GL11.glColor4f(1, 1, 1, 1);
            Gui.func_146110_a(x + (w - side) / 2, y + (h - side) / 2,
                    0, 0, side, side, side, side);
        } else if (waitingForCapture || captureFailed) {
            font.drawSplitString(I18n.format(waitingForCapture ? "mbo.weapon_skin.capturing" : "mbo.weapon_skin.capture_failed"),
                    x + 6, y + h / 2 - 9, w - 12, 0xdac99f);
        }
    }

    private void requestCapture(Minecraft mc) {
        ItemStack expected = previewStack;
        waitingForCapture = true;
        captureFailed = false;
        WeaponSkinCapture.INSTANCE.request(mc.currentScreen, expected, pixels -> {
            waitingForCapture = false;
            if (expected != previewStack) return;
            if (pixels == null) {
                captureFailed = true;
                return;
            }
            if (snapshot == null) {
                snapshot = new DynamicTexture(WeaponSkinCapture.SIZE, WeaponSkinCapture.SIZE);
                snapshotLocation = mc.getTextureManager().getDynamicTextureLocation("mbo_weapon_skin_preview", snapshot);
            }
            System.arraycopy(pixels, 0, snapshot.getTextureData(), 0, pixels.length);
            snapshot.updateDynamicTexture();
            hasSnapshot = true;
        });
    }

    void close(GuiScreen owner) {
        WeaponSkinCapture.INSTANCE.cancel(owner);
        if (snapshotLocation != null) Minecraft.getMinecraft().getTextureManager().deleteTexture(snapshotLocation);
        snapshotLocation = null;
        snapshot = null;
        hasSnapshot = false;
    }

    boolean click(int x, int y, int mouseButton, int window) {
        if (mouseButton != 0) return false;
        updateButtons();
        Minecraft mc = Minecraft.getMinecraft();
        if (capture.mousePressed(mc, x, y)) {
            requestCapture(mc);
            return true;
        }
        int thirdX = thirdPaneX();
        if (previewStack != null && x >= thirdX && x < thirdX + paneWidth()
                && y >= top + 66 && y < top + 170) {
            rotatingPreview = true;
            lastDragX = x;
            lastDragY = y;
            return true;
        }
        if (x >= left + 12 && x < left + previewX - 12 && y >= top + GRID_TOP
                && y < top + GRID_TOP + CELL * ROWS) {
            if (x >= left + previewX - 28) {
                draggingScrollbar = maxScroll() > 0;
                drag(x, y);
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
