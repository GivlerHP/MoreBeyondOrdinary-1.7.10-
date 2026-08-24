package ru.givler.mbo.client.font;

import com.google.gson.*;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Compatibility renderer for modern resource-pack fonts. Supported providers:
 * bitmap, space, unihex and reference. Missing glyphs fall back to 1.7.10.
 */
@SideOnly(Side.CLIENT)
public final class ModernFontRenderer extends FontRenderer implements IResourceManagerReloadListener {
    private static final ResourceLocation DEFAULT = new ResourceLocation("minecraft", "font/default.json");
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final ResourceLocation LEGACY_ASCII = new ResourceLocation("minecraft", "textures/font/ascii_old.png");
    private static final int ATLAS_SIZE = 2048;
    private static final int CELL = 18;
    // Slightly wider than the exact legacy average: the exact 0.678 ratio is
    // too small visually in 1.7.10 GUIs, while 0.78 still fits old layouts.
    private static final float LEGACY_UNICODE_SCALE = 0.78F;

    private final TextureManager textures;
    private final FontRenderer legacyRenderer;
    private final Map<Integer, Glyph> glyphs = new HashMap<Integer, Glyph>();
    private final Map<Integer, Float> spaces = new HashMap<Integer, Float>();
    private final Set<String> loading = new HashSet<String>();
    private final List<DynamicTexture> hexAtlases = new ArrayList<DynamicTexture>();
    private final List<ResourceLocation> hexAtlasLocations = new ArrayList<ResourceLocation>();
    private final List<BufferedImage> hexAtlasImages = new ArrayList<BufferedImage>();
    private final List<DynamicTexture> bitmapTextures = new ArrayList<DynamicTexture>();
    private BufferedImage hexAtlasImage;
    private int hexX, hexY, hexPage;
    private boolean scaleAsciiWithUnicode;

    public ModernFontRenderer(GameSettings settings, TextureManager textures) {
        super(settings, new ResourceLocation("textures/font/ascii.png"), textures, false);
        this.textures = textures;
        // Let 1.7.10 itself decode old resource-pack strings. Its internal
        // allowed-characters table is not identical to Java's IBM437 charset.
        this.legacyRenderer = new FontRenderer(settings, LEGACY_ASCII, textures, false);
    }

    @Override
    public void onResourceManagerReload(IResourceManager manager) {
        glyphs.clear();
        spaces.clear();
        loading.clear();
        hexAtlases.clear();
        hexAtlasLocations.clear();
        hexAtlasImages.clear();
        bitmapTextures.clear();
        scaleAsciiWithUnicode = isRussianLanguage();
        hexX = hexY = hexPage = 0;
        newHexPage();
        try {
            loadFont(manager, DEFAULT);
            legacyRenderer.onResourceManagerReload(manager);
            legacyRenderer.setUnicodeFlag(false);
            legacyRenderer.setBidiFlag(false);
            uploadHexAtlases();
        } catch (Exception ignored) {
            glyphs.clear();
            spaces.clear();
        }
    }

    private void loadFont(IResourceManager manager, ResourceLocation jsonLocation) throws IOException {
        String key = jsonLocation.toString();
        if (!loading.add(key)) return;
        IResource resource = manager.getResource(jsonLocation);
        Reader reader = new InputStreamReader(resource.getInputStream(), UTF8);
        JsonObject root;
        try { root = new JsonParser().parse(reader).getAsJsonObject(); }
        finally { reader.close(); }
        JsonArray providers = root.getAsJsonArray("providers");
        if (providers == null) return;
        // Modern Minecraft gives providers near the start higher priority.
        for (int i = providers.size() - 1; i >= 0; i--) {
            JsonObject provider = providers.get(i).getAsJsonObject();
            if (!providerAllowed(provider)) continue;
            String type = provider.get("type").getAsString();
            if (type.endsWith("bitmap")) loadBitmap(manager, provider);
            else if (type.endsWith("space")) loadSpaces(provider);
            else if (type.endsWith("unihex")) loadUnihex(manager, provider);
            else if (type.endsWith("reference")) {
                String id = provider.has("id") ? provider.get("id").getAsString() : provider.get("font").getAsString();
                loadFont(manager, fontJson(id));
            }
        }
    }

    private void loadSpaces(JsonObject provider) {
        JsonObject advances = provider.getAsJsonObject("advances");
        if (advances == null) return;
        for (Map.Entry<String, JsonElement> entry : advances.entrySet()) {
            int[] cps = codePoints(entry.getKey());
            if (cps.length == 1) {
                float advance = entry.getValue().getAsFloat();
                if (scaleAsciiWithUnicode) advance *= LEGACY_UNICODE_SCALE;
                spaces.put(cps[0], advance);
            }
        }
    }

    private void loadBitmap(IResourceManager manager, JsonObject provider) throws IOException {
        ResourceLocation file = textureLocation(provider.get("file").getAsString());
        BufferedImage image = ImageIO.read(manager.getResource(file).getInputStream());
        JsonArray rows = provider.getAsJsonArray("chars");
        if (image == null || rows == null || rows.size() == 0) return;
        int columns = 0;
        for (JsonElement row : rows) columns = Math.max(columns, codePoints(row.getAsString()).length);
        if (columns == 0) return;
        int cellW = image.getWidth() / columns;
        int cellH = image.getHeight() / rows.size();
        DynamicTexture dynamicTexture = new DynamicTexture(image);
        bitmapTextures.add(dynamicTexture);
        ResourceLocation loadedTexture = textures.getDynamicTextureLocation(
                "mbo_font_" + file.getResourceDomain() + "_" + bitmapTextures.size(), dynamicTexture);
        float logicalHeight = provider.has("height") ? provider.get("height").getAsFloat() : 8F;
        float ascent = provider.has("ascent") ? provider.get("ascent").getAsFloat() : 7F;
        float scale = logicalHeight / cellH;
        for (int row = 0; row < rows.size(); row++) {
            int[] cps = codePoints(rows.get(row).getAsString());
            for (int col = 0; col < cps.length; col++) {
                int right = opaqueRight(image, col * cellW, row * cellH, cellW, cellH);
                if (right < 0) continue;
                Glyph glyph = new Glyph();
                // Do not ask 1.7.10's TextureManager to resolve a modern pack
                // resource later: it caches it as missing during reload. Upload
                // the image that the provider has already read instead.
                glyph.texture = loadedTexture;
                glyph.u0 = (col * cellW) / (float) image.getWidth();
                glyph.v0 = (row * cellH) / (float) image.getHeight();
                glyph.u1 = ((col + 1) * cellW) / (float) image.getWidth();
                glyph.v1 = ((row + 1) * cellH) / (float) image.getHeight();
                glyph.width = cellW * scale;
                glyph.height = logicalHeight;
                glyph.y = 7F - ascent;
                glyph.advance = Math.round((right + 1) * scale) + 1F;
                applyLegacyUnicodeMetrics(cps[col], glyph);
                glyphs.put(cps[col], glyph);
            }
        }
    }

    private void loadUnihex(IResourceManager manager, JsonObject provider) throws IOException {
        String value = provider.has("hex_file") ? provider.get("hex_file").getAsString() : provider.get("file").getAsString();
        ResourceLocation file = directLocation(value);
        ZipInputStream zip = new ZipInputStream(manager.getResource(file).getInputStream(), UTF8);
        ZipEntry entry;
        while ((entry = zip.getNextEntry()) != null) {
            if (!entry.isDirectory() && entry.getName().endsWith(".hex")) parseHex(new BufferedReader(new InputStreamReader(zip, UTF8)));
            zip.closeEntry();
        }
        zip.close();
    }

    private void parseHex(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            int colon = line.indexOf(':');
            if (colon < 1) continue;
            try {
                int cp = Integer.parseInt(line.substring(0, colon), 16);
                String bits = line.substring(colon + 1).trim();
                int width = bits.length() / 4;
                if (width <= 0 || width > 32) continue;
                addHexGlyph(cp, bits, width);
            } catch (NumberFormatException ignored) {}
        }
    }

    private void addHexGlyph(int cp, String data, int width) {
        if (hexX + CELL > ATLAS_SIZE) { hexX = 0; hexY += CELL; }
        if (hexY + CELL > ATLAS_SIZE) {
            hexPage++;
            hexX = hexY = 0;
            newHexPage();
        }
        int min = width, max = -1;
        int digits = width / 4;
        for (int y = 0; y < 16; y++) {
            long row;
            try { row = Long.parseLong(data.substring(y * digits, (y + 1) * digits), 16); }
            catch (RuntimeException ex) { return; }
            for (int x = 0; x < width; x++) if ((row & (1L << (width - 1 - x))) != 0) {
                hexAtlasImage.setRGB(hexX + x, hexY + y, 0xFFFFFFFF);
                min = Math.min(min, x); max = Math.max(max, x);
            }
        }
        if (max >= min) {
            Glyph glyph = new Glyph();
            glyph.atlas = true;
            glyph.atlasPage = hexPage;
            glyph.u0 = hexX / (float) ATLAS_SIZE; glyph.v0 = hexY / (float) ATLAS_SIZE;
            glyph.u1 = (hexX + width) / (float) ATLAS_SIZE; glyph.v1 = (hexY + 16) / (float) ATLAS_SIZE;
            glyph.width = width / 2F; glyph.height = 8F; glyph.advance = (max - min + 1) / 2F + 1F;
            applyLegacyUnicodeMetrics(cp, glyph);
            glyphs.put(cp, glyph);
        }
        hexX += CELL;
    }

    private void newHexPage() {
        hexAtlasImage = new BufferedImage(ATLAS_SIZE, ATLAS_SIZE, BufferedImage.TYPE_INT_ARGB);
        hexAtlasImages.add(hexAtlasImage);
    }

    private void uploadHexAtlases() {
        for (int i = 0; i < hexAtlasImages.size(); i++) {
            DynamicTexture atlas = new DynamicTexture(hexAtlasImages.get(i));
            hexAtlases.add(atlas);
            hexAtlasLocations.add(textures.getDynamicTextureLocation("mbo_modern_font_" + i, atlas));
        }
    }

    private void applyLegacyUnicodeMetrics(int codePoint, Glyph glyph) {
        if (codePoint <= 255 && !scaleAsciiWithUnicode) return;
        float oldHeight = glyph.height;
        glyph.width *= LEGACY_UNICODE_SCALE;
        glyph.height *= LEGACY_UNICODE_SCALE;
        glyph.advance *= LEGACY_UNICODE_SCALE;
        // Keep every scaled glyph on the common provider baseline.
        glyph.y += oldHeight - glyph.height;
    }

    private static boolean isRussianLanguage() {
        try {
            String code = net.minecraft.client.Minecraft.getMinecraft()
                    .getLanguageManager().getCurrentLanguage().getLanguageCode();
            return code != null && code.toLowerCase(Locale.ROOT).startsWith("ru_");
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    @Override
    public int drawString(String text, int x, int y, int color, boolean shadow) {
        if (text == null || ((glyphs.isEmpty() && spaces.isEmpty()) && !TooltipElements.containsTag(text))) return super.drawString(text, x, y, color, shadow);
        if (hasObfuscatedFormat(text) || isLegacyEncoded(text))
            return legacyRenderer.drawString(text, x, y, color, shadow);
        if (getBidiFlag()) text = bidiReorder(text);
        float end = 0;
        if (shadow) end = drawModern(text, x + 1, y + 1, shadowColor(color), true) - 1F;
        end = Math.max(end, drawModern(text, x, y, color, false));
        // Vanilla returns the final screen X, not the string width.
        return Math.round(x + end);
    }

    @Override
    public int drawStringWithShadow(String text, int x, int y, int color) {
        // FontRenderer 1.7.10 calls its private legacy renderer directly here,
        // bypassing an overridden drawString. Route shadowed GUI text (notably
        // main-menu splashes) through the modern glyph pipeline as well.
        return drawString(text, x, y, color, true);
    }

    /** Mirrors FontRenderer's private bidi pass, bypassed by our drawString override. */
    private static String bidiReorder(String text) {
        try {
            // These are the exact modes used by FontRenderer 1.7.10. In
            // particular, paragraph level 127 keeps inline section-sign
            // formatting codes attached to the following character.
            Bidi bidi = new Bidi(new ArabicShaping(8).shape(text), 127);
            bidi.setReorderingMode(0);
            return bidi.writeReordered(2);
        } catch (ArabicShapingException ignored) {
            return text;
        }
    }

    private static boolean hasObfuscatedFormat(String text) {
        return text.indexOf("\u00a7k") >= 0 || text.indexOf("\u00a7K") >= 0;
    }

    private static boolean isLegacyEncoded(String text) {
        int extendedLatin = 0;
        for (int offset = 0; offset < text.length();) {
            if (text.charAt(offset) == '\u00a7' && offset + 1 < text.length()) {
                offset += 2;
                continue;
            }
            int cp = text.codePointAt(offset);
            offset += Character.charCount(cp);
            if (cp >= 0x2500 && cp <= 0x259F) return true;
            if (cp >= 0x0400 && cp <= 0x052F) return false;
            if (cp >= 0x00A0 && cp <= 0x00FF) extendedLatin++;
        }
        return extendedLatin >= 2;
    }

    private float drawModern(String text, float x, float y, int initialColor, boolean shadow) {
        float start = x;
        int color = initialColor;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        for (int offset = 0; offset < text.length();) {
            char ch = text.charAt(offset);
            if (ch == '\u00a7' && offset + 1 < text.length()) {
                int idx = "0123456789abcdef".indexOf(Character.toLowerCase(text.charAt(offset + 1)));
                if (idx >= 0) color = colorCode(idx, initialColor, shadow);
                else if (Character.toLowerCase(text.charAt(offset + 1)) == 'r') color = initialColor;
                offset += 2; continue;
            }
            TooltipElements.Token token = TooltipElements.tokenAt(text, offset);
            if (token != null) {
                if (!shadow) x += TooltipElements.draw(token, x, y);
                else x += token.advance;
                offset = token.end;
                continue;
            }
            int cp = text.codePointAt(offset);
            offset += Character.charCount(cp);
            Float space = spaces.get(cp);
            if (space != null) { x += space; continue; }
            Glyph glyph = glyphs.get(cp);
            if (glyph == null) {
                String s = new String(Character.toChars(cp));
                super.drawString(s, Math.round(x), Math.round(y), color, false);
                x += super.getStringWidth(s);
                continue;
            }
            drawGlyph(glyph, x, y + glyph.y, color);
            x += glyph.advance;
        }
        GL11.glColor4f(1, 1, 1, 1);
        return x - start;
    }

    private void drawGlyph(Glyph g, float x, float y, int color) {
        ResourceLocation texture = g.atlas && g.atlasPage < hexAtlasLocations.size()
                ? hexAtlasLocations.get(g.atlasPage) : g.texture;
        if (texture == null) return;
        textures.bindTexture(texture);
        float a = ((color >>> 24) & 255) / 255F; if (a == 0) a = 1;
        GL11.glColor4f(((color >> 16) & 255) / 255F, ((color >> 8) & 255) / 255F, (color & 255) / 255F, a);
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.addVertexWithUV(x, y + g.height, 0, g.u0, g.v1);
        t.addVertexWithUV(x + g.width, y + g.height, 0, g.u1, g.v1);
        t.addVertexWithUV(x + g.width, y, 0, g.u1, g.v0);
        t.addVertexWithUV(x, y, 0, g.u0, g.v0);
        t.draw();
    }

    @Override
    public int getStringWidth(String text) {
        if (text == null || ((glyphs.isEmpty() && spaces.isEmpty()) && !TooltipElements.containsTag(text))) return super.getStringWidth(text);
        if (hasObfuscatedFormat(text) || isLegacyEncoded(text)) return legacyRenderer.getStringWidth(text);
        float width = 0;
        for (int offset = 0; offset < text.length();) {
            if (text.charAt(offset) == '\u00a7' && offset + 1 < text.length()) { offset += 2; continue; }
            TooltipElements.Token token = TooltipElements.tokenAt(text, offset);
            if (token != null) {
                width += token.advance;
                offset = token.end;
                continue;
            }
            int cp = text.codePointAt(offset); offset += Character.charCount(cp);
            Float space = spaces.get(cp); Glyph glyph = glyphs.get(cp);
            width += space != null ? space : glyph != null ? glyph.advance : super.getStringWidth(new String(Character.toChars(cp)));
        }
        return Math.round(width);
    }

    @Override
    public void drawSplitString(String text, int x, int y, int width, int color) {
        if (text == null || (glyphs.isEmpty() && spaces.isEmpty())) {
            super.drawSplitString(text, x, y, width, color);
            return;
        }
        List<String> lines = listFormattedStringToWidth(text, width);
        for (String line : lines) {
            drawString(line, x, y, color, false);
            y += FONT_HEIGHT;
        }
    }

    @Override
    public int splitStringWidth(String text, int width) {
        if (text == null || (glyphs.isEmpty() && spaces.isEmpty())) {
            return super.splitStringWidth(text, width);
        }
        return listFormattedStringToWidth(text, width).size() * FONT_HEIGHT;
    }

    @Override
    public List<String> listFormattedStringToWidth(String text, int width) {
        if (text == null || (glyphs.isEmpty() && spaces.isEmpty())) {
            return super.listFormattedStringToWidth(text, width);
        }

        List<String> result = new ArrayList<String>();
        String[] paragraphs = text.split("\\n", -1);
        String format = "";
        for (String paragraph : paragraphs) {
            wrapParagraph(paragraph, width, format, result);
            if (!result.isEmpty()) format = activeFormat(result.get(result.size() - 1));
        }
        return result;
    }

    private void wrapParagraph(String paragraph, int width, String inheritedFormat, List<String> result) {
        String remaining = inheritedFormat + paragraph;
        if (paragraph.length() == 0) {
            result.add(inheritedFormat);
            return;
        }

        while (visibleLength(remaining) > 0) {
            int end = fittingIndex(remaining, width);
            if (end >= remaining.length()) {
                result.add(remaining);
                return;
            }

            int breakAt = lastBreakableSpace(remaining, end);
            if (breakAt <= leadingFormatEnd(remaining)) breakAt = end;
            String line = remaining.substring(0, breakAt);
            result.add(line);

            int next = breakAt;
            while (next < remaining.length() && remaining.charAt(next) == ' ') next++;
            String continuation = activeFormat(line);
            remaining = continuation + remaining.substring(next);
        }
    }

    private int fittingIndex(String text, int maxWidth) {
        float width = 0F;
        int offset = 0;
        int lastVisibleEnd = leadingFormatEnd(text);
        while (offset < text.length()) {
            if (text.charAt(offset) == '\u00a7' && offset + 1 < text.length()) {
                offset += 2;
                lastVisibleEnd = offset;
                continue;
            }
            int cp = text.codePointAt(offset);
            int next = offset + Character.charCount(cp);
            float advance = advance(cp);
            if (width + advance > maxWidth && width > 0F) return lastVisibleEnd;
            width += advance;
            offset = next;
            lastVisibleEnd = offset;
            if (width > maxWidth) return offset;
        }
        return text.length();
    }

    private float advance(int cp) {
        Float space = spaces.get(cp);
        Glyph glyph = glyphs.get(cp);
        return space != null ? space : glyph != null ? glyph.advance
                : super.getStringWidth(new String(Character.toChars(cp)));
    }

    private static int lastBreakableSpace(String text, int before) {
        for (int i = Math.min(before, text.length()) - 1; i >= 0; i--) {
            if (text.charAt(i) == ' ') return i;
        }
        return -1;
    }

    private static int leadingFormatEnd(String text) {
        int i = 0;
        while (i + 1 < text.length() && text.charAt(i) == '\u00a7') i += 2;
        return i;
    }

    private static int visibleLength(String text) {
        int count = 0;
        for (int i = 0; i < text.length();) {
            if (text.charAt(i) == '\u00a7' && i + 1 < text.length()) i += 2;
            else { int cp = text.codePointAt(i); i += Character.charCount(cp); count++; }
        }
        return count;
    }

    private static String activeFormat(String text) {
        String color = "";
        StringBuilder styles = new StringBuilder();
        for (int i = 0; i + 1 < text.length(); i++) {
            if (text.charAt(i) != '\u00a7') continue;
            char code = Character.toLowerCase(text.charAt(++i));
            if ("0123456789abcdef".indexOf(code) >= 0) {
                color = "\u00a7" + String.valueOf(code);
                styles.setLength(0);
            } else if (code == 'r') {
                color = "";
                styles.setLength(0);
            } else if ("klmno".indexOf(code) >= 0 && styles.indexOf("\u00a7" + String.valueOf(code)) < 0) {
                styles.append('\u00a7').append(code);
            }
        }
        return color + styles.toString();
    }

    @Override
    public String trimStringToWidth(String text, int width) {
        return trimStringToWidth(text, width, false);
    }

    @Override
    public String trimStringToWidth(String text, int width, boolean reverse) {
        if (text == null || (glyphs.isEmpty() && spaces.isEmpty())) {
            return super.trimStringToWidth(text, width, reverse);
        }
        if (!reverse) return text.substring(0, fittingIndex(text, width));

        float used = 0F;
        int start = text.length();
        while (start > 0) {
            int cp = text.codePointBefore(start);
            int previous = start - Character.charCount(cp);
            if (previous > 0 && text.charAt(previous - 1) == '\u00a7') {
                start = previous - 1;
                continue;
            }
            float advance = advance(cp);
            if (used + advance > width) break;
            used += advance;
            start = previous;
        }
        return text.substring(start);
    }

    private static int shadowColor(int color) { return (color & 0xFC000000) | ((color & 0xFCFCFC) >> 2); }
    private static boolean providerAllowed(JsonObject provider) {
        JsonObject filter = provider.has("filter") ? provider.getAsJsonObject("filter") : null;
        // The Japanese Unihex variant is selected by a modern language option
        // which does not exist in 1.7.10. The regular Unihex provider is the
        // correct global fallback; loading both would let JP overwrite it.
        return filter == null || !filter.has("jp") || !filter.get("jp").getAsBoolean();
    }
    private static int colorCode(int index, int base, boolean shadow) {
        int j = (index >> 3 & 1) * 85;
        int r = (index >> 2 & 1) * 170 + j;
        int g = (index >> 1 & 1) * 170 + j;
        int b = (index & 1) * 170 + j;
        if (shadow) { r >>= 2; g >>= 2; b >>= 2; }
        return (base & 0xFF000000) | r << 16 | g << 8 | b;
    }
    private static int opaqueRight(BufferedImage image, int ox, int oy, int w, int h) {
        for (int x = w - 1; x >= 0; x--) for (int y = 0; y < h; y++)
            if ((image.getRGB(ox + x, oy + y) >>> 24) != 0) return x;
        return -1;
    }
    private static int[] codePoints(String s) {
        int[] out = new int[s.codePointCount(0, s.length())];
        for (int i = 0, o = 0; i < s.length(); o++) { out[o] = s.codePointAt(i); i += Character.charCount(out[o]); }
        return out;
    }
    private static ResourceLocation fontJson(String value) {
        ResourceLocation id = directLocation(value);
        return new ResourceLocation(id.getResourceDomain(), "font/" + id.getResourcePath() + ".json");
    }
    private static ResourceLocation textureLocation(String value) {
        ResourceLocation id = directLocation(value);
        return new ResourceLocation(id.getResourceDomain(), "textures/" + id.getResourcePath());
    }
    private static ResourceLocation directLocation(String value) { return new ResourceLocation(value); }

    private static final class Glyph {
        ResourceLocation texture;
        boolean atlas;
        int atlasPage;
        float u0, v0, u1, v1, width, height, y, advance;
    }
}
