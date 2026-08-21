package ru.givler.mbo.client.particle;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import ru.givler.mbo.MoreBeyondOrdinary;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/** Client-side equivalent of modern LeavesBlock.randomDisplayTick. */
public final class FallingLeavesHandler {
    private static final int FRAMES = 12;
    private static final int SEARCH_ATTEMPTS = 96;
    private final IIcon[] tintedIcons = new IIcon[FRAMES];
    private final IIcon[] cherryIcons = new IIcon[FRAMES];
    private final IIcon[] paleOakIcons = new IIcon[FRAMES];
    private final Random random = new Random();
    private final Map<String, Integer> textureColors = new HashMap<String, Integer>();
    private Block bopLeaves3;
    private boolean lookedUpBopLeaves;

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() != 0) return;
        textureColors.clear();
        TextureMap map = event.map;
        for (int i = 0; i < FRAMES; i++) {
            tintedIcons[i] = map.registerIcon(MoreBeyondOrdinary.MODID + ":particles/leaf_" + i);
            cherryIcons[i] = map.registerIcon(MoreBeyondOrdinary.MODID + ":particles/cherry_" + i);
            paleOakIcons[i] = map.registerIcon(MoreBeyondOrdinary.MODID + ":particles/pale_oak_" + i);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.theWorld;
        EntityPlayer player = mc.thePlayer;
        if (world == null || player == null || mc.effectRenderer == null) return;
        if (mc.gameSettings.particleSetting >= 2) return;

        int attempts = mc.gameSettings.particleSetting == 1 ? SEARCH_ATTEMPTS / 3 : SEARCH_ATTEMPTS;
        int px = (int)Math.floor(player.posX);
        int py = (int)Math.floor(player.posY);
        int pz = (int)Math.floor(player.posZ);
        for (int i = 0; i < attempts; i++) {
            int x = px + random.nextInt(33) - 16;
            int y = py + random.nextInt(25) - 8;
            int z = pz + random.nextInt(33) - 16;
            Block leaves = world.getBlock(x, y, z);
            if (!leaves.isLeaves(world, x, y, z) || random.nextInt(10) != 0) continue;

            Block below = world.getBlock(x, y - 1, z);
            if (below.isOpaqueCube() || below.getMaterial().isLiquid()) continue;
            int metadata = world.getBlockMetadata(x, y, z);
            IIcon[] set = tintedIcons;
            int color = leaves.colorMultiplier(world, x, y, z);
            if (isBopCherry(leaves, metadata, 1)) {
                set = cherryIcons;
                color = 0xFFFFFF;
            } else if (isBopCherry(leaves, metadata, 3)) {
                set = paleOakIcons;
                color = 0xFFFFFF;
            } else if (isUntintedBopLeaves(leaves)) {
                color = getTextureColor(leaves.getIcon(1, metadata), color);
            }
            IIcon icon = set[random.nextInt(FRAMES)];
            if (icon == null) continue;
            double sx = x + random.nextDouble();
            double sy = y - 0.05D;
            double sz = z + random.nextDouble();
            mc.effectRenderer.addEffect(new ParticleFallingLeaf(world, sx, sy, sz, icon, color));
        }
    }

    /**
     * BOP's BlockBOPColorizedLeaves uses the biome foliage colour. Its other
     * leaf classes use already-coloured textures and normally return a white
     * colour multiplier, so those colours have to be read from the sprite.
     */
    private boolean isUntintedBopLeaves(Block block) {
        String className = block.getClass().getName();
        return className.startsWith("biomesoplenty.")
                && className.indexOf("BlockBOPColorizedLeaves") < 0;
    }

    private int getTextureColor(IIcon icon, int fallback) {
        if (!(icon instanceof TextureAtlasSprite)) return fallback;

        TextureAtlasSprite sprite = (TextureAtlasSprite)icon;
        String name = sprite.getIconName();
        Integer cached = textureColors.get(name);
        if (cached != null) return cached.intValue();

        int color = readTextureColor(name, fallback);
        textureColors.put(name, Integer.valueOf(color));
        return color;
    }

    private int readTextureColor(String iconName, int fallback) {
        ResourceLocation icon = new ResourceLocation(iconName);
        ResourceLocation texture = new ResourceLocation(icon.getResourceDomain(),
                "textures/blocks/" + icon.getResourcePath() + ".png");
        InputStream stream = null;
        try {
            stream = Minecraft.getMinecraft().getResourceManager().getResource(texture).getInputStream();
            BufferedImage image = ImageIO.read(stream);
            if (image != null) return averageColor(image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth()), fallback);
        } catch (Exception ignored) {
            // Some generated/third-party sprites have no standalone PNG.
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception ignored) {
                }
            }
        }

        // Kept as a fallback for animated sprites whose frame data is retained.
        TextureAtlasSprite sprite = (TextureAtlasSprite)Minecraft.getMinecraft()
                .getTextureMapBlocks().getAtlasSprite(iconName);
        int[][] frame = sprite.getFrameCount() > 0 ? sprite.getFrameTextureData(0) : null;
        if (frame == null || frame.length == 0 || frame[0] == null) return fallback;

        return averageColor(frame[0], fallback);
    }

    private int averageColor(int[] pixels, int fallback) {
        long red = 0L;
        long green = 0L;
        long blue = 0L;
        long weight = 0L;
        for (int pixel : pixels) {
            int alpha = pixel >>> 24;
            if (alpha < 16) continue;
            red += ((pixel >>> 16) & 255) * (long)alpha;
            green += ((pixel >>> 8) & 255) * (long)alpha;
            blue += (pixel & 255) * (long)alpha;
            weight += alpha;
        }

        if (weight > 0L) {
            return ((int)(red / weight) << 16)
                    | ((int)(green / weight) << 8)
                    | (int)(blue / weight);
        }
        return fallback;
    }

    private boolean isBopCherry(Block block, int metadata, int cherryMetadata) {
        if (!lookedUpBopLeaves) {
            lookedUpBopLeaves = true;
            bopLeaves3 = GameRegistry.findBlock("BiomesOPlenty", "leaves3");
        }
        return block == bopLeaves3 && (metadata & 3) == cherryMetadata;
    }
}
