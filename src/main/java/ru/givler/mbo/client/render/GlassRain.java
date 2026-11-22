package ru.givler.mbo.client.render;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.awt.image.BufferedImage;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.ChunkEvent;
import cpw.mods.fml.common.FMLLog;
import ru.givler.mbo.client.texture.WeatherAtlasFilm;
import ru.givler.mbo.registry.BlockRegistry;

public final class GlassRain {
  private static final double OFFSET = 1.0 / 1024;
  private static final int[] DX = {0, 0, -1, 1};
  private static final int[] DZ = {-1, 1, 0, 0};
  private static final boolean ENABLED = !Boolean.getBoolean("mbo.disableGlassRain");
  private static final int EXPOSURE_REACH = 4;
  private static final int PANE_EFFECT_DISTANCE = 48;
  private static final Map<ChunkCoordinates, Set<ChunkCoordinates>> ANGELICA_PANES =
      new ConcurrentHashMap<>();
  private static final Set<ChunkCoordinates> SECTIONS = ConcurrentHashMap.newKeySet();
  private static final Set<ChunkCoordinates> QUEUED = ConcurrentHashMap.newKeySet();
  private static final ConcurrentLinkedQueue<ChunkCoordinates> REFRESH = new ConcurrentLinkedQueue<>();
  private static final int LEFT = 1, RIGHT = 2, TOP = 4, BOTTOM = 8;
  private static final IIcon[] FROST = new IIcon[16], FOG = new IIcon[16];
  private static final int SNOW_PIXELS = 16;
  private static final int LEDGE_ROWS = 5;
  private static final int FILM_STEPS = 32;
  private static final IIcon[][] SNOWFLAKES = new IIcon[WeatherGlassSnowSurface.STRIPS][4];
  private static volatile boolean hasColdWindows;
  private final WeatherAtlasFilm[][] snowflakeFilms = new WeatherAtlasFilm[SNOWFLAKES.length][4];
  private static final int RAIN_TILE_SIZE = 16;
  private static final IIcon[] SIDE_RAIN = new IIcon[4];
  private static final WeatherAtlasFilm[] sideRainFilms = new WeatherAtlasFilm[SIDE_RAIN.length];
  private static IIcon rainTop, dust;
  private static final IIcon[] SNOW_LEDGES = new IIcon[16];
  private static final WeatherAtlasFilm[] snowLedgeFilms = new WeatherAtlasFilm[16];
  private static final WeatherAtlasFilm[] frostFilm = new WeatherAtlasFilm[16];
  private static final WeatherAtlasFilm[] fogFilm = new WeatherAtlasFilm[16];
  private static WeatherAtlasFilm rainTopFilm, dustFilm;
  private static boolean atlasReady;
  private static volatile World world;
  private World observedWorld;
  private final GlassRainWorldAccess observer = new GlassRainWorldAccess(this);
  private boolean refreshing;
  private int ticks;
  private float rainOpacity, frostLevel, fogLevel, dustLevel, snowLedgeLevel, snowflakeLevel;

  public static boolean supports(Block block) {
    return ENABLED && block != null && (block == Blocks.glass || block == Blocks.stained_glass
        || block == Blocks.glass_pane || block == Blocks.stained_glass_pane
        || block == BlockRegistry.FramelessGlass || block == BlockRegistry.FramelessStainedGlass
        || block == BlockRegistry.FramelessTintedGlass);
  }

  public static boolean needsPass(Block block) {
    return supports(block);
  }

  @SubscribeEvent
  public void stitch(TextureStitchEvent.Pre event) {
    if (!ENABLED || event.map.getTextureType() != 0) return;
    atlasReady = false;
    for (int strip = 0; strip < SNOWFLAKES.length; strip++) {
      for (int row = 0; row < 4; row++) {
        SNOWFLAKES[strip][row] = event.map.registerIcon("weatherglass:snow16_" + strip + "_" + row);
      }
    }
    for (int tile = 0; tile < SIDE_RAIN.length; tile++) {
      SIDE_RAIN[tile] = event.map.registerIcon("weatherglass:rain16_0_" + tile);
    }
    rainTop = event.map.registerIcon("mbo:weather/rain_top_weather_slot");
    dust = event.map.registerIcon("mbo:weather/dust");
    for (int mask = 0; mask < 16; mask++) {
      FROST[mask] = event.map.registerIcon("mbo:weather/frost/" + mask);
      if ((mask & BOTTOM) != 0) {
        SNOW_LEDGES[mask] = event.map.registerIcon("mbo:weather/snow/3/" + mask);
      }
      FOG[mask] = event.map.registerIcon("mbo:weather/condensation/" + mask);
    }
  }

  @SubscribeEvent
  public void stitched(TextureStitchEvent.Post event) {
    if (!ENABLED || event.map.getTextureType() != 0 || SIDE_RAIN[0] == null) return;
    try {
      verifyAtlasSlots(event.map);
      int mipmaps = WeatherAtlasFilm.mipmapLevels(event.map);
      loadSnowflakeFilms(mipmaps);
      loadSideRain(mipmaps);
      rainTopFilm = loadRain("glass_rain_top", rainTop, mipmaps);
      int size = dust.getIconWidth();
      dustFilm = new WeatherAtlasFilm((TextureAtlasSprite) dust, size, mipmaps, new int[][] {
          new int[size * size], WeatherAtlasFilm.pixels("dust", size),
          WeatherAtlasFilm.pixels("dust_storm", size)
      });
      for (int mask = 0; mask < 16; mask++) {
        int width = FROST[mask].getIconWidth();
        frostFilm[mask] = new WeatherAtlasFilm((TextureAtlasSprite) FROST[mask], width, mipmaps,
            frostFrames(mask, width, false));
        if ((mask & BOTTOM) != 0) {
          int ledgeSize = SNOW_LEDGES[mask].getIconWidth();
          snowLedgeFilms[mask] = new WeatherAtlasFilm((TextureAtlasSprite) SNOW_LEDGES[mask],
              ledgeSize, mipmaps, frostFrames(mask, ledgeSize, true));
        }
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

  private static void verifyAtlasSlots(TextureMap atlas) throws IOException {
    verifyAtlasSlot(atlas, rainTop);
    verifyAtlasSlot(atlas, dust);
    for (IIcon icon : SIDE_RAIN) verifyAtlasSlot(atlas, icon);
    for (IIcon[] strip : SNOWFLAKES) {
      for (IIcon icon : strip) verifyAtlasSlot(atlas, icon);
    }
    for (int mask = 0; mask < FROST.length; mask++) {
      verifyAtlasSlot(atlas, FROST[mask]);
      verifyAtlasSlot(atlas, FOG[mask]);
      if ((mask & BOTTOM) != 0) verifyAtlasSlot(atlas, SNOW_LEDGES[mask]);
    }
  }

  private static void verifyAtlasSlot(TextureMap atlas, IIcon icon) throws IOException {
    TextureAtlasSprite missing = atlas.getAtlasSprite("missingno");
    if (icon == null || icon == missing || atlas.getAtlasSprite(icon.getIconName()) == missing) {
      throw new IOException("Missing atlas slot for " + (icon == null ? "weather texture" : icon.getIconName()));
    }
  }

  private static int[][] frostFrames(int edges, int width, boolean ledge) {
    int mask = 0;
    if ((edges & LEFT) != 0) mask |= WeatherGlassFrostPattern.LEFT;
    if ((edges & RIGHT) != 0) mask |= WeatherGlassFrostPattern.RIGHT;
    if ((edges & TOP) != 0) mask |= WeatherGlassFrostPattern.TOP;
    if ((edges & BOTTOM) != 0) mask |= WeatherGlassFrostPattern.BOTTOM;
    int[][] frames = new int[FILM_STEPS + 1][width * width];
    byte[] rgba = new byte[SNOW_PIXELS * SNOW_PIXELS * 4];
    int[] pattern = new int[SNOW_PIXELS * SNOW_PIXELS];
    for (int step = 0; step < frames.length; step++) {
      float amount = (float) step / FILM_STEPS;
      WeatherGlassFrostPattern.draw(rgba, SNOW_PIXELS, mask, ledge ? 0 : amount, ledge ? amount : 0);
      readTile(rgba, 0, pattern);
      copySnowPixels(pattern, frames[step], width);
    }
    return frames;
  }

  private void loadSnowflakeFilms(int mipmaps) {
    WeatherGlassSnowSurface simulation = new WeatherGlassSnowSurface(SNOW_PIXELS);
    // Take one settled pattern from the original simulation for each shared strip.
    for (int tick = 0; tick < 600; tick++) simulation.tick(1, 1);
    for (int strip = 0; strip < SNOWFLAKES.length; strip++) {
      byte[] rgba = simulation.pixels(strip);
      for (int row = 0; row < 4; row++) {
        TextureAtlasSprite sprite = (TextureAtlasSprite) SNOWFLAKES[strip][row];
        int width = sprite.getIconWidth();
        int[] pattern = new int[SNOW_PIXELS * SNOW_PIXELS];
        readTile(rgba, row, pattern);
        int[] pixels = new int[width * width];
        copySnowPixels(pattern, pixels, width);
        snowflakeFilms[strip][row] = new WeatherAtlasFilm(sprite, width, mipmaps,
            new int[][] {new int[pixels.length], pixels});
      }
    }
  }

  /** The atlas can upscale a slot for mipmaps. Keep the artwork on a 16-pixel grid. */
  private static void copySnowPixels(int[] pattern, int[] destination, int width) {
    if (width == SNOW_PIXELS) {
      System.arraycopy(pattern, 0, destination, 0, pattern.length);
      return;
    }
    for (int y = 0; y < width; y++) {
      for (int x = 0; x < width; x++) {
        destination[y * width + x] =
            pattern[(y * SNOW_PIXELS / width) * SNOW_PIXELS + x * SNOW_PIXELS / width];
      }
    }
  }

  private static void readTile(byte[] rgba, int row, int[] pixels) {
    for (int pixel = 0; pixel < pixels.length; pixel++) {
      int i = (row * pixels.length + pixel) * 4;
      int alpha = rgba[i + 3] & 255;
      pixels[pixel] = alpha == 0 ? 0 : alpha << 24 | (rgba[i] & 255) << 16
          | (rgba[i + 1] & 255) << 8 | (rgba[i + 2] & 255);
    }
  }

  private static void loadSideRain(int mipmaps) throws IOException {
    BufferedImage strip = WeatherAtlasFilm.read("glass_rain");
    int frameCount = strip.getHeight() / RAIN_TILE_SIZE;
    if (strip.getWidth() != RAIN_TILE_SIZE || frameCount < SIDE_RAIN.length
        || strip.getHeight() % RAIN_TILE_SIZE != 0) {
      throw new IOException("Invalid side rain texture strip");
    }
    int height = SIDE_RAIN.length * RAIN_TILE_SIZE;
    int[] curtain = new int[RAIN_TILE_SIZE * height];
    int[] artwork = new int[RAIN_TILE_SIZE * RAIN_TILE_SIZE];
    for (int tile = 0; tile < SIDE_RAIN.length; tile++) {
      // Use the established wet frames so dry transition frames cannot leave gaps in the curtain.
      int frame = frameCount - SIDE_RAIN.length + tile;
      strip.getRGB(0, frame * RAIN_TILE_SIZE, RAIN_TILE_SIZE, RAIN_TILE_SIZE,
          artwork, 0, RAIN_TILE_SIZE);
      int horizontalOffset = tile * 5;
      for (int row = 0; row < RAIN_TILE_SIZE; row++) {
        for (int x = 0; x < RAIN_TILE_SIZE; x++) {
          int sourceX = (x + horizontalOffset) & (RAIN_TILE_SIZE - 1);
          curtain[(tile * RAIN_TILE_SIZE + row) * RAIN_TILE_SIZE + x] =
              artwork[row * RAIN_TILE_SIZE + sourceX];
        }
      }
    }
    for (int tile = 0; tile < SIDE_RAIN.length; tile++) {
      int[][] frames = new int[height + 1][RAIN_TILE_SIZE * RAIN_TILE_SIZE];
      for (int shift = 0; shift < height; shift++) {
        for (int row = 0; row < RAIN_TILE_SIZE; row++) {
          int source = Math.floorMod(tile * RAIN_TILE_SIZE + row - shift, height);
          System.arraycopy(curtain, source * RAIN_TILE_SIZE, frames[shift + 1],
              row * RAIN_TILE_SIZE, RAIN_TILE_SIZE);
        }
      }
      sideRainFilms[tile] = new WeatherAtlasFilm((TextureAtlasSprite) SIDE_RAIN[tile],
          RAIN_TILE_SIZE, mipmaps, frames);
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
    if (observedWorld != current) {
      if (observedWorld != null) observedWorld.removeWorldAccess(observer);
      observedWorld = current;
      if (current != null) current.addWorldAccess(observer);
      reset(current);
      rainOpacity = fogLevel = dustLevel = 0;
      boolean wet = current != null && current.getRainStrength(1) > .05F;
      frostLevel = wet ? 1 : current != null && !current.isDaytime() ? .7F : .45F;
      snowLedgeLevel = wet ? 1 : 0;
      snowflakeLevel = 0;
    }
    if (world == null || world.provider.hasNoSky) return;
    boolean precipitating = world.getRainStrength(1) > 0.05F;
    boolean night = !world.isDaytime();
    rainOpacity = approach(rainOpacity, precipitating ? 1 : 0, precipitating ? .05F : 1F / 600);
    boolean thunder = world.getWeightedThunderStrength(1) > 0.3F;
    float frostTarget = precipitating ? 1 : night ? .7F : .45F;
    frostLevel = approach(frostLevel, frostTarget, frostLevel < frostTarget ? 1F / 1800 : 1F / 3000);
    fogLevel = approach(fogLevel, night || precipitating ? 1 : 0, 0.025F);
    dustLevel = approach(dustLevel, thunder ? 2 : precipitating ? 1 : 0,
        precipitating ? 0.005F : 2F / 3600);
    snowLedgeLevel = approach(snowLedgeLevel, precipitating ? 1 : 0,
        precipitating ? 1F / 1200 : 1F / 4800);
    snowflakeLevel = approach(snowflakeLevel, precipitating ? 1 : 0,
        precipitating ? 1F / 400 : 1F / 800);
    ++ticks;
    if (atlasReady && !SECTIONS.isEmpty() && (ticks & 1) == 0) {
      WeatherAtlasFilm.bind();
      updateFilms();
    }
    for (int budget = 0; budget < 8; budget++) {
      ChunkCoordinates section = REFRESH.poll();
      if (section == null) break;
      QUEUED.remove(section);
      int x = section.posX, y = section.posY, z = section.posZ;
      if (!SECTIONS.contains(section) || !world.blockExists(x, y, z)) continue;
      refreshing = true;
      try {
        world.markBlockRangeForRenderUpdate(x, y, z, x + 15, y + 15, z + 15);
      } finally {
        refreshing = false;
      }
    }
  }

  void changed(int x, int y, int z, int x2, int y2, int z2) {
    if (refreshing || world == null || world.provider.hasNoSky) return;
    int minX = (x - EXPOSURE_REACH) & ~15, maxX = (x2 + EXPOSURE_REACH) & ~15;
    int minZ = (z - EXPOSURE_REACH) & ~15, maxZ = (z2 + EXPOSURE_REACH) & ~15;
    int maxY = (y2 + 1) & ~15;
    if (minX == maxX && minZ == maxZ) {
      for (int sy = Math.min(maxY, 240); sy >= -64; sy -= 16) {
        ChunkCoordinates section = new ChunkCoordinates(minX, sy, minZ);
        if (SECTIONS.contains(section)) enqueue(section);
      }
      return;
    }
    for (ChunkCoordinates section : SECTIONS) {
      if (section.posX >= minX && section.posX <= maxX && section.posZ >= minZ
          && section.posZ <= maxZ && section.posY <= maxY) enqueue(section);
    }
  }

  @SubscribeEvent
  public void chunkLoaded(ChunkEvent.Load event) {
    if (event.world != world) return;
    int x = event.getChunk().xPosition << 4, z = event.getChunk().zPosition << 4;
    changed(x, 0, z, x + 15, 255, z + 15);
  }

  @SubscribeEvent
  public void chunkUnloaded(ChunkEvent.Unload event) {
    if (event.world != world) return;
    int x = event.getChunk().xPosition << 4, z = event.getChunk().zPosition << 4;
    for (ChunkCoordinates section : SECTIONS) {
      if (section.posX == x && section.posZ == z) {
        SECTIONS.remove(section);
        ANGELICA_PANES.remove(section);
      }
    }
    changed(x, 0, z, x + 15, 255, z + 15);
  }

  private static void enqueue(ChunkCoordinates section) {
    if (QUEUED.add(section)) REFRESH.add(section);
  }

  private static float approach(float current, float target, float step) {
    return current < target ? Math.min(target, current + step) : Math.max(target, current - step);
  }

  private void updateFilms() {
    for (WeatherAtlasFilm film : sideRainFilms) {
      film.update(0, 1 + (ticks / 3) % (film.frameCount() - 1), rainOpacity);
    }
    rainTopFilm.update(0, 1 + (ticks / 3) % (rainTopFilm.frameCount() - 1), rainOpacity);
    if (hasColdWindows) {
      for (WeatherAtlasFilm[] strip : snowflakeFilms) {
        for (WeatherAtlasFilm film : strip) film.update(0, 1, snowflakeLevel);
      }
    }
    int dustFrom = dustLevel >= 1 ? 1 : 0;
    dustFilm.update(dustFrom, dustFrom + 1, dustLevel - dustFrom);
    for (int mask = 0; mask < 16; mask++) {
      updateLevel(frostFilm[mask], frostLevel);
      if ((mask & BOTTOM) != 0) updateLevel(snowLedgeFilms[mask], snowLedgeLevel);
      fogFilm[mask].update(0, 1, fogLevel);
    }
  }

  private static void updateLevel(WeatherAtlasFilm film, float level) {
    float position = level * FILM_STEPS;
    int lower = Math.min(FILM_STEPS - 1, (int) position);
    film.update(lower, lower + 1, position - lower);
  }

  private static synchronized void reset(World current) {
    if (world == current) return;
    world = current;
    SECTIONS.clear();
    ANGELICA_PANES.clear();
    REFRESH.clear();
    QUEUED.clear();
    hasColdWindows = false;
  }

  static void trackAngelicaPane(Block block, int x, int y, int z) {
    if (!supports(block)) return;
    World current = Minecraft.getMinecraft().theWorld;
    if (current == null || current.provider.hasNoSky) return;
    if (world != current) reset(current);
    ChunkCoordinates section = new ChunkCoordinates(x & ~15, y & ~15, z & ~15);
    SECTIONS.add(section);
    ANGELICA_PANES.computeIfAbsent(section, unused -> ConcurrentHashMap.newKeySet())
        .add(new ChunkCoordinates(x, y, z));
  }

  @SubscribeEvent
  public void renderAngelicaPanes(RenderWorldLastEvent event) {
    Minecraft client = Minecraft.getMinecraft();
    EntityLivingBase camera = client.renderViewEntity;
    World current = world;
    if (!atlasReady || ANGELICA_PANES.isEmpty() || current == null || current != client.theWorld
        || camera == null || current.provider.hasNoSky) return;
    float partial = event.partialTicks;
    double cx = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partial;
    double cy = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partial;
    double cz = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partial;
    int radius = PANE_EFFECT_DISTANCE;
    java.util.List<ChunkCoordinates> nearby = new java.util.ArrayList<>();
    for (int x = ((int) Math.floor(cx - radius)) & ~15; x <= cx + radius; x += 16) {
      for (int z = ((int) Math.floor(cz - radius)) & ~15; z <= cz + radius; z += 16) {
        for (int y = ((int) Math.floor(cy - radius)) & ~15; y <= cy + radius; y += 16) {
          Set<ChunkCoordinates> panes = ANGELICA_PANES.get(new ChunkCoordinates(x, y, z));
          if (panes == null || !current.blockExists(x, y, z)) continue;
          for (ChunkCoordinates pos : panes) {
            Block block = current.getBlock(pos.posX, pos.posY, pos.posZ);
            if (!(block instanceof BlockPane) || !supports(block)) {
              panes.remove(pos);
            } else if (distanceSquared(pos, cx, cy, cz) <= radius * radius) {
              nearby.add(pos);
            }
          }
        }
      }
    }
    if (nearby.isEmpty()) return;
    nearby.sort(java.util.Comparator.comparingDouble(pos -> -distanceSquared(pos, cx, cy, cz)));
    RenderBlocks renderer = new RenderBlocks(current);
    Tessellator out = Tessellator.instance;
    GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_CURRENT_BIT);
    try {
      WeatherAtlasFilm.bind();
      client.entityRenderer.enableLightmap(partial);
      GL11.glEnable(GL11.GL_BLEND);
      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
      GL11.glEnable(GL11.GL_DEPTH_TEST);
      GL11.glEnable(GL11.GL_CULL_FACE);
      GL11.glDepthMask(false);
      out.startDrawingQuads();
      out.setTranslation(-cx, -cy, -cz);
      for (ChunkCoordinates pos : nearby) {
        int x = pos.posX, y = pos.posY, z = pos.posZ;
        BiomeGenBase climate = current.getBiomeGenForCoords(x, z);
        boolean cold = isColdWindow(climate, x, y, z);
        if (cold || (climate.canSpawnLightningBolt() ? rainOpacity : dustLevel) > 0) {
          renderPane(renderer, (BlockPane) current.getBlock(x, y, z), x, y, z, climate, cold);
        }
      }
      out.draw();
    } finally {
      out.setTranslation(0, 0, 0);
      client.entityRenderer.disableLightmap(partial);
      GL11.glPopAttrib();
    }
  }

  private static double distanceSquared(ChunkCoordinates pos, double x, double y, double z) {
    double dx = pos.posX + .5 - x, dy = pos.posY + .5 - y, dz = pos.posZ + .5 - z;
    return dx * dx + dy * dy + dz * dz;
  }

  public static boolean render(RenderBlocks renderer, Block block, int x, int y, int z, int pass) {
    if (!supports(block)) return false;
    World current = Minecraft.getMinecraft().theWorld;
    if (current == null) return false;
    if (world != current) reset(current);
    SECTIONS.add(new ChunkCoordinates(x & ~15, y & ~15, z & ~15));
    if (pass != 1 || !atlasReady || renderer.hasOverrideBlockTexture() || current.provider.hasNoSky) return false;

    Tessellator tessellator = Tessellator.instance;
    BiomeGenBase climate = current.getBiomeGenForCoords(x, z);
    boolean cold = isColdWindow(climate, x, y, z);
    if (block instanceof BlockPane) return renderPane(renderer, (BlockPane) block, x, y, z, climate, cold);
    boolean drawn = false;
    for (int face = 0; face < DX.length; face++) {
      int nx = x + DX[face], nz = z + DZ[face];
      if (!current.blockExists(nx, y, nz)
          || supports(renderer.blockAccess.getBlock(nx, y, nz))
          || !block.shouldSideBeRendered(renderer.blockAccess, nx, y, nz, face + 2)) continue;
      IIcon film = filmFor(renderer, block, x, y, z, face, climate, cold);
      if (film == null) continue;
      tessellator.setBrightness(block.getMixedBrightnessForBlock(renderer.blockAccess, nx, y, nz));
      tessellator.setColorOpaque_F(1, 1, 1);
      emitWeather(tessellator, renderer, block, x, y, z, face,
          (face & 1) == 0 ? -OFFSET : 1 + OFFSET, 0, 1, film);
      drawn = true;
    }
    if (y < 255 && current.getPrecipitationHeight(x, z) <= y + 1
        && block.shouldSideBeRendered(renderer.blockAccess, x, y + 1, z, 1)) {
      if (!cold && climate.canSpawnLightningBolt()) {
        tessellator.setBrightness(block.getMixedBrightnessForBlock(renderer.blockAccess, x, y + 1, z));
        tessellator.setColorOpaque_F(1, 1, 1);
        tessellator.setNormal(0, 1, 0);
        emitTopRain(tessellator, x, y, z);
        drawn = true;
      }
    }
    return drawn;
  }

  private static void emitTopRain(Tessellator out, int x, int y, int z) {
    double top = y + 1 + OFFSET;
    for (int side = 0; side < 2; side++) {
      out.setNormal(0, side == 0 ? 1 : -1, 0);
      for (int point = 0; point < 4; point++) {
        int corner = side == 0 ? point : 3 - point;
        int u = corner >= 2 ? 1 : 0, v = corner == 1 || corner == 2 ? 1 : 0;
        out.addVertexWithUV(x + u, top, z + v,
            rainTop.getInterpolatedU(u * 16), rainTop.getInterpolatedV(v * 16));
      }
    }
  }

  private static IIcon filmFor(RenderBlocks renderer, Block block, int x, int y, int z, int face,
                              BiomeGenBase climate, boolean cold) {
    int nx = x + DX[face], nz = z + DZ[face];
    if (exposed(nx, y, nz, DX[face], DZ[face])) {
      if (cold) return FROST[windowEdges(renderer, block, x, y, z, face)];
      int plane = face < 2 ? z : x;
      int column = face < 2 ? x : z;
      int phase = (plane * 31 + column * 17 + face) & (SIDE_RAIN.length - 1);
      return climate.canSpawnLightningBolt() ? SIDE_RAIN[Math.floorMod(phase - y, SIDE_RAIN.length)] : dust;
    }

    int ox = x - DX[face], oz = z - DZ[face];
    if (cold && exposed(ox, y, oz, -DX[face], -DZ[face])) {
      return FOG[windowEdges(renderer, block, x, y, z, face)];
    }
    return null;
  }

  private static boolean isColdWindow(BiomeGenBase climate, int x, int y, int z) {
    if (isCold(climate, x, y, z)) return true;
    // At biome borders, use the exposed snow side for the whole block, including its top.
    for (int face = 0; face < DX.length; face++) {
      int nx = x + DX[face], nz = z + DZ[face];
      if (world.blockExists(nx, y, nz) && isCold(world.getBiomeGenForCoords(nx, nz), nx, y, nz)
          && exposed(nx, y, nz, DX[face], DZ[face])) return true;
    }
    return false;
  }

  private static boolean isCold(BiomeGenBase biome, int x, int y, int z) {
    return biome.getEnableSnow() || biome.getFloatTemperature(x, y, z) < 0.15F;
  }

  private static boolean exposed(int x, int y, int z, int dx, int dz) {
    // Test the adjacent column first, then a short outward path for slanted rain.
    for (int step = 0; step < EXPOSURE_REACH; step++) {
      int sx = x + step * dx, sz = z + step * dz, sy = y + step;
      if (sy >= world.getHeight() || !world.blockExists(sx, sy, sz)) return false;
      if (world.getBlock(sx, sy, sz).getMaterial().blocksMovement()) return false;
      if (world.getPrecipitationHeight(sx, sz) <= sy) return true;
      if (step + 1 < EXPOSURE_REACH && world.getBlock(sx, sy + 1, sz).getMaterial().blocksMovement()) return false;
    }
    return false;
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

  private static boolean renderPane(RenderBlocks renderer, BlockPane pane, int x, int y, int z,
                                    BiomeGenBase climate, boolean cold) {
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
      IIcon film = filmFor(renderer, pane, x, y, z, face, climate, cold);
      if (film == null) continue;
      Tessellator out = Tessellator.instance;
      out.setBrightness(pane.getMixedBrightnessForBlock(renderer.blockAccess, x, y, z));
      out.setColorOpaque_F(1, 1, 1);
      double start = face < 2 ? (west ? 0 : low) : (north ? 0 : low);
      double stop = face < 2 ? (east ? 1 : high) : (south ? 1 : high);
      double plane = (face & 1) == 0 ? low - OFFSET : high + OFFSET;
      if (end) {
        if (start < low) { emitWeather(out, renderer, pane, x, y, z, face, plane, start, low, film); drawn = true; }
        if (stop > high) { emitWeather(out, renderer, pane, x, y, z, face, plane, high, stop, film); drawn = true; }
      } else {
        emitWeather(out, renderer, pane, x, y, z, face, plane, start, stop, film);
        drawn = true;
      }
    }
    return drawn;
  }

  private static void emitWeather(Tessellator out, RenderBlocks renderer, Block block,
                                  int x, int y, int z, int face, double plane,
                                  double start, double stop, IIcon film) {
    emitFace(out, x, y, z, face, plane, start, stop, film, 1);
    if (film == dust) return;
    int edges = windowEdges(renderer, block, x, y, z, face);
    if (film != FROST[edges]) return;

    hasColdWindows = true;
    double outward = (face & 1) == 0 ? -OFFSET : OFFSET;
    int strip = (face < 2 ? z : x) * 31 + face;
    strip = (strip ^ (strip >>> 16)) & (SNOWFLAKES.length - 1);
    emitFace(out, x, y, z, face, plane + outward, start, stop, SNOWFLAKES[strip][3 - (y & 3)], 1);
    if ((edges & BOTTOM) != 0) {
      IIcon snowLedge = SNOW_LEDGES[edges];
      double height = (double) LEDGE_ROWS / SNOW_PIXELS;
      emitFace(out, x, y, z, face, plane + outward * 2, start, stop, snowLedge, height);
    }
  }

  private static void emitFace(Tessellator out, int x, int y, int z, int face,
                               double plane, double start, double stop, IIcon film, double height) {
    boolean reverse = face == 0 || face == 3;
    double left = reverse ? stop : start;
    double right = reverse ? start : stop;
    double leftX = x + (face < 2 ? left : plane);
    double rightX = x + (face < 2 ? right : plane);
    double leftZ = z + (face < 2 ? plane : left);
    double rightZ = z + (face < 2 ? plane : right);
    double u0 = film.getInterpolatedU((reverse ? 1 - stop : start) * 16);
    double u1 = film.getInterpolatedU((reverse ? 1 - start : stop) * 16);
    double topV = film.getInterpolatedV(16 * (1 - height));
    out.setNormal(DX[face], 0, DZ[face]);
    out.addVertexWithUV(leftX, y + height, leftZ, u0, topV);
    out.addVertexWithUV(leftX, y, leftZ, u0, film.getMaxV());
    out.addVertexWithUV(rightX, y, rightZ, u1, film.getMaxV());
    out.addVertexWithUV(rightX, y + height, rightZ, u1, topV);

    // Keep the film on its original side of the window, visible through the glass.
    // Reverse winding and normal, retaining UVs at the same physical points.
    out.setNormal(-DX[face], 0, -DZ[face]);
    out.addVertexWithUV(rightX, y + height, rightZ, u1, topV);
    out.addVertexWithUV(rightX, y, rightZ, u1, film.getMaxV());
    out.addVertexWithUV(leftX, y, leftZ, u0, film.getMaxV());
    out.addVertexWithUV(leftX, y + height, leftZ, u0, topV);
  }
}
