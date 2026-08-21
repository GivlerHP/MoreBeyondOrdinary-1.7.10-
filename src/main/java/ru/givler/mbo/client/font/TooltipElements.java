package ru.givler.mbo.client.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Image tags available inside item tooltip strings, for example <mbo:weight>. */
public final class TooltipElements {
    private static final String PREFIX = "<mbo:";
    private static final Map<String, Element> ELEMENTS = new LinkedHashMap<String, Element>();

    static {
        addBlock("needatribute", 16, 80, 12, 7, 12);
        addBlock("upatribute", 16, 80, 12, 7, 12);
        addBlock("swordstat", 13, 76, 13, 7, 13);
        addBlock("plaska", 16, 48, 2, 6, 2);
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
    }

    private TooltipElements() {}

    private static void add(String name, int width, int height) {
        ELEMENTS.put(name, new Element(name, width, height, false, height, -1, -1));
    }

    private static void addBlock(String name, int width, int height,
                                 int iconHeight, int lineX, int lineY) {
        ELEMENTS.put(name, new Element(name, width, height, true, iconHeight, lineX, lineY));
    }

    public static Token tokenAt(String text, int offset) {
        if (text == null || offset < 0 || !text.startsWith(PREFIX, offset)) return null;
        int end = text.indexOf('>', offset + PREFIX.length());
        if (end < 0) return null;
        String name = text.substring(offset + PREFIX.length(), end);
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
        return element == null ? null : new Token(end + 1, element, element.width + 3, renderHeight, false);
    }

    public static boolean containsTag(String text) {
        return text != null && text.indexOf(PREFIX) >= 0;
    }

    public static float draw(Token token, float x, float y) {
        if (token.element == null) return token.advance;
        Element e = token.element;
        Minecraft.getMinecraft().getTextureManager().bindTexture(e.texture);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        if (!e.block) {
            quad(x, y, e.width, e.height, 0, 0, 1, 1);
        } else {
            int height = Math.max(e.iconHeight, token.renderHeight);
            quad(x, y, e.width, e.iconHeight, 0, 0, 1, e.iconHeight / (float) e.height);
            float u0 = e.lineX / (float) e.width;
            float u1 = (e.lineX + 1) / (float) e.width;
            float v0 = e.lineY / (float) e.height;
            float v1 = (e.lineY + 1) / (float) e.height;
            for (int py = e.lineY; py < height; py++) {
                quad(x + e.lineX, y + py, 1, 1, u0, v0, u1, v1);
            }
        }
        GL11.glColor4f(1F, 1F, 1F, 1F);
        return token.advance;
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
            int height = Math.max(start.element.iconHeight, (endLine - start.line) * 10 - 2);
            String line = String.valueOf(lines.get(start.line));
            String dynamicTag = PREFIX + start.element.name + "@" + height + ">";
            lines.set(start.line, line.substring(0, start.offset) + dynamicTag + line.substring(start.end));
            String padding = PREFIX + "pad:" + (start.element.width + 3) + ">";
            for (int row = start.line + 1; row < endLine; row++) {
                String content = String.valueOf(lines.get(row));
                if (!content.startsWith(padding)) lines.set(row, padding + content);
            }
        }
    }

    public static final class Token {
        public final int end;
        private final Element element;
        public final int advance;
        private final int renderHeight;
        private final boolean endMarker;

        private Token(int end, Element element, int advance, int renderHeight, boolean endMarker) {
            this.end = end;
            this.element = element;
            this.advance = Math.max(0, advance);
            this.renderHeight = renderHeight;
            this.endMarker = endMarker;
        }
    }

    private static final class Element {
        final String name;
        final int width, height, iconHeight, lineX, lineY;
        final boolean block;
        final ResourceLocation texture;

        Element(String name, int width, int height, boolean block,
                int iconHeight, int lineX, int lineY) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.block = block;
            this.iconHeight = iconHeight;
            this.lineX = lineX;
            this.lineY = lineY;
            this.texture = new ResourceLocation("mbo", "textures/gui/tooltip/" + name + ".png");
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
