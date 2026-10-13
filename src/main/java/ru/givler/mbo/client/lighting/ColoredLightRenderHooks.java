package ru.givler.mbo.client.lighting;

import java.nio.ByteOrder;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.ChunkCache;
import ru.givler.mbo.config.ColoredLightConfig;
import ru.givler.mbo.lighting.ColoredLightVolume;

/** Only RenderBlocks world faces are tinted. Inventory, particles and GUI never inherit a tint. */
public final class ColoredLightRenderHooks {
  private static final boolean LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
  private static final int[] DX = {0, 0, 0, 0, -1, 1}, DY = {-1, 1, 0, 0, 0, 0}, DZ = {0, 0, -1, 1, 0, 0};
  private static final ThreadLocal<State> STATE = new ThreadLocal<State>() {
    @Override protected State initialValue() { return new State(); }
  };
  private ColoredLightRenderHooks() {}

  public static long begin(RenderBlocks renderer, double x, double y, double z, int face) {
    State state = STATE.get();
    int previous = state.depth;
    if (state.depth == state.frames.length) state.frames = java.util.Arrays.copyOf(state.frames, state.depth * 2);
    Frame frame = state.frames[state.depth];
    if (frame == null) state.frames[state.depth] = frame = new Frame();
    state.depth++;
    frame.x = (int) Math.floor(x); frame.y = (int) Math.floor(y); frame.z = (int) Math.floor(z); frame.face = face;
    frame.cached = false;
    frame.active = ColoredLightConfig.enabled && (ColoredLightClient.cacheActive || ColoredLightClient.volume != null)
        && renderer.blockAccess instanceof ChunkCache;
    return previous;
  }

  private static int sample(int x, int y, int z) {
    if (ColoredLightClient.cacheActive) return ru.givler.mbo.lighting.ColoredLightVolume.merge(ColoredLightClient.CACHE.sample(x, y, z), ColoredDynamicLights.INSTANCE.sample(x, y, z));
    ColoredLightVolume volume = ColoredLightClient.volume;
    return volume == null ? 0 : volume.sample(x, y, z);
  }

  public static void end(long previous) {
    STATE.get().depth = (int) previous;
  }

  public static int tint(int packedColor, int brightness) {
    State state = STATE.get();
    if (state.depth == 0) return packedColor;
    Frame frame = state.frames[state.depth - 1];
    return tintVertex(packedColor, brightness, frame.x + 0.5, frame.y + 0.5, frame.z + 0.5);
  }

  public static int tintVertex(int packedColor, int brightness, double x, double y, double z) {
    if (ColoredLightShaderBridge.active) return packedColor;
    if (!ColoredLightConfig.enabled || (!ColoredLightClient.cacheActive && ColoredLightClient.volume == null)) return packedColor;
    State state = STATE.get();
    if (state.depth == 0) return packedColor;
    Frame frame = state.frames[state.depth - 1];
    if (!frame.active) return packedColor;
    int rgb = vertexRGB(frame, x, y, z);
    if (rgb == 0) return packedColor;
    return tintColor(packedColor, rgb, ColoredLightConfig.strength * skyColorWeight(brightness, ColoredLightClient.skyDarkness), LITTLE_ENDIAN);
  }

  /** Night retains sky level 4 in vanilla; that ambient floor must not wash out RGB. */
  public static float skyColorWeight(int brightness, int darkness) {
    int sunlight = Math.max(0, (brightness >>> 20 & 15) - darkness - 4);
    return 1F - Math.min(11, sunlight) / 11F;
  }

  public static int brightnessVertex(int brightness, double x, double y, double z) {
    if (ColoredLightShaderBridge.active) return brightness;
    if (!ColoredLightConfig.enabled || (!ColoredLightClient.cacheActive && ColoredLightClient.volume == null)) return brightness;
    State state = STATE.get();
    if (state.depth == 0 || !state.frames[state.depth - 1].active) return brightness;
    int rgb = vertexRGB(state.frames[state.depth - 1], x, y, z);
    int max = Math.max(rgb >>> 16, Math.max(rgb >> 8 & 255, rgb & 255));
    int level = Math.min(240, Math.round(max * (240F / 255F) * ColoredLightConfig.brightnessGain));
    return (brightness & 0xFFFF0000) | Math.max(brightness & 0xFFFF, level);
  }

  private static int vertexRGB(Frame frame, double x, double y, double z) {
    // Crossed plants have no cube face. One cell lookup per plant also avoids random-offset seams.
    if (frame.face == 6) {
      if (!frame.cached) { frame.rgb = sample(frame.x,frame.y,frame.z); frame.cached = true; }
      return frame.rgb;
    }
    if (frame.cached && frame.vx == x && frame.vy == y && frame.vz == z) return frame.rgb;
    frame.vx = x; frame.vy = y; frame.vz = z; frame.cached = true;
    return frame.rgb = interpolateFace(frame, x, y, z);
  }

  private static int interpolateFace(Frame frame, double x, double y, double z) {
    double a = frame.face < 2 ? x : frame.face < 4 ? x : y;
    double b = frame.face < 2 ? z : frame.face < 4 ? y : z;
    int ia = (int) Math.floor(a - 0.5), ib = (int) Math.floor(b - 0.5);
    double fa = a - 0.5 - ia, fb = b - 0.5 - ib;
    int c00 = faceSample(frame, ia, ib), c10 = faceSample(frame, ia + 1, ib);
    int c01 = faceSample(frame, ia, ib + 1), c11 = faceSample(frame, ia + 1, ib + 1);
    double w00 = (1 - fa) * (1 - fb), w10 = fa * (1 - fb), w01 = (1 - fa) * fb, w11 = fa * fb;
    int rgb = 0;
    for (int shift = 0; shift <= 16; shift += 8) {
      int value = (int) Math.round((c00 >> shift & 255) * w00 + (c10 >> shift & 255) * w10
          + (c01 >> shift & 255) * w01 + (c11 >> shift & 255) * w11);
      rgb |= value << shift;
    }
    return rgb;
  }

  private static int faceSample(Frame frame, int a, int b) {
    if (frame.face < 2) return sample(a, frame.y + DY[frame.face], b);
    if (frame.face < 4) return sample(a, b, frame.z + DZ[frame.face]);
    return sample(frame.x + DX[frame.face], a, b);
  }

  /** Multiplies existing face/AO/biome color; preserves alpha and native byte order. */
  public static int tintColor(int packed, int light, float strength, boolean littleEndian) {
    int lr = light >>> 16 & 255, lg = light >>> 8 & 255, lb = light & 255;
    int max = Math.max(lr, Math.max(lg, lb));
    if (max == 0 || strength <= 0) return packed;
    float weight = Math.min(1, strength) * (float) Math.pow(max / 255F, ColoredLightConfig.colorFalloff);
    int rs = littleEndian ? 0 : 24, gs = littleEndian ? 8 : 16, bs = littleEndian ? 16 : 8;
    int r = Math.round((packed >>> rs & 255) * (1 - weight + weight * lr / max));
    int g = Math.round((packed >>> gs & 255) * (1 - weight + weight * lg / max));
    int b = Math.round((packed >>> bs & 255) * (1 - weight + weight * lb / max));
    int alphaMask = littleEndian ? 0xFF000000 : 0xFF;
    return packed & alphaMask | r << rs | g << gs | b << bs;
  }

  private static final class Frame { boolean active, cached; int x, y, z, face, rgb; double vx, vy, vz; }
  private static final class State { int depth; Frame[] frames = new Frame[4]; }
}
