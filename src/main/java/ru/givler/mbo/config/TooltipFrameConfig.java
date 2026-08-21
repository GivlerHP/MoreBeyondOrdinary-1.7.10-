package ru.givler.mbo.config;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/** Per-item ornamental tooltip frames. */
public final class TooltipFrameConfig {
    private static final String CATEGORY = "custom_frames";
    private static final String COLOR_CATEGORY = "background_colors";
    private static final ResourceLocation DEFAULT_FRAME = frame("ramka1");
    private static final int DEFAULT_BACKGROUND = 0xF0150B01;
    private static final Map<String, ResourceLocation> FRAMES = new HashMap<String, ResourceLocation>();
    private static final Map<String, Integer> BACKGROUNDS = new HashMap<String, Integer>();

    private TooltipFrameConfig() {}

    public static void load(File configDir) {
        Configuration cfg = new Configuration(new File(configDir, "MoreBeyondOrdinary/tooltip.cfg"));
        try {
            cfg.load();
            cfg.get(CATEGORY, "mbo:DragonSlayer", "ramka2",
                    "Item registry name = frame texture beside textures/gui/tooltip/ramka1.png");
            cfg.get(COLOR_CATEGORY, "mbo:DragonSlayer", "#06181B",
                    "Tooltip background color in #RRGGBB format");
            FRAMES.clear();
            ConfigCategory category = cfg.getCategory(CATEGORY);
            for (Map.Entry<String, Property> entry : category.entrySet()) {
                String itemName = entry.getKey().trim();
                String textureName = entry.getValue().getString().trim();
                if (!itemName.isEmpty() && !textureName.isEmpty()) {
                    FRAMES.put(itemName, frame(textureName));
                }
            }
            BACKGROUNDS.clear();
            ConfigCategory colors = cfg.getCategory(COLOR_CATEGORY);
            for (Map.Entry<String, Property> entry : colors.entrySet()) {
                String itemName = entry.getKey().trim();
                Integer color = parseColor(entry.getValue().getString());
                if (!itemName.isEmpty() && color != null) BACKGROUNDS.put(itemName, color);
            }
        } catch (Exception e) {
            System.err.println("[MBO] Failed to load tooltip.cfg: " + e);
        } finally {
            if (cfg.hasChanged()) cfg.save();
        }
    }

    public static ResourceLocation getFrame(ItemStack stack) {
        if (stack != null && stack.getItem() != null) {
            GameRegistry.UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(stack.getItem());
            if (id != null) {
                ResourceLocation custom = FRAMES.get(id.toString());
                if (custom != null) return custom;
            }
        }
        return DEFAULT_FRAME;
    }

    public static int getBackground(ItemStack stack) {
        String id = getItemId(stack);
        Integer custom = id == null ? null : BACKGROUNDS.get(id);
        return custom == null ? DEFAULT_BACKGROUND : custom;
    }

    private static String getItemId(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return null;
        GameRegistry.UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(stack.getItem());
        return id == null ? null : id.toString();
    }

    private static Integer parseColor(String value) {
        if (value == null) return null;
        String text = value.trim();
        if (text.startsWith("#")) text = text.substring(1);
        else if (text.startsWith("0x") || text.startsWith("0X")) text = text.substring(2);
        try {
            long parsed = Long.parseLong(text, 16);
            if (text.length() <= 6) parsed |= 0xF0000000L;
            return (int) parsed;
        } catch (NumberFormatException ignored) {
            System.err.println("[MBO] Invalid tooltip background color: " + value);
            return null;
        }
    }

    private static ResourceLocation frame(String name) {
        String path = name.replace('\\', '/');
        if (path.endsWith(".png")) path = path.substring(0, path.length() - 4);
        return new ResourceLocation("mbo", "textures/gui/tooltip/" + path + ".png");
    }
}
