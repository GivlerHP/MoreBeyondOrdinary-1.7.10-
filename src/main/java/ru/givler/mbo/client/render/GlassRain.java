package ru.givler.mbo.client.render;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.FMLLog;
import ru.givler.mbo.client.texture.WeatherAtlasFilm;

public final class GlassRain {
  private static final double OFFSET = 1.0 / 1024;
  private static final int[] DX = {0, 0, -1, 1};
  private static final int[] DZ = {-1, 1, 0, 0};
  private static final boolean ENABLED = !Boolean.getBoolean("mbo.disableGlassRain");
  private static final Set<ChunkCoordinates> SECTIONS = new HashSet<>();
  private static final ArrayDeque<ChunkCoordinates> REFRESH = new ArrayDeque<>();
  private static final int LEFT = 1, RIGHT = 2, TOP = 4, BOTTOM = 8;
  private static final int SNOW_STAGE_TICKS = 200;
  private static final int MAX_SNOW_STAGE = 3;
  private static final IIcon[] FROST = new IIcon[16], FOG = new IIcon[16];
  private static IIcon rain, rainTop, dust;
  private static final WeatherAtlasFilm[] frostFilm = new WeatherAtlasFilm[16];
  private static final WeatherAtlasFilm[] fogFilm = new WeatherAtlasFilm[16];
  private static WeatherAtlasFilm rainFilm, rainTopFilm, dustFilm;
  private static boolean atlasReady;
  private static World world;
  private int ticks, snowTicks;
  private float rainOpacity, nightLevel, fogLevel, dustLevel;

  public static boolean supports(Block block) {
    return ENABLED && (block == Blocks.glass || block == Blocks.stained_glass
        || block == Blocks.glass_pane || block == Blocks.stained_glass_pane);
  }

  public static boolean needsPass(Block block) {
    return supports(block);
  }

  @SubscribeEvent
  public void stitch(TextureStitchEvent.Pre event) {
    if (!ENABLED || event.map.getTextureType() != 0) return;
    atlasReady = false;
    rain = event.map.registerIcon("mbo:weather/rain_weather_slot");
    rainTop = event.map.registerIcon("mbo:weather/rain_top_weather_slot");
    dust = event.map.registerIcon("mbo:weather/dust");
    for (int mask = 0; mask < 16; mask++) {
      FROST[mask] = event.map.registerIcon("mbo:weather/frost/" + mask);
      FOG[mask] = event.map.registerIcon("mbo:weather/condensation/" + mask);
    }
  }

  @SubscribeEvent
  public void stitched(TextureStitchEvent.Post event) {
    if (!ENABLED || event.map.getTextureType() != 0 || rain == null) return;
    try {
      int mipmaps = WeatherAtlasFilm.mipmapLevels(event.map);
      rainFilm = loadRain("glass_rain", rain, mipmaps);
      rainTopFilm = loadRain("glass_rain_top", rainTop, mipmaps);
      int size = dust.getIconWidth();
      dustFilm = new WeatherAtlasFilm((TextureAtlasSprite) dust, size, mipmaps, new int[][] {
          new int[size * size], WeatherAtlasFilm.pixels("dust", size),
          WeatherAtlasFilm.pixels("dust_storm", size)
      });
      for (int mask = 0; mask < 16; mask++) {
        int width = FROST[mask].getIconWidth();
        int[][] frostFrames = new int[MAX_SNOW_STAGE + 2][];
        frostFrames[0] = WeatherAtlasFilm.pixels("frost/" + mask, width);
        frostFrames[1] = WeatherAtlasFilm.pixels("frost_night/" + mask, width);
        for (int stage = 1; stage <= MAX_SNOW_STAGE; stage++) {
          frostFrames[stage + 1] = WeatherAtlasFilm.pixels("snow/" + stage + "/" + mask, width);
        }
        frostFilm[mask] = new WeatherAtlasFilm((TextureAtlasSprite) FROST[mask], width, mipmaps, frostFrames);
        width = FOG[mask].getIconWidth();
        fogFilm[mask] = new WeatherAtlasFilm((TextureAtlasSprite) FOG[mask], width, mipmaps, new int[][] {
            new int[width * width], WeatherAtlasFilm.pixels("condensation/" + mask, width)
        });
      }
      // The atlas finished loading before the first client tick uploads a film.
      atlasReady = true;
    } catch (IOException failure) {
      FMLLog.warning("MBO could not load weather glass keyframes: %s", failure.getMessage());
    }
  }

  private static WeatherAtlasFilm loadRain(String name, IIcon slot, int mipmaps) throws IOException {
    BufferedImage strip = WeatherAtlasFilm.read(name);
    int size = strip.getWidth();
    if (size != slot.getIconWidth() || strip.getHeight() % size != 0) {
      throw new IOException("Rain strip and atlas slot have different frame sizes: " + name);
    }
    int[][] frames = new int[1 + strip.getHeight() / size][size * size];
    for (int frame = 1; frame < frames.length; frame++) {
      strip.getRGB(0, (frame - 1) * size, size, size, frames[frame], 0, size);
    }
    return new WeatherAtlasFilm((TextureAtlasSprite) slot, size, mipmaps, frames);
  }

  @SubscribeEvent
  public void tick(TickEvent.ClientTickEvent event) {
    if (!ENABLED || event.phase != TickEvent.Phase.END) return;
    World current = Minecraft.getMinecraft().theWorld;
    if (world != current) {
      reset(current);
      snowTicks = 0;
      rainOpacity = nightLevel = fogLevel = dustLevel = 0;
    }
    if (world == null || world.provider.hasNoSky) return;
    boolean precipitating = world.getRainStrength(1) > 0.05F;
    boolean night = !world.isDaytime();
    boolean thunder = world.getWeightedThunderStrength(1) > 0.3F;
    rainOpacity = approach(rainOpacity, precipitating ? 1 : 0, precipitating ? 0.05F : 1F / 600);
    nightLevel = approach(nightLevel, night ? 1 : 0, 0.025F);
    fogLevel = approach(fogLevel, night || precipitating ? 1 : 0, 0.025F);
    dustLevel = approach(dustLevel, thunder ? 2 : precipitating ? 1 : 0,
        precipitating ? 0.005F : 2F / 3600);
    snowTicks = Math.max(0, Math.min(MAX_SNOW_STAGE * SNOW_STAGE_TICKS,
        snowTicks + (precipitating ? 1 : -1)));
    ++ticks;
    if (atlasReady && !SECTIONS.isEmpty() && (ticks & 1) == 0) {
      WeatherAtlasFilm.bind();
      updateFilms();
    }
    // Nearby roofs can change exposure without modifying the glass section itself.
    if (ticks % 100 == 0 && REFRESH.isEmpty()) {
      Iterator<ChunkCoordinates> iterator = SECTIONS.iterator();
      while (iterator.hasNext()) {
        ChunkCoordinates section = iterator.next();
        if (world.blockExists(section.posX, section.posY, section.posZ)) REFRESH.add(section);
        else iterator.remove();
      }
    }
    for (int budget = 0; budget < 8 && !REFRESH.isEmpty(); budget++) {
      ChunkCoordinates section = REFRESH.removeFirst();
      int x = section.posX, y = section.posY, z = section.posZ;
      if (world.blockExists(x, y, z)) {
        world.markBlockRangeForRenderUpdate(x, y, z, x + 15, y + 15, z + 15);
      }
    }
  }

  private static float approach(float current, float target, float step) {
    return current < target ? Math.min(target, current + step) : Math.max(target, current - step);
  }

  private void updateFilms() {
    rainFilm.update(0, 1 + (ticks / 3) % (rainFilm.frameCount() - 1), rainOpacity);
    rainTopFilm.update(0, 1 + (ticks / 3) % (rainTopFilm.frameCount() - 1), rainOpacity);
    int dustFrom = dustLevel >= 1 ? 1 : 0;
    dustFilm.update(dustFrom, dustFrom + 1, dustLevel - dustFrom);
    float snow = (float) snowTicks / SNOW_STAGE_TICKS;
    int lower = Math.min((int) snow, MAX_SNOW_STAGE - 1);
    for (int mask = 0; mask < 16; mask++) {
      if (lower == 0) frostFilm[mask].update(0, 2, snow, nightLevel);
      else frostFilm[mask].update(lower + 1, lower + 2, snow - lower);
      fogFilm[mask].update(0, 1, fogLevel);
    }
  }

  private static void reset(World current) {
    world = current;
    SECTIONS.clear();
    REFRESH.clear();
  }

  public static boolean render(RenderBlocks renderer, Block block, int x, int y, int z, int pass) {
    if (!supports(block)) return false;
    World current = Minecraft.getMinecraft().theWorld;
    if (current == null) return false;
    if (world != current) reset(current);
    SECTIONS.add(new ChunkCoordinates(x & ~15, y & ~15, z & ~15));
    if (pass != 1 || !atlasReady || renderer.hasOverrideBlockTexture() || current.provider.hasNoSky) return false;

    Tessellator tessellator = Tessellator.instance;
    if (block instanceof BlockPane) return renderPane(renderer, (BlockPane) block, x, y, z);
    boolean drawn = false;
    for (int face = 0; face < DX.length; face++) {
      int nx = x + DX[face], nz = z + DZ[face];
      if (!current.blockExists(nx, y, nz)
          || supports(renderer.blockAccess.getBlock(nx, y, nz))
          || !block.shouldSideBeRendered(renderer.blockAccess, nx, y, nz, face + 2)) continue;
      IIcon film = filmFor(renderer, block, x, y, z, face);
      if (film == null) continue;
      tessellator.setBrightness(block.getMixedBrightnessForBlock(renderer.blockAccess, nx, y, nz));
      tessellator.setColorOpaque_F(1, 1, 1);
      tessellator.setNormal(DX[face], 0, DZ[face]);
      quad(tessellator, x, y, z, face, film);
      drawn = true;
    }
    if (y < 255 && current.getPrecipitationHeight(x, z) <= y + 1
        && block.shouldSideBeRendered(renderer.blockAccess, x, y + 1, z, 1)) {
      BiomeGenBase biome = current.getBiomeGenForCoords(x, z);
      if (biome.canSpawnLightningBolt() && biome.getFloatTemperature(x, y, z) >= 0.15F) {
        tessellator.setBrightness(block.getMixedBrightnessForBlock(renderer.blockAccess, x, y + 1, z));
        tessellator.setColorOpaque_F(1, 1, 1);
        tessellator.setNormal(0, 1, 0);
        double top = y + 1 + OFFSET;
        IIcon film = rainTop;
        tessellator.addVertexWithUV(x, top, z, film.getMinU(), film.getMinV());
        tessellator.addVertexWithUV(x, top, z + 1, film.getMinU(), film.getMaxV());
        tessellator.addVertexWithUV(x + 1, top, z + 1, film.getMaxU(), film.getMaxV());
        tessellator.addVertexWithUV(x + 1, top, z, film.getMaxU(), film.getMinV());
        drawn = true;
      }
    }
    return drawn;
  }

  private static IIcon filmFor(RenderBlocks renderer, Block block, int x, int y, int z, int face) {
    int nx = x + DX[face], nz = z + DZ[face];
    boolean exposed = world.getPrecipitationHeight(nx, nz) <= y;
    BiomeGenBase biome = world.getBiomeGenForCoords(nx, nz);
    boolean cold = biome.getEnableSnow() || biome.getFloatTemperature(nx, y, nz) < 0.15F;
    if (cold) {
      if (exposed) return FROST[windowEdges(renderer, block, x, y, z, face)];
      int ox = x - DX[face], oz = z - DZ[face];
      if (world.blockExists(ox, y, oz) && world.getPrecipitationHeight(ox, oz) <= y) {
        return FOG[windowEdges(renderer, block, x, y, z, face)];
      }
    } else if (exposed) {
      return biome.canSpawnLightningBolt() ? rain : dust;
    }
    return null;
  }

  private static int windowEdges(RenderBlocks renderer, Block block, int x, int y, int z, int face) {
    boolean reverse = face == 0 || face == 3;
    int step = reverse ? 1 : -1;
    int dx = face < 2 ? step : 0;
    int dz = face < 2 ? 0 : step;
    int edges = 0;
    if (!sameWindow(renderer, block, x + dx, y, z + dz)) edges |= LEFT;
    if (!sameWindow(renderer, block, x - dx, y, z - dz)) edges |= RIGHT;
    if (!sameWindow(renderer, block, x, y + 1, z)) edges |= TOP;
    if (!sameWindow(renderer, block, x, y - 1, z)) edges |= BOTTOM;
    return edges;
  }

  private static boolean sameWindow(RenderBlocks renderer, Block block, int x, int y, int z) {
    Block neighbor = renderer.blockAccess.getBlock(x, y, z);
    return supports(neighbor) && (block instanceof BlockPane) == (neighbor instanceof BlockPane);
  }

  private static boolean renderPane(RenderBlocks renderer, BlockPane pane, int x, int y, int z) {
    boolean north = pane.canPaneConnectTo(renderer.blockAccess, x, y, z - 1, ForgeDirection.NORTH);
    boolean south = pane.canPaneConnectTo(renderer.blockAccess, x, y, z + 1, ForgeDirection.SOUTH);
    boolean west = pane.canPaneConnectTo(renderer.blockAccess, x - 1, y, z, ForgeDirection.WEST);
    boolean east = pane.canPaneConnectTo(renderer.blockAccess, x + 1, y, z, ForgeDirection.EAST);
    boolean ns = north || south, ew = west || east;
    double low = 7.0 / 16.0, high = 9.0 / 16.0;
    boolean drawn = false;
    for (int face = 0; face < 4; face++) {
      boolean end = face == 0 ? north : face == 1 ? south : face == 2 ? west : east;
      boolean across = face < 2 ? ew : ns;
      if (!across && end) continue;
      int nx = x + DX[face], nz = z + DZ[face];
      if (!world.blockExists(nx, y, nz)) continue;
      IIcon film = filmFor(renderer, pane, x, y, z, face);
      if (film == null) continue;
      Tessellator out = Tessellator.instance;
      out.setBrightness(pane.getMixedBrightnessForBlock(renderer.blockAccess, x, y, z));
      out.setColorOpaque_F(1, 1, 1);
      out.setNormal(DX[face], 0, DZ[face]);
      double start = face < 2 ? (west ? 0 : low) : (north ? 0 : low);
      double stop = face < 2 ? (east ? 1 : high) : (south ? 1 : high);
      double plane = (face & 1) == 0 ? low - OFFSET : high + OFFSET;
      if (end) {
        if (start < low) { emitFace(out, x, y, z, face, plane, start, low, film); drawn = true; }
        if (stop > high) { emitFace(out, x, y, z, face, plane, high, stop, film); drawn = true; }
      } else {
        emitFace(out, x, y, z, face, plane, start, stop, film);
        drawn = true;
      }
    }
    return drawn;
  }

  private static void quad(Tessellator out, int x, int y, int z, int face, IIcon film) {
    emitFace(out, x, y, z, face, (face & 1) == 0 ? -OFFSET : 1 + OFFSET, 0, 1, film);
  }

  private static void emitFace(Tessellator out, int x, int y, int z, int face,
                               double plane, double start, double stop, IIcon film) {
    boolean reverse = face == 0 || face == 3;
    double left = reverse ? stop : start;
    double right = reverse ? start : stop;
    double leftX = x + (face < 2 ? left : plane);
    double rightX = x + (face < 2 ? right : plane);
    double leftZ = z + (face < 2 ? plane : left);
    double rightZ = z + (face < 2 ? plane : right);
    double u0 = film.getInterpolatedU((reverse ? 1 - stop : start) * 16);
    double u1 = film.getInterpolatedU((reverse ? 1 - start : stop) * 16);
    out.addVertexWithUV(leftX, y + 1, leftZ, u0, film.getMinV());
    out.addVertexWithUV(leftX, y, leftZ, u0, film.getMaxV());
    out.addVertexWithUV(rightX, y, rightZ, u1, film.getMaxV());
    out.addVertexWithUV(rightX, y + 1, rightZ, u1, film.getMinV());
  }
}
