package ru.givler.mbo.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import ru.givler.mbo.item.skin.EquipmentSkinDefinition.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.client.render.weaponskin.WeaponSkinCapture;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.client.render.weaponskin.WeaponSkinRenderer;
import ru.givler.mbo.item.skin.EquipmentSkinData;
import ru.givler.mbo.item.skin.EquipmentSkinDefinition;
import ru.givler.mbo.item.skin.ItemSkinTemplate;
import ru.givler.mbo.item.skin.WardrobeSlot;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketEquipmentSkin;
import ru.givler.mbo.registry.EquipmentSkinRegistry;

final class EquipmentSkinTab {
    private static final int CELL = 20, CATALOG_OFFSET = 14, GRID_TOP = 68 + CATALOG_OFFSET, ROWS = 4;
    private static final Item[] ARMOR_SAMPLES = {Items.iron_helmet, Items.iron_chestplate, Items.iron_leggings, Items.iron_boots};
    private final List<EquipmentSkinDefinition> catalog = new ArrayList<>();
    private final GuiButton[] categories = new GuiButton[Category.values().length];
    private final ItemStack[] categoryIcons = new ItemStack[categories.length];
    private final RenderItem categoryRenderer = new RenderItem();
    private Category category = Category.WEAPON;
    private GuiTextField search;
    private GuiButton armorView;
    private boolean fullSet = true;
    private int templatePart;
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
        String previousQuery = search == null ? "" : search.getText();
        search = new GuiTextField(font, left + 16, top + 53 + CATALOG_OFFSET, listWidth - 8, 12);
        search.setEnableBackgroundDrawing(false);
        search.setMaxStringLength(80);
        search.setText(previousQuery);
        categoryIcons[Category.WEAPON.ordinal()] = new ItemStack(Items.iron_sword);
        categoryIcons[Category.ARMOR.ordinal()] = new ItemStack(Items.chainmail_chestplate);
        categoryIcons[Category.TOOL.ordinal()] = new ItemStack(Items.golden_pickaxe);
        Item shield = Loader.isModLoaded("minefantasy2") ? GameRegistry.findItem("minefantasy2", "standard_shield") : null;
        categoryIcons[Category.SHIELD.ordinal()] = shield == null ? null : new ItemStack(shield);
        if (categoryIcons[category.ordinal()] == null) category = Category.WEAPON;
        int visibleCategories = shield == null ? categories.length - 1 : categories.length;
        for (Category type : Category.values()) {
            int index = type.ordinal();
            int x = index * listWidth / visibleCategories;
            int next = (index + 1) * listWidth / visibleCategories;
            categories[index] = new GuiButton(10 + index, left + 12 + x, top + 32 + CATALOG_OFFSET,
                    next - x - 1, 18, "");
            categories[index].visible = categoryIcons[index] != null;
        }
        armorView = new GuiButton(15, thirdPaneX(), top + 174, paneWidth(), 20, "");
        capture = new GuiButton(5, left + previewX + 7, top + 174, paneWidth(), 20, "");
        columns = Math.max(1, (listWidth - 21) / CELL);
        filter(null);
        apply = new GuiButton(3, left + 12, top + 174, (listWidth - 4) / 2, 20, I18n.format("mbo.weapon_skin.apply"));
        remove = new GuiButton(4, left + 16 + (listWidth - 4) / 2, top + 174,
                (listWidth - 4) / 2, 20, I18n.format("mbo.weapon_skin.remove"));
    }

    void load(ItemStack stack) {
        item = stack;
        EquipmentSkinDefinition installed = template() ? ItemSkinTemplate.skin(stack) : EquipmentSkinData.resolve(stack);
        if (installed != null) {
            WardrobeSlot target = ItemSkinTemplate.slot(stack);
            if (target != null && target.armor()) templatePart = target.part();
            category = installed.category;
            filter(installed);
        } else if (stack != null && stack.getItem() instanceof ItemArmor && category != Category.ARMOR) {
            category = Category.ARMOR;
            filter(null);
        } else {
            refreshPreview();
        }
    }

    private void filter(EquipmentSkinDefinition preferred) {
        String query = search.getText().trim().toLowerCase(Locale.ROOT);
        catalog.clear();
        for (EquipmentSkinDefinition skin : EquipmentSkinRegistry.ALL) {
            if (skin.category == category && I18n.format(skin.titleKey()).toLowerCase(Locale.ROOT).contains(query)) {
                catalog.add(skin);
            }
        }
        selected = Math.max(0, catalog.indexOf(preferred));
        scroll = Math.max(0, Math.min(maxScroll(), selected / columns));
        refreshPreview();
    }

    boolean keyTyped(char character, int key) {
        if (key == org.lwjgl.input.Keyboard.KEY_ESCAPE) {
            if (!search.isFocused()) return false;
            search.setFocused(false);
            return true;
        }
        String query = search.getText();
        if (!search.textboxKeyTyped(character, key)) return false;
        if (!query.equals(search.getText())) filter(null);
        return true;
    }

    void tick() {
        search.updateCursorCounter();
    }

    private boolean armorPreview() {
        return category == Category.ARMOR && !catalog.isEmpty();
    }

    private boolean template() { return item != null && item.getItem() == ItemSkinTemplate.INSTANCE; }

    private void refreshPreview() {
        previewStack = null;
        hasSnapshot = false;
        captureFailed = false;
        if (catalog.isEmpty()) return;
        EquipmentSkinDefinition skin = catalog.get(selected);
        if (armorPreview()) {
            if (template()) {
                for (int i = 0; i < 4 && !skin.supportsArmorSlot(templatePart); i++) templatePart = (templatePart + 1) % 4;
                if (skin.supportsArmorSlot(templatePart)) previewStack = new ItemStack(ARMOR_SAMPLES[templatePart]);
            } else if (compatible()) previewStack = item.copy();
            else for (int slot = 0; slot < ARMOR_SAMPLES.length; slot++) {
                if (skin.supportsArmorSlot(slot)) {
                    previewStack = new ItemStack(ARMOR_SAMPLES[slot]);
                    break;
                }
            }
        } else if (compatible() && !template()) {
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
        if (previewStack != null) EquipmentSkinData.set(previewStack, skin.id);
    }

    private boolean compatible() {
        if (item == null || catalog.isEmpty()) return false;
        EquipmentSkinDefinition skin = catalog.get(selected);
        return template() ? ItemSkinTemplate.target(skin, templatePart) != null : skin.supports(item.getItem());
    }

    private void updateButtons() {
        EquipmentSkinDefinition installed = ItemSkinTemplate.skin(item);
        apply.enabled = compatible() && (template()
            ? installed != catalog.get(selected) || ItemSkinTemplate.slot(item) != ItemSkinTemplate.target(installed, templatePart)
            : !catalog.get(selected).id.equals(EquipmentSkinData.id(item)));
        remove.enabled = installed != null || !EquipmentSkinData.id(item).isEmpty();
        capture.visible = category != Category.ARMOR;
        capture.enabled = previewStack != null && !waitingForCapture;
        armorView.visible = armorPreview();
        WardrobeSlot target = template() && armorPreview() ? ItemSkinTemplate.target(catalog.get(selected), templatePart) : null;
        String view = target == null ? I18n.format(fullSet ? "mbo.weapon_skin.armor.part" : "mbo.weapon_skin.armor.set")
            : I18n.format("mbo.wardrobe.part." + new String[]{"head", "chest", "legs", "feet"}[target.part()]);
        armorView.displayString = font.trimStringToWidth(view, paneWidth() - 6);
        for (Category type : Category.values()) categories[type.ordinal()].enabled = category != type;
        capture.displayString = font.trimStringToWidth(I18n.format("mbo.weapon_skin.capture"), paneWidth() - 6);
    }

    private void drawCatalog(int mouseX, int mouseY) {
        int x = gridX(), y = top + GRID_TOP;
        DescriptionWorkbenchSkin.panel(x - 2, y - 2, gridWidth() + 4, CELL * ROWS + 4);
        int hovered = cellAt(mouseX, mouseY);
        for (int cell = 0; cell < columns * ROWS; cell++) {
            int index = scroll * columns + cell;
            int cellX = x + cell % columns * CELL;
            int cellY = y + cell / columns * CELL;
            DescriptionWorkbenchSkin.inputSlot(cellX + 1, cellY + 1);
            if (index >= catalog.size()) continue;
            if (index == selected || index == hovered) {
                Gui.drawRect(cellX + 1, cellY + 1, cellX + 19, cellY + 19,
                        index == selected ? 0xa07e623e : 0x6049372a);
            }
            EquipmentSkinDefinition skin = catalog.get(index);
            WeaponSkinRenderer.INSTANCE.preview(skin, skin.inventory, cellX + 10, cellY + 16, 8);
        }
        int trackX = x + columns * CELL;
        DescriptionWorkbenchSkin.scrollTrack(trackX, y, CELL * ROWS);
        int offset = maxScroll() == 0 ? 0 : scroll * (CELL * ROWS - 15) / maxScroll();
        DescriptionWorkbenchSkin.scrollThumb(trackX + 1, y + offset, draggingScrollbar);
    }

    private int gridWidth() {
        return columns * CELL + 14;
    }

    private int gridX() {
        return left + 12 + (previewX - 24 - gridWidth()) / 2;
    }

    private int cellAt(int x, int y) {
        int colX = x - gridX(), rowY = y - top - GRID_TOP;
        if (colX < 0 || rowY < 0 || colX >= columns * CELL || rowY >= ROWS * CELL
                || colX % CELL < 1 || colX % CELL >= 19 || rowY % CELL < 1 || rowY % CELL >= 19) return -1;
        int index = (scroll + rowY / CELL) * columns + colX / CELL;
        return index < catalog.size() ? index : -1;
    }

    String hoveredName(int x, int y) {
        for (Category type : Category.values()) {
            GuiButton button = categories[type.ordinal()];
            if (button.visible && x >= button.xPosition && x < button.xPosition + button.width
                    && y >= button.yPosition && y < button.yPosition + button.height) {
                return I18n.format("mbo.weapon_skin.category." + type.name().toLowerCase(Locale.ROOT));
            }
        }
        int index = cellAt(x, y);
        return index < 0 ? null : I18n.format(catalog.get(index).titleKey());
    }

    private int maxScroll() {
        return Math.max(0, (catalog.size() + columns - 1) / columns - ROWS);
    }

    boolean scroll(int x, int y, int wheel) {
        if (wheel == 0 || x < gridX() - 2 || x >= gridX() + gridWidth() + 2
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
        font.drawString(I18n.format("mbo.weapon_skin.catalog"), left + 12, top + 21 + CATALOG_OFFSET, 0xdac99f);
        for (int i = 0; i < categories.length; i++) {
            GuiButton button = categories[i];
            if (!button.visible) continue;
            button.drawButton(mc, mouseX, mouseY);
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_CURRENT_BIT | GL11.GL_COLOR_BUFFER_BIT);
            try {
                GL11.glColor4f(1, 1, 1, 1);
                categoryRenderer.renderItemAndEffectIntoGUI(font, mc.getTextureManager(), categoryIcons[i],
                        button.xPosition + (button.width - 16) / 2, button.yPosition + 1);
            } finally {
                GL11.glPopAttrib();
            }
        }
        DescriptionWorkbenchSkin.field(left + 12, top + 51 + CATALOG_OFFSET, previewX - 24, 15, search.isFocused());
        if (search.getText().isEmpty() && !search.isFocused()) {
            font.drawString(I18n.format("mbo.weapon_skin.search"), left + 16, top + 55 + CATALOG_OFFSET, 0xa29278);
        }
        search.drawTextBox();
        drawCatalog(mouseX, mouseY);
        if (!compatible()) {
            String hint = catalog.isEmpty() ? "mbo.weapon_skin.empty" : item == null
                    ? (category == Category.ARMOR ? "mbo.weapon_skin.armor.insert" : "mbo.weapon_skin.insert")
                    : "mbo.weapon_skin.incompatible";
            font.drawString(font.trimStringToWidth(I18n.format(hint), previewX - 24),
                    left + 12, top + GRID_TOP + CELL * ROWS + 2, 0xdac99f);
        }
        if (previewStack != null && mc.thePlayer != null) {
            drawPreviews(mc, screenWidth, screenHeight);
        }
        apply.drawButton(mc, mouseX, mouseY);
        remove.drawButton(mc, mouseX, mouseY);
        capture.drawButton(mc, mouseX, mouseY);
        armorView.drawButton(mc, mouseX, mouseY);
    }

    private void drawPreviews(Minecraft mc, int screenWidth, int screenHeight) {
        int x = left + previewX + 7, y = top + 66;
        int pane = paneWidth();
        int height = 104;
        int thirdX = thirdPaneX();
        if (mannequin == null || mannequin.worldObj != mc.theWorld) {
            mannequin = new EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.getGameProfile());
        }
        mannequin.setCurrentItemOrArmor(0, armorPreview() ? null : previewStack);
        for (int slot = 0; slot < 4; slot++) mannequin.setCurrentItemOrArmor(4 - slot, null);
        if (armorPreview()) {
            EquipmentSkinDefinition skin = catalog.get(selected);
            int focusSlot = ((ItemArmor) previewStack.getItem()).armorType;
            for (int slot = 0; slot < ARMOR_SAMPLES.length; slot++) {
                if (!skin.supportsArmorSlot(slot) || ((template() || !fullSet) && slot != focusSlot)) continue;
                ItemStack part = slot == focusSlot ? previewStack : new ItemStack(ARMOR_SAMPLES[slot]);
                EquipmentSkinData.set(part, skin.id);
                mannequin.setCurrentItemOrArmor(4 - slot, part);
            }
            int armorWidth = thirdX + pane - x;
            DescriptionWorkbenchSkin.panel(x, y, armorWidth, height);
            drawThirdPerson(mc, x, y, armorWidth, height, 23, screenWidth, screenHeight);
            drawCaption("mbo.weapon_skin.view.1", x, y - 12, armorWidth);
            return;
        }
        DescriptionWorkbenchSkin.panel(x, y, pane, height);
        DescriptionWorkbenchSkin.panel(thirdX, y, pane, height);
        drawSnapshot(mc, x, y, pane, height);
        int view = Math.max(12, Math.min(38, (pane - 10) * 2 / 3));
        drawThirdPerson(mc, thirdX, y, pane, height, view, screenWidth, screenHeight);
        drawCaption("mbo.weapon_skin.view.0", x, y - 12, pane);
        drawCaption("mbo.weapon_skin.view.1", thirdX, y - 12, pane);
    }

    private void drawThirdPerson(Minecraft mc, int x, int y, int w, int h, int scale,
                                 int screenWidth, int screenHeight) {
        MannequinPreview.draw(mc, mannequin, x, y, w, h, scale, screenWidth, screenHeight, previewYaw, previewPitch);
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
        search.mouseClicked(x, y, mouseButton);
        if (search.isFocused()) return true;
        for (Category type : Category.values()) {
            if (categories[type.ordinal()].mousePressed(mc, x, y)) {
                category = type;
                filter(null);
                return true;
            }
        }
        if (armorView.mousePressed(mc, x, y)) {
            if (template()) {
                templatePart = (templatePart + 1) % 4;
                refreshPreview();
            } else fullSet = !fullSet;
            return true;
        }
        if (capture.mousePressed(mc, x, y)) {
            requestCapture(mc);
            return true;
        }
        int thirdX = thirdPaneX();
        if (previewStack != null && x >= (armorPreview() ? left + previewX + 7 : thirdX) && x < thirdX + paneWidth()
                && y >= top + 66 && y < top + 170) {
            rotatingPreview = true;
            lastDragX = x;
            lastDragY = y;
            return true;
        }
        if (x >= gridX() && x < gridX() + gridWidth() && y >= top + GRID_TOP
                && y < top + GRID_TOP + CELL * ROWS) {
            if (x >= gridX() + columns * CELL) {
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
        PacketManager.INSTANCE.sendToServer(new PacketEquipmentSkin(window, item, id, templatePart));
    }
}
