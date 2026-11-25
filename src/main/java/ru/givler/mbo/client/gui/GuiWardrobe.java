package ru.givler.mbo.client.gui;

import java.util.EnumMap;
import java.util.Map;
import cpw.mods.fml.common.Loader;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.container.ContainerWardrobe;
import ru.givler.mbo.item.skin.*;

public final class GuiWardrobe extends GuiContainer {
    private static final int PREVIEW_Y = 46, PREVIEW_W = 161, PREVIEW_H = 81;
    private static final int TAB_X = 8, TAB_Y = 22, TAB_H = 19;
    private static final int SLOTS_PER_PAGE = 15, PAGE_Y = 48, PREVIOUS_PAGE_X = 104, NEXT_PAGE_X = 132;
    private static final int ARMOR_TOP = 71, ARMOR_STEP = 52, ARMOR_ROW_HEIGHT = 48;
    private static final ResourceLocation TOGGLES = new ResourceLocation("mbo", "textures/gui/wardrobe_toggles.png");
    private static final String[] ARMOR = {"light", "heavy", "magic"};
    private static final String[] PARTS = {"head", "chest", "legs", "feet"};
    private enum Category {
        ARMOR, MELEE, RANGED, TOOLS, SHIELD, STAFF;

        static Category of(WardrobeSlot slot) {
            if (slot.armor()) return ARMOR;
            if (slot.ordinal() < WardrobeSlot.BOW.ordinal()) return MELEE;
            if (slot == WardrobeSlot.BOW || slot == WardrobeSlot.CROSSBOW) return RANGED;
            if (slot == WardrobeSlot.SHIELD) return SHIELD;
            if (slot == WardrobeSlot.STAFF) return STAFF;
            return TOOLS;
        }

        String title() { return I18n.format("mbo.wardrobe.category." + name().toLowerCase(java.util.Locale.ROOT)); }
    }
    private static final Category[] CATEGORIES = Category.values();
    private final ContainerWardrobe wardrobe;
    private final Map<WardrobeSlot, ItemStack> examples = new EnumMap<>(WardrobeSlot.class);
    private final RenderItem icons = new RenderItem();
    private final GuiButton[] tabs = new GuiButton[CATEGORIES.length];
    private final ItemStack[] tabIcons = new ItemStack[CATEGORIES.length];
    private EntityOtherPlayerMP mannequin;
    private Category category = Category.ARMOR;
    private int page, pageCount = 1;
    private int extraWidth, tabWidth, cardStep;
    private WardrobeSlot held = WardrobeSlot.SWORD;
    private boolean showHeld = true, showShield, compare, rotating;
    private int armorParts = 0b1111;
    private int armorGroup = 2, lastX, lastY;
    private float yaw = 25F, pitch = -8F;

    public GuiWardrobe(EntityPlayer player) {
        super(new ContainerWardrobe(player));
        wardrobe = (ContainerWardrobe) inventorySlots;
        xSize = 320;
        ySize = 237;
    }

    @Override public void initGui() {
        xSize = Math.max(320, Math.min(354, width - 8));
        extraWidth = xSize - 320;
        tabWidth = (xSize - 16) / CATEGORIES.length;
        cardStep = 46 + extraWidth / 3;
        super.initGui();
        for (Category tab : CATEGORIES) {
            int index = tab.ordinal();
            tabs[index] = new GuiButton(index, guiLeft + TAB_X + index * tabWidth, guiTop + TAB_Y, tabWidth - 2, TAB_H, "");
            tabIcons[index] = tabIcon(tab);
        }
        for (int index = 0; index < 36; index++) {
            wardrobe.getSlot(WardrobeSlot.ALL.length + index).xDisplayPosition = right(154) + index % 9 * 18;
        }
        positionSlots();
    }

    private int right(int x) { return x + extraWidth; }

    private ItemStack tabIcon(Category tab) {
        switch (tab) {
            case ARMOR: return example(WardrobeSlot.HEAVY_CHEST);
            case MELEE: return example(WardrobeSlot.SWORD);
            case RANGED: return example(WardrobeSlot.BOW);
            case TOOLS: return new ItemStack(Items.golden_pickaxe);
            case SHIELD: return example(WardrobeSlot.SHIELD);
            case STAFF:
                ItemStack staff = example(WardrobeSlot.STAFF);
                return staff == null ? new ItemStack(Items.blaze_rod) : staff;
            default: return null;
        }
    }

    private void positionSlots() {
        int visible = 0;
        for (WardrobeSlot type : WardrobeSlot.ALL) {
            net.minecraft.inventory.Slot slot = wardrobe.getSlot(type.ordinal());
            slot.xDisplayPosition = -1000;
            slot.yDisplayPosition = -1000;
            if (Category.of(type) != category) continue;
            if (category == Category.ARMOR) {
                slot.xDisplayPosition = armorCenter(type.part()) - 8;
                slot.yDisplayPosition = armorRowY(visible / 4) + 14;
            } else {
                int index = visible - page * SLOTS_PER_PAGE;
                if (index >= 0 && index < SLOTS_PER_PAGE) {
                    slot.xDisplayPosition = 13 + index % 3 * cardStep;
                    slot.yDisplayPosition = 62 + index / 3 * 35;
                }
            }
            visible++;
        }
        pageCount = Math.max(1, (visible + SLOTS_PER_PAGE - 1) / SLOTS_PER_PAGE);
    }

    private int armorCenter(int part) { return 10 + (2 * part + 1) * (136 + extraWidth) / 8; }

    private static int armorRowY(int group) { return ARMOR_TOP + group * ARMOR_STEP; }

    @Override protected void drawGuiContainerBackgroundLayer(float partial, int mouseX, int mouseY) {
        DescriptionWorkbenchSkin.background(guiLeft, guiTop, xSize, ySize);
        DescriptionWorkbenchSkin.panel(guiLeft + 6, guiTop + 46, 144 + extraWidth, 187);
        DescriptionWorkbenchSkin.panel(guiLeft + right(153), guiTop + PREVIEW_Y, PREVIEW_W, PREVIEW_H);
        DescriptionWorkbenchSkin.inventory(guiLeft + right(153), guiTop + 157);
        if (category == Category.ARMOR) drawArmorRows();
        for (Category tab : CATEGORIES) {
            GuiButton button = tabs[tab.ordinal()];
            button.enabled = tab != category;
            button.drawButton(mc, mouseX, mouseY);
            drawIcon(tabIcons[tab.ordinal()], button.xPosition + (button.width - 16) / 2, button.yPosition + 1);
        }
        for (WardrobeSlot type : WardrobeSlot.ALL) {
            net.minecraft.inventory.Slot slot = wardrobe.getSlot(type.ordinal());
            if (slot.xDisplayPosition < 0) continue;
            int x = guiLeft + slot.xDisplayPosition, y = guiTop + slot.yDisplayPosition;
            if (type.armor()) drawArmorCard(x, y, selected(type));
            else drawCard(x, y, selected(type));
            WardrobeSlotAtlas.draw(type, x - 1, y - 1);
        }
        drawMannequin();
    }

    private void drawArmorRows() {
        int left = guiLeft + 9, right = guiLeft + 147 + extraWidth;
        for (int group = 0; group < ARMOR.length; group++) {
            int top = guiTop + armorRowY(group), bottom = top + ARMOR_ROW_HEIGHT;
            boolean selected = armorGroup == group;
            drawRect(left, top, right, bottom, selected ? 0xFF765A3B : 0xFF4A382E);
            drawRect(left + 1, top + 1, right - 1, bottom - 1, selected ? 0xFF392B21 : 0xFF2D221D);
        }
    }

    private void drawArmorCard(int x, int y, boolean selected) {
        drawRect(x - 3, y - 2, x + 20, y + 29, selected ? 0xFF765A3B : 0xFF4A382E);
        drawRect(x - 2, y - 1, x + 19, y + 28, selected ? 0xFF3D2F22 : 0xFF2D221D);
    }

    private void drawCard(int x, int y, boolean selected) {
        int left = x - 4, top = y - 3, right = left + cardStep - 3, bottom = top + 34;
        drawRect(left, top, right, bottom, selected ? 0xFF765A3B : 0xFF4A382E);
        drawRect(left + 1, top + 1, right - 1, bottom - 1, selected ? 0xFF3D2F22 : 0xFF2D221D);
        drawRect(left + 1, bottom - 1, right, bottom, 0xFF1F1714);
    }

    private ItemStack example(WardrobeSlot slot) {
        if (!examples.containsKey(slot)) {
            Item item = slot.sample();
            examples.put(slot, sampleStack(item));
        }
        return examples.get(slot);
    }

    private static ItemStack sampleStack(Item item) {
        if (item == null) return null;
        if (!Loader.isModLoaded("minefantasy2")) return new ItemStack(item);
        if (item instanceof minefantasy.mf2.item.weapon.ItemShieldMF) {
            return ((minefantasy.mf2.item.weapon.ItemShieldMF) item).construct("Iron");
        }
        if (item instanceof minefantasy.mf2.item.weapon.ItemWeaponMF) {
            return ((minefantasy.mf2.item.weapon.ItemWeaponMF) item).construct("Iron", "OakWood");
        }
        if (item instanceof minefantasy.mf2.item.gadget.ItemCrossbow) {
            java.util.List<ItemStack> variants = new java.util.ArrayList<>();
            item.getSubItems(item, item.getCreativeTab(), variants);
            if (!variants.isEmpty()) return variants.get(0);
        }
        return new ItemStack(item);
    }

    private ItemStack worn(WardrobeSlot slot) {
        ItemStack sample = example(slot);
        if (sample == null) return null;
        ItemStack copy = sample.copy();
        EquipmentSkinDefinition skin = ItemSkinTemplate.skin(wardrobe.template(slot));
        if (skin != null && !compare) EquipmentSkinData.set(copy, skin.id);
        return copy;
    }

    private void drawMannequin() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mannequin == null || mannequin.worldObj != mc.theWorld) {
            mannequin = new PreviewPlayer(mc.theWorld, mc.thePlayer.getGameProfile());
            if (Loader.isModLoaded("minefantasy2")) mannequin.inventory = new InventoryPlayerBattle(mannequin);
        }
        mannequin.setCurrentItemOrArmor(0, showHeld ? worn(held) : null);
        for (int part = 0; part < 4; part++) {
            boolean visible = armorGroup >= 0 && (armorParts & 1 << part) != 0;
            mannequin.setCurrentItemOrArmor(4 - part, visible ? worn(WardrobeSlot.ALL[armorGroup * 4 + part]) : null);
        }
        if (Loader.isModLoaded("minefantasy2")) {
            mannequin.inventory.setInventorySlotContents(InventoryPlayerBattle.OFFHAND_SLOT, showShield ? worn(WardrobeSlot.SHIELD) : null);
        }
        MannequinPreview.draw(mc, mannequin, guiLeft + right(153), guiTop + PREVIEW_Y, PREVIEW_W, PREVIEW_H,
            31, width, height, yaw, pitch);
    }

    public static final class PreviewPlayer extends EntityOtherPlayerMP {
        PreviewPlayer(net.minecraft.world.World world, com.mojang.authlib.GameProfile profile) {
            super(world, profile);
        }
    }

    @Override protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString(I18n.format("mbo.wardrobe.title"), 8, 8, 0xE4D2AC);
        fontRendererObj.drawString(category.title(), 11, 51, 0xE4D2AC);
        if (pageCount > 1) {
            fontRendererObj.drawString("<", right(PREVIOUS_PAGE_X) + 2, PAGE_Y + 3, page > 0 ? 0xE4D2AC : 0x786953);
            fontRendererObj.drawString((page + 1) + "/" + pageCount, right(PREVIOUS_PAGE_X) + 10, PAGE_Y + 3, 0xB9A687);
            fontRendererObj.drawString(">", right(NEXT_PAGE_X) + 2, PAGE_Y + 3, page + 1 < pageCount ? 0xE4D2AC : 0x786953);
        }
        if (showHeld) fontRendererObj.drawString(fontRendererObj.trimStringToWidth(held.label(), 67),
            right(153) + 5, PREVIEW_Y + 6, 0xC3AF86);
        if (category == Category.ARMOR) {
            for (int part = 0; part < PARTS.length; part++) {
                String label = I18n.format("mbo.wardrobe.part." + PARTS[part]);
                fontRendererObj.drawString(label, armorCenter(part) - fontRendererObj.getStringWidth(label) / 2, 61, 0xB9A687);
            }
            for (int group = 0; group < ARMOR.length; group++) {
                String label = I18n.format(group == 1 ? "mbo.wardrobe.armor.heavy.full" : "mbo.wardrobe.armor." + ARMOR[group]);
                fontRendererObj.drawString(label, 13, armorRowY(group) + 3, armorGroup == group ? 0xFFF0C6 : 0xE4D2AC);
            }
        }
        for (WardrobeSlot type : WardrobeSlot.ALL) {
            net.minecraft.inventory.Slot slot = wardrobe.getSlot(type.ordinal());
            if (slot.xDisplayPosition < 0) continue;
            int x = slot.xDisplayPosition, y = slot.yDisplayPosition;
            drawMark(markX(type, slot), markY(type, slot), selected(type) ? 36 : 18);
            if (slot.getHasStack()) drawMark(badgeX(type, slot), markY(type, slot), wardrobe.isDisabled(type) ? 46 : 56);
            if (!type.armor()) {
                drawSlotLabel(type.label(), x - 3, y + 16, selected(type) ? 0xFFF0C6 : 0xC3AF86);
            }
        }
        for (int group = 0; group < ARMOR.length; group++) {
            drawToggle(right(154) + group * 53, 130, I18n.format("mbo.wardrobe.armor." + ARMOR[group]), 52, armorGroup == group, true);
        }
        drawToggle(right(154), 143, I18n.format("mbo.wardrobe.preview.hand"), 58, showHeld, false);
        drawToggle(right(213), 143, WardrobeSlot.SHIELD.label(), 33, showShield, false);
        drawToggle(right(247), 143, I18n.format("mbo.wardrobe.compare"), 67, compare, false);
    }

    private void drawSlotLabel(String label, int x, int y, int color) {
        int split = -1;
        if (fontRendererObj.getStringWidth(label) > cardStep - 5) {
            int best = Integer.MAX_VALUE;
            for (int i = label.indexOf(' '); i >= 0; i = label.indexOf(' ', i + 1)) {
                int width = Math.max(fontRendererObj.getStringWidth(label.substring(0, i)),
                    fontRendererObj.getStringWidth(label.substring(i + 1)));
                if (width < best) {
                    best = width;
                    split = i;
                }
            }
        }
        drawSlotLabelLine(split < 0 ? label : label.substring(0, split), x, split < 0 ? y + 4 : y, color);
        if (split >= 0) drawSlotLabelLine(label.substring(split + 1), x, y + 7, color);
    }

    private void drawSlotLabelLine(String text, int x, int y, int color) {
        int naturalWidth = fontRendererObj.getStringWidth(text);
        int available = cardStep - 5;
        float scale = naturalWidth > available ? (float) available / naturalWidth : 1F;
        GL11.glPushMatrix();
        try {
            GL11.glTranslatef(x + (available - naturalWidth * scale) / 2, y, 0);
            GL11.glScalef(scale, 1, 1);
            fontRendererObj.drawString(text, 0, 0, color);
        } finally { GL11.glPopMatrix(); }
    }

    private void drawIcon(ItemStack sample, int x, int y) {
        if (sample == null) return;
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_CURRENT_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        try {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glColor4f(1, 1, 1, 1);
            icons.renderItemIntoGUI(fontRendererObj, mc.getTextureManager(), sample, x, y);
        } finally { GL11.glPopAttrib(); }
    }

    private void drawToggle(int x, int y, String label, int w, boolean selected, boolean radio) {
        drawMark(x, y, (radio ? 18 : 0) + (selected ? 9 : 0));
        fontRendererObj.drawString(fontRendererObj.trimStringToWidth(label, w - 12), x + 11, y + 1, 0xE4D2AC);
    }

    private void drawMark(int x, int y, int u) {
        GL11.glColor4f(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(TOGGLES);
        Gui.func_146110_a(x, y, u, 0, 9, 9, 65, 9);
    }

    private static int markX(WardrobeSlot type, net.minecraft.inventory.Slot slot) {
        return slot.xDisplayPosition + (type.armor() ? -1 : 18);
    }

    private static int markY(WardrobeSlot type, net.minecraft.inventory.Slot slot) {
        return slot.yDisplayPosition + (type.armor() ? 18 : 3);
    }

    private static int badgeX(WardrobeSlot type, net.minecraft.inventory.Slot slot) {
        return markX(type, slot) + 11;
    }

    private boolean selected(WardrobeSlot slot) {
        if (slot.armor()) return armorGroup == slot.ordinal() / 4 && (armorParts & 1 << slot.part()) != 0;
        if (slot == WardrobeSlot.SHIELD) return showShield;
        return showHeld && held == slot;
    }

    private void togglePreview(WardrobeSlot slot) {
        if (slot.armor()) {
            int group = slot.ordinal() / 4;
            if (armorGroup != group) {
                armorGroup = group;
                armorParts = 0;
            }
            armorParts ^= 1 << slot.part();
            if (armorParts == 0) armorGroup = -1;
        } else if (slot == WardrobeSlot.SHIELD) {
            showShield = !showShield;
        } else {
            showHeld = !selected(slot);
            held = slot;
        }
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partial) {
        if (rotating) {
            yaw = (yaw + (mouseX - lastX) * 2.5F) % 360F;
            pitch = Math.max(-55, Math.min(55, pitch + (mouseY - lastY) * 2F));
            lastX = mouseX;
            lastY = mouseY;
        }
        super.drawScreen(mouseX, mouseY, partial);
        WardrobeSlot badge = badgeAt(mouseX, mouseY);
        if (badge != null && mc.thePlayer.inventory.getItemStack() == null) {
            drawHoveringText(java.util.Collections.singletonList(I18n.format(wardrobe.isDisabled(badge)
                ? "mbo.wardrobe.skin.enable" : "mbo.wardrobe.skin.disable")), mouseX, mouseY, fontRendererObj);
            return;
        }
        Category tab = categoryAt(mouseX, mouseY);
        if (tab != null && mc.thePlayer.inventory.getItemStack() == null) {
            drawHoveringText(java.util.Collections.singletonList(tab.title()), mouseX, mouseY, fontRendererObj);
            return;
        }
        WardrobeSlot mark = at(mouseX, mouseY, true);
        WardrobeSlot hovered = mark != null ? mark : at(mouseX, mouseY, false);
        if (hovered != null && (mark != null || wardrobe.template(hovered) == null)
                && mc.thePlayer.inventory.getItemStack() == null) {
            java.util.List<String> lines = new java.util.ArrayList<>();
            lines.add(hovered.label());
            if (mark != null) lines.add(I18n.format("mbo.wardrobe.preview.select"));
            drawHoveringText(lines, mouseX, mouseY, fontRendererObj);
        }
    }

    @Override protected void renderToolTip(ItemStack stack, int x, int y) {
        if (badgeAt(x, y) == null) super.renderToolTip(stack, x, y);
    }

    private WardrobeSlot badgeAt(int x, int y) {
        for (WardrobeSlot type : WardrobeSlot.ALL) {
            net.minecraft.inventory.Slot slot = wardrobe.getSlot(type.ordinal());
            if (slot.xDisplayPosition >= 0 && slot.getHasStack()
                    && contains(badgeX(type, slot), markY(type, slot), 9, 9, x, y)) return type;
        }
        return null;
    }

    private WardrobeSlot at(int x, int y, boolean mark) {
        for (WardrobeSlot type : WardrobeSlot.ALL) {
            net.minecraft.inventory.Slot slot = wardrobe.getSlot(type.ordinal());
            if (slot.xDisplayPosition < 0) continue;
            if (mark ? contains(markX(type, slot) - 1, markY(type, slot) - 1, 11, 11, x, y)
                    : contains(slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, x, y)) return type;
        }
        return null;
    }

    private boolean contains(int left, int top, int w, int h, int x, int y) {
        int relativeX = x - guiLeft, relativeY = y - guiTop;
        return relativeX >= left && relativeX < left + w && relativeY >= top && relativeY < top + h;
    }

    @Override protected void mouseClicked(int x, int y, int button) {
        WardrobeSlot badge = button == 0 ? badgeAt(x, y) : null;
        if (badge != null) {
            mc.playerController.sendEnchantPacket(wardrobe.windowId, badge.ordinal());
            return;
        }
        if (button == 0 && clickControls(x, y)) return;
        if (button == 0 && contains(right(153), PREVIEW_Y, PREVIEW_W, PREVIEW_H, x, y)) {
            rotating = true;
            lastX = x;
            lastY = y;
            return;
        }
        WardrobeSlot target = button == 0 ? at(x, y, true) : null;
        if (target != null) {
            togglePreview(target);
            return;
        }
        super.mouseClicked(x, y, button);
    }

    private boolean clickControls(int x, int y) {
        Category tab = categoryAt(x, y);
        if (tab != null) {
            if (category != tab) {
                tabs[tab.ordinal()].func_146113_a(mc.getSoundHandler());
                category = tab;
                page = 0;
                positionSlots();
            }
            return true;
        }
        if (pageCount > 1) {
            int direction = contains(right(PREVIOUS_PAGE_X), PAGE_Y, 9, 13, x, y) ? -1
                : contains(right(NEXT_PAGE_X), PAGE_Y, 9, 13, x, y) ? 1 : 0;
            if (direction != 0) {
                page = Math.max(0, Math.min(pageCount - 1, page + direction));
                positionSlots();
                return true;
            }
        }
        for (int group = 0; group < ARMOR.length; group++) {
            if (contains(right(154) + group * 53, 128, 52, 12, x, y)) {
                armorGroup = armorGroup == group ? -1 : group;
                armorParts = 0b1111;
                return true;
            }
        }
        if (contains(right(154), 141, 58, 13, x, y)) showHeld = !showHeld;
        else if (contains(right(213), 141, 33, 13, x, y)) showShield = !showShield;
        else if (contains(right(247), 141, 67, 13, x, y)) compare = !compare;
        else return false;
        return true;
    }

    private Category categoryAt(int x, int y) {
        for (Category tab : CATEGORIES) {
            if (contains(TAB_X + tab.ordinal() * tabWidth, TAB_Y, tabWidth - 2, TAB_H, x, y)) return tab;
        }
        return null;
    }

    @Override protected void mouseMovedOrUp(int x, int y, int button) {
        if (button == 0 && rotating) {
            rotating = false;
            return;
        }
        super.mouseMovedOrUp(x, y, button);
    }
}
