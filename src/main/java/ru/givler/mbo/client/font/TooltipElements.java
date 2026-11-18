package ru.givler.mbo.client.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Image tags available inside item tooltip strings, for example <mbo:weight>. */
public final class TooltipElements {
    private static final String PREFIX = "<mbo:";
    private static final Map<String, Element> ELEMENTS = new LinkedHashMap<String, Element>();
    private static final Map<String, Element> STAT_RAILS = new LinkedHashMap<String, Element>();

    static {
        addBlock("needatribute", 16, 80, 12, 7, 12);
        addBlock("upatribute", 16, 80, 12, 7, 12);
        addBlock("swordstat", 13, 76, 13, 7, 13);
        addBlock("plaska", 16, 48, 2, 7, 2);
        // Independent stat rail and icons: a rail never consumes horizontal text space.
        ELEMENTS.put("statline", new Element("plaska", 13, 48, true, 0, 7, 10));
        ELEMENTS.put("statrail", new Element("plaska", 13, 48, true, 0, 7, 0));
        // Coordinates in rendered pixels, after each icon's texture has been scaled.
        addStatRail("armour", 5, 12);
        addStatRail("weapon", 5, 10);
        addStatRail("handling", 6, 12);
        addStatRail("mounted", 6, 12);
        addStatRail("magicguard", 7, 12);
        addStatRail("fireguard", 7, 10);
        addStatRail("scroll", 6, 12);
        addStatRail("spellscroll", 6, 12);
        addStatRail("enchants", 6, 12);
        addStatRail("viscapacity", 6, 12);
        addStatRail("viscost", 6, 12);
        addStatRail("wandfocus", 6, 12);
        addThaumcraftIcon("viscapacity", "wand_rod_greatwood.png");
        addThaumcraftIcon("viscost", "focus.png");
        addThaumcraftIcon("wandfocus", "focus.png");
        ELEMENTS.put("fireguard", new Element("fireguard", 13, 13, false, 13, -1, -1));
        ELEMENTS.put("weapon", new Element("swordstat", 13, 76, false, 13, -1, -1));
        ELEMENTS.put("armour", new Element("passiveatribute", 12, 12, false, 12, -1, -1));
        ELEMENTS.put("magicguard", new Element("magicguard", 13, 13, false, 13, -1, -1));
        ELEMENTS.put("speed", new Element("needatribute", 13, 80, false, 12, -1, -1));
        ELEMENTS.put("reach", new Element("needatribute", 13, 80, false, 12, -1, -1));
        ELEMENTS.put("handling", new Element("needatribute", 13, 80, false, 12, -1, -1));
        ELEMENTS.put("mounted", new Element("upatribute", 13, 80, false, 12, -1, -1));
        add("passiveatribute", 12, 12);
        add("voklicatel", 18, 24);
        add("weight", 9, 11);
        add("alt", 24, 10);
        add("alt_a", 24, 12);
        add("ctrl", 24, 10);
        add("ctrl_a", 24, 10);
        add("ctrlalt", 40, 10);
        add("ctrlalt_a", 40, 10);
        add("shift", 26, 10);
        add("shift_a", 26, 10);
        for (ru.givler.mbo.magic.api.MagicElement element : ru.givler.mbo.magic.api.MagicElement.values())
            add("element_" + element.name().toLowerCase(java.util.Locale.ROOT), 8, 8);
        ELEMENTS.put("spellscroll", new Element("spellscroll", 13, 13, false, 13, -1, -1,
                new ResourceLocation("minecraft", "textures/items/map_empty.png")));
        ELEMENTS.put("upbonus", new Element("upatribute", 13, 80, false, 12, -1, -1));
        ELEMENTS.put("durability", new Element("durability", 13, 13, false, 13, -1, -1,
                new ResourceLocation("minecraft", "textures/items/iron_pickaxe.png")));
        ELEMENTS.put("enchants", new Element("enchants", 13, 13, false, 13, -1, -1,
                new ResourceLocation("minecraft", "textures/items/book_enchanted.png")));
        ELEMENTS.put("scroll", new Element("scroll", 13, 13, false, 13, -1, -1,
                new ResourceLocation("minecraft", "textures/items/map_empty.png")));
        ELEMENTS.put("ingot", new Element("ingot", 12, 12, false, 12, -1, -1,
                new ResourceLocation("minecraft", "textures/items/iron_ingot.png")));
        ELEMENTS.put("timber", new Element("timber", 12, 12, false, 12, -1, -1,
                new ResourceLocation("minefantasy2", "textures/items/custom/component/plank.png")));
    }

    private TooltipElements() {}

    private static void addThaumcraftIcon(String name, String texture) {
        ELEMENTS.put(name, new Element(name, 13, 13, false, 13, -1, -1,
                new ResourceLocation("thaumcraft", "textures/items/" + texture)));
    }

    private static void add(String name, int width, int height) {
        ELEMENTS.put(name, new Element(name, width, height, false, height, -1, -1));
    }

    private static void addBlock(String name, int width, int height,
                                 int iconHeight, int lineX, int lineY) {
        ELEMENTS.put(name, new Element(name, width, height, true, iconHeight, lineX, lineY));
    }

    private static void addStatRail(String icon, int x, int y) {
        STAT_RAILS.put(icon, new Element("plaska", 13, 48, true, 0, x, y));
    }

    public static Token tokenAt(String text, int offset) {
        if (text == null || offset < 0 || !text.startsWith(PREFIX, offset)) return null;
        int end = text.indexOf('>', offset + PREFIX.length());
        if (end < 0) return null;
        String name = text.substring(offset + PREFIX.length(), end);
        if (name.startsWith("focusicon:")) {
            String sprite = name.substring("focusicon:".length());
            if (!sprite.matches("[a-zA-Z0-9_./:-]+")) return null;
            Element icon = new Element("focusicon:" + sprite, 13, 10, false, 10, -1, -1,
                    TextureMap.locationItemsTexture);
            return new Token(end + 1, icon, 16, -1, false);
        }
        if (name.startsWith("color:") && name.substring(6).matches("[0-9a-fA-F]{6}")) {
            return new Token(end + 1, null, 0, -2, false, Integer.parseInt(name.substring(6), 16));
        }
        if (name.startsWith("itemtex:")) {
            String[] parts = name.split(":", 3);
            if (parts.length != 3 || !parts[1].matches("[a-z0-9_]+")
                    || !parts[2].matches("[a-zA-Z0-9_/]+")) return null;
            Element item = new Element("itemtex", 12, 12, false, 12, -1, -1,
                    new ResourceLocation(parts[1], "textures/items/" + parts[2] + ".png"));
            return new Token(end + 1, item, 16, -1, false);
        }
        if (name.startsWith("aspect:")) {
            String[] parts = name.split(":", 4);
            if (parts.length < 3 || !parts[1].matches("[a-z0-9_.-]+")
                    || !parts[2].matches("textures/[a-zA-Z0-9_./-]+\\.png")
                    || parts[2].contains("..")
                    || (parts.length == 4 && !parts[3].matches("[0-9a-fA-F]{6}"))) return null;
            Element aspect = new Element("aspect", 8, 8, false, 8, -1, -1,
                    new ResourceLocation(parts[1], parts[2]));
            int tint = parts.length == 4 ? Integer.parseInt(parts[3], 16) : 0xffffff;
            return new Token(end + 1, aspect, 10, -1, false, tint);
        }
        if (name.startsWith("ingot:") || name.startsWith("timber:")) {
            int separator = name.indexOf(':');
            String color = name.substring(separator + 1);
            if (!color.matches("[0-9a-fA-F]{6}")) return null;
            Element material = ELEMENTS.get(name.substring(0, separator));
            return new Token(end + 1, material, 16, -1, false, Integer.parseInt(color, 16));
        }
        if ("end".equals(name)) {
            return new Token(end + 1, null, 0, -1, true);
        }
        if (name.startsWith("pad:")) {
            try {
                return new Token(end + 1, null, Integer.parseInt(name.substring(4)), -1, false);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        int renderHeight = -1;
        int heightSeparator = name.lastIndexOf('@');
        if (heightSeparator >= 0) {
            try {
                renderHeight = Integer.parseInt(name.substring(heightSeparator + 1));
                name = name.substring(0, heightSeparator);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        Element element = ELEMENTS.get(name);
        if (name.equals("statline")) {
            for (Map.Entry<String, Element> rail : STAT_RAILS.entrySet()) {
                if (text.startsWith(PREFIX + rail.getKey() + ">", end + 1)) {
                    element = rail.getValue();
                    break;
                }
            }
        }
        return element == null ? null : new Token(end + 1, element,
                (name.equals("statline") || name.equals("statrail")) ? 0
                        : name.equals("armour") ? 16 : element.width + 3, renderHeight, false);
    }

    public static boolean containsTag(String text) {
        return text != null && text.indexOf(PREFIX) >= 0;
    }

    public static float draw(Token token, float x, float y) {
        if (token.element == null) return token.advance;
        Element e = token.element;
        if (e.name.equals("magicguard") || e.name.equals("fireguard")) {
            drawProtectionIcon(x, y, e.name);
            return token.advance;
        }
        Minecraft.getMinecraft().getTextureManager().bindTexture(e.texture);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(((token.tint >> 16) & 255) / 255F,
                ((token.tint >> 8) & 255) / 255F, (token.tint & 255) / 255F, 1F);
        if (e.name.startsWith("focusicon:")) {
            ITextureObject texture = Minecraft.getMinecraft().getTextureManager().getTexture(e.texture);
            if (texture instanceof TextureMap) {
                IIcon icon = ((TextureMap) texture).getAtlasSprite(e.name.substring("focusicon:".length()));
                quad(x + 2, y - 1, 10, 10, icon.getMinU(), icon.getMinV(), icon.getMaxU(), icon.getMaxV());
            }
        } else if (!e.block) {
            quad(x + (e.name.startsWith("element_") ? 2 : 0), y, e.name.equals("swordstat") ? 10 : e.width,
                    e.name.equals("swordstat") ? 10 : e.iconHeight, 0, 0, 1, e.iconHeight / (float) e.height);
        } else {
            int height = Math.max(e.iconHeight, token.renderHeight);
            if (e.iconHeight > 0) quad(x, y, e.width, e.iconHeight, 0, 0, 1, e.iconHeight / (float) e.height);
            int textureWidth = e.iconHeight == 0 ? 16 : e.width;
            int sampleX = e.iconHeight == 0 ? 7 : e.lineX;
            float u0 = sampleX / (float) textureWidth;
            float u1 = (sampleX + 1) / (float) textureWidth;
            int sampleY = e.iconHeight == 0 ? 2 : e.lineY;
            float v0 = sampleY / (float) e.height;
            float v1 = (sampleY + 1) / (float) e.height;
            // Keep the rail solid through the text, then fade its extended lower tip.
            int fadeStart = Math.max(e.lineY, height - 5);
            if (fadeStart > e.lineY) quad(x + e.lineX, y + e.lineY, 1, fadeStart - e.lineY, u0, v0, u1, v1);
            if (height > fadeStart) fadingRail(x + e.lineX, y + fadeStart, height - fadeStart, u0, v0, u1, v1);
        }
        GL11.glColor4f(1F, 1F, 1F, 1F);
        return token.advance;
    }

    /** Ward item texture; the fire-resistance effect comes from the active pack. */
    private static void drawProtectionIcon(float x, float y, String name) {
        boolean magic = name.equals("magicguard");
        ResourceLocation atlas = magic ? new ResourceLocation("mbo", "textures/items/bijouterie/amulet_cleansing.png")
                : new ResourceLocation("minecraft", "textures/gui/container/inventory.png");
        Minecraft.getMinecraft().getTextureManager().bindTexture(atlas);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        if (magic) {
            quad(x + 1, y - 1, 12, 12, 0, 0, 1, 1);
        } else {
            quad(x + 2, y, 10, 10, 126 / 256F, 216 / 256F, 144 / 256F, 234 / 256F);
        }
    }

    private static void quad(float x, float y, float width, float height,
                             float u0, float v0, float u1, float v1) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.addVertexWithUV(x, y + height, 0, u0, v1);
        t.addVertexWithUV(x + width, y + height, 0, u1, v1);
        t.addVertexWithUV(x + width, y, 0, u1, v0);
        t.addVertexWithUV(x, y, 0, u0, v0);
        t.draw();
    }

    private static void fadingRail(float x, float y, float height, float u0, float v0, float u1, float v1) {
        boolean alphaTest = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        int shadeModel = GL11.glGetInteger(GL11.GL_SHADE_MODEL);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setColorRGBA_F(1F, 1F, 1F, 0F);
        t.addVertexWithUV(x, y + height, 0, u0, v1);
        t.addVertexWithUV(x + 1, y + height, 0, u1, v1);
        t.setColorRGBA_F(1F, 1F, 1F, 1F);
        t.addVertexWithUV(x + 1, y, 0, u1, v0);
        t.addVertexWithUV(x, y, 0, u0, v0);
        t.draw();
        GL11.glShadeModel(shadeModel);
        if (alphaTest) GL11.glEnable(GL11.GL_ALPHA_TEST);
    }

    /** Extends each block until the next block and indents all contained rows. */
    public static void expandBlocks(List lines) {
        if (lines == null) return;
        List<BlockStart> blocks = new java.util.ArrayList<BlockStart>();
        List<Integer> endings = new java.util.ArrayList<Integer>();
        for (int i = 0; i < lines.size(); i++) {
            String line = String.valueOf(lines.get(i));
            for (int offset = 0; offset < line.length();) {
                Token token = tokenAt(line, offset);
                if (token != null) {
                    if (token.endMarker) endings.add(i);
                    if (token.element != null && token.element.block) {
                        blocks.add(new BlockStart(i, offset, token.end, token.element));
                        break;
                    }
                    offset = token.end;
                } else {
                    offset++;
                }
            }
        }
        for (int b = blocks.size() - 1; b >= 0; b--) {
            BlockStart start = blocks.get(b);
            int endLine = b + 1 < blocks.size() ? blocks.get(b + 1).line : lines.size();
            for (Integer ending : endings) {
                if (ending > start.line && ending < endLine) {
                    endLine = ending;
                    break;
                }
            }
            int height = Math.max(start.element.iconHeight, (endLine - start.line) * 10 + 3);
            String line = String.valueOf(lines.get(start.line));
            if (start.element.iconHeight == 0 && endLine - start.line == 1) {
                // A standalone resistance icon needs no one-pixel rail below it.
                lines.set(start.line, line.substring(0, start.offset) + line.substring(start.end));
                continue;
            }
            String tagName = start.element.iconHeight == 0 ? (start.element.lineY == 0 ? "statrail" : "statline") : start.element.name;
            String dynamicTag = PREFIX + tagName + "@" + height + ">";
            lines.set(start.line, line.substring(0, start.offset) + dynamicTag + line.substring(start.end));
            String padding = PREFIX + "pad:" + (start.element.width + 3) + ">";
            for (int row = start.line + 1; row < endLine; row++) {
                String content = String.valueOf(lines.get(row));
                if (!content.startsWith(padding)) lines.set(row, padding + content);
            }
        }
        // Reserve a real layout row below a final rail. NEI measures the list itself,
        // so extending only the frame would leave its tooltip sizing unchanged.
        if (!blocks.isEmpty()) {
            int lastStart = blocks.get(blocks.size() - 1).line;
            boolean hasEnding = false;
            for (Integer ending : endings) if (ending > lastStart) hasEnding = true;
            if (!hasEnding) lines.add(PREFIX + "end>");
        }
    }

    public static final class Token {
        public final int end;
        private final Element element;
        public final int advance;
        private final int renderHeight;
        private final boolean endMarker;
        private final int tint;
        public final int textColor;

        private Token(int end, Element element, int advance, int renderHeight, boolean endMarker) {
            this(end, element, advance, renderHeight, endMarker, 0xffffff);
        }

        private Token(int end, Element element, int advance, int renderHeight, boolean endMarker, int tint) {
            this.end = end;
            this.element = element;
            this.advance = Math.max(0, advance);
            this.renderHeight = renderHeight;
            this.endMarker = endMarker;
            this.tint = tint;
            this.textColor = renderHeight == -2 ? tint : -1;
        }
    }

    private static final class Element {
        final String name;
        final int width, height, iconHeight, lineX, lineY;
        final boolean block;
        final ResourceLocation texture;

        Element(String name, int width, int height, boolean block,
                int iconHeight, int lineX, int lineY) {
            this(name, width, height, block, iconHeight, lineX, lineY,
                    new ResourceLocation("mbo", "textures/gui/tooltip/" + name + ".png"));
        }

        Element(String name, int width, int height, boolean block,
                int iconHeight, int lineX, int lineY, ResourceLocation texture) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.block = block;
            this.iconHeight = iconHeight;
            this.lineX = lineX;
            this.lineY = lineY;
            this.texture = texture;
        }
    }

    private static final class BlockStart {
        final int line, offset, end;
        final Element element;

        BlockStart(int line, int offset, int end, Element element) {
            this.line = line;
            this.offset = offset;
            this.end = end;
            this.element = element;
        }
    }
}
