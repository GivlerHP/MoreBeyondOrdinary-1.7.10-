package ru.givler.mbo.client.gui;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.item.skin.WardrobeSlot;

import static ru.givler.mbo.item.skin.WardrobeSlot.*;

final class WardrobeSlotAtlas {
    private static final ResourceLocation TEXTURE = new ResourceLocation("mbo", "textures/gui/wardrobe_slots.png");
    private static final int WIDTH = 302, HEIGHT = 458, SPRITE = 36, STEP = 38, DISPLAY = 18;
    private static final Map<WardrobeSlot, Integer> CELLS = new EnumMap<>(WardrobeSlot.class);

    static {
        row(0, LIGHT_HEAD, LIGHT_CHEST, LIGHT_LEGS, LIGHT_FEET);
        row(44, HEAVY_HEAD, HEAVY_CHEST, HEAVY_LEGS, HEAVY_FEET);
        row(88, MAGIC_HEAD, MAGIC_CHEST, MAGIC_LEGS, MAGIC_FEET);
        row(132, SWORD, GREATSWORD, KATANA, RAPIER, DAGGER, DUAL_DAGGER, MACE, WARHAMMER);
        row(170, WARAXE, BATTLEAXE, SPEAR, LANCE, HALBERD, WAR_SCYTHE);
        row(214, BOW, CROSSBOW);
        row(258, PICKAXE, AXE, SHOVEL, HOE, SHEARS, HANDPICK, HEAVY_PICK, HEAVY_SHOVEL);
        row(296, LUMBER_AXE, MATTOCK, SCYTHE, TROWEL, LIGHTER, CRAFT_HAMMER, KNIFE, NEEDLE);
        row(334, SAW, TONGS, SPANNER, PAINT_BRUSH);
        row(378, SHIELD);
        row(422, STAFF);
    }

    private WardrobeSlotAtlas() {}

    private static void row(int y, WardrobeSlot... slots) {
        for (int i = 0; i < slots.length; i++) CELLS.put(slots[i], y * WIDTH + i * STEP);
    }

    static void draw(WardrobeSlot slot, int x, int y) {
        int pixel = CELLS.get(slot);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1, 1, 1, 1);
        Gui.func_152125_a(x, y, pixel % WIDTH, pixel / WIDTH, SPRITE, SPRITE, DISPLAY, DISPLAY, WIDTH, HEIGHT);
    }
}
