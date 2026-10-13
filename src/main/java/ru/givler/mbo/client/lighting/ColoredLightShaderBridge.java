package ru.givler.mbo.client.lighting;

import java.nio.ByteBuffer;
import java.util.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import ru.givler.mbo.config.ColoredLightConfig;
import ru.givler.mbo.lighting.ColoredLightApi;

/** RGB field + independent moving sources. Texture uploads happen only on field/origin changes. */
public final class ColoredLightShaderBridge {
  private static final int SIZE = 64, UNIT = 15;
  private static ByteBuffer pixels;
  private static final Map<Integer, Uniforms> uniforms = new HashMap<>();
  private static int texture, ox = Integer.MIN_VALUE, oy, oz;
  private static long version = -1;
  private static String pack = "";
  public static volatile boolean active;
  private static float partialTicks;
  private static java.lang.reflect.Method packName;
  private static boolean resolved;
  private static boolean reload;
  private ColoredLightShaderBridge() {}

  public static void renderStart(float partial) { partialTicks = partial; }
  public static void tick(Minecraft mc) {
    try {
      if (!resolved) {
        resolved = true;
        try { packName = Class.forName("shadersmod.client.Shaders").getMethod("getShaderPackName"); } catch (ReflectiveOperationException | LinkageError ignored) {}
      }
      String name = packName == null ? "" : String.valueOf(packName.invoke(null));
      if (!name.equals(pack)) {
        boolean previous = active; pack = name; active = false; uniforms.clear(); version = -1;
        if (previous) reload = true;
      }
    } catch (ReflectiveOperationException | LinkageError ignored) {}
    if (reload && mc.renderGlobal != null) { reload = false; mc.renderGlobal.loadRenderers(); }
  }

  public static void resetPrograms() {
    uniforms.clear(); version = -1; if (active) reload = true; active = false;
    if (texture != 0) { GL11.glDeleteTextures(texture); texture = 0; }
    pixels = null;
  }
  public static void release() { resetPrograms(); reload = false; ox = Integer.MIN_VALUE; }
  public static boolean retainsField() { return texture != 0 || pixels != null; }

  public static void useProgram(double cameraX, double cameraY, double cameraZ) {
    if (!GLContext.getCapabilities().OpenGL20 || !GLContext.getCapabilities().OpenGL12) return;
    int program = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
    if (program == 0) return;
    if (!ColoredLightConfig.enabled) {
      // Keep an already selected MBO shaderpack neutral without retaining RGB resources.
      int sampler = GL20.glGetUniformLocation(program,"mboLightVolume");
      if (sampler >= 0) {
        GL20.glUniform1i(sampler,UNIT);
        GL20.glUniform1f(GL20.glGetUniformLocation(program,"mboLightSize"),0);
        GL20.glUniform1i(GL20.glGetUniformLocation(program,"mboLightCount"),0);
      }
      return;
    }
    Uniforms u = uniforms.get(program);
    if (u == null) { u = new Uniforms(program); uniforms.put(program, u); }
    if (u.volume < 0) return;
    if (GL11.glGetInteger(GL20.GL_MAX_TEXTURE_IMAGE_UNITS) <= UNIT) return;
    GL20.glUniform1i(u.volume, UNIT); GL20.glUniform1i(u.count, 0);
    Minecraft mc = Minecraft.getMinecraft();
    if (mc.theWorld == null) return;
    if (!active) {
      active = true;
      // Previously baked tint would otherwise be applied twice by an RGB-aware shader.
      reload = true;
    }
    int x = ((int) Math.floor(cameraX) >> 4) * 16 - 24;
    int y = ((int) Math.floor(cameraY) >> 4) * 16 - 24;
    int z = ((int) Math.floor(cameraZ) >> 4) * 16 - 24;
    int oldActive = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
    GL13.glActiveTexture(GL13.GL_TEXTURE0 + UNIT);
    try {
      if (texture == 0) {
        texture = GL11.glGenTextures(); GL11.glBindTexture(GL12.GL_TEXTURE_3D, texture);
        GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        for (int wrap : new int[]{GL11.GL_TEXTURE_WRAP_S, GL11.GL_TEXTURE_WRAP_T, GL12.GL_TEXTURE_WRAP_R}) GL11.glTexParameteri(GL12.GL_TEXTURE_3D, wrap, GL12.GL_CLAMP_TO_EDGE);
        GL12.glTexImage3D(GL12.GL_TEXTURE_3D, 0, GL11.GL_RGBA8, SIZE, SIZE, SIZE, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
      }
      GL11.glBindTexture(GL12.GL_TEXTURE_3D, texture);
      long current = ColoredLightClient.CACHE.version() * 31 + ColoredLightClient.INSTANCE.geometryVersion();
      if (current != version || x != ox || y != oy || z != oz) {
        long diagnosticUpload=ColoredLightDiagnostics.start();
        long allocationUpload=ColoredLightAllocationDiagnostics.start();
        if (pixels == null) pixels = BufferUtils.createByteBuffer(SIZE * SIZE * SIZE * 4);
        pixels.clear();
        for (int bz = 0; bz < SIZE; bz++) for (int by = 0; by < SIZE; by++) for (int bx = 0; bx < SIZE; bx++) {
          int rgb = ColoredLightConfig.enabled ? ColoredLightClient.CACHE.sample(x + bx, y + by, z + bz) : 0;
          int opacity = ColoredLightClient.INSTANCE.opacity(x + bx, y + by, z + bz);
          pixels.put((byte) (rgb >> 16)).put((byte) (rgb >> 8)).put((byte) rgb).put((byte) (opacity * 17));
        }
        pixels.flip(); GL12.glTexSubImage3D(GL12.GL_TEXTURE_3D, 0, 0, 0, 0, SIZE, SIZE, SIZE, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
        ColoredLightDiagnostics.upload(diagnosticUpload);
        ColoredLightAllocationDiagnostics.end(ColoredLightAllocationDiagnostics.UPLOAD,allocationUpload);
        ColoredLightAllocationDiagnostics.work(ColoredLightAllocationDiagnostics.UPLOAD,SIZE*SIZE*SIZE*4);
        version = current; ox = x; oy = y; oz = z;
      }
      GL20.glUniform1i(u.volume, UNIT);
      GL20.glUniform3f(u.origin, (float) (ox - cameraX), (float) (oy - cameraY), (float) (oz - cameraZ));
      GL20.glUniform1f(u.size, SIZE); GL20.glUniform1f(u.gain, ColoredLightConfig.brightnessGain);
      GL20.glUniform1f(u.strength, ColoredLightConfig.strength); GL20.glUniform1f(u.falloff, ColoredLightConfig.colorFalloff);
      GL20.glUniform1i(u.entities,ColoredLightConfig.entityLighting?1:0);
      List<ColoredDynamicLights.Source> sources = ColoredLightConfig.enabled ? ColoredDynamicLights.INSTANCE.sources() : Collections.emptyList();
      int count = Math.min(8, sources.size()); GL20.glUniform1i(u.count, count);
      for (int i = 0; i < count; i++) {
        ColoredDynamicLights.Source source = sources.get(i); int rgb = ColoredLightApi.rgb(source.packed);
        GL20.glUniform4f(u.positions[i], (float) (source.x(partialTicks) - cameraX), (float) (source.y(partialTicks) - cameraY), (float) (source.z(partialTicks) - cameraZ), (float) source.radius);
        GL20.glUniform3f(u.colors[i], (rgb >>> 16) / 255F, (rgb >> 8 & 255) / 255F, (rgb & 255) / 255F);
      }
    } finally { GL13.glActiveTexture(oldActive); }
  }
  private static final class Uniforms {
    final int volume, origin, size, count, gain, strength, falloff, entities;
    final int[] positions = new int[8], colors = new int[8];
    Uniforms(int program) {
      volume = GL20.glGetUniformLocation(program, "mboLightVolume"); origin = GL20.glGetUniformLocation(program, "mboLightOrigin"); size = GL20.glGetUniformLocation(program, "mboLightSize");
      count = GL20.glGetUniformLocation(program, "mboLightCount"); gain = GL20.glGetUniformLocation(program, "mboLightGain");
      strength = GL20.glGetUniformLocation(program, "mboLightStrength"); falloff = GL20.glGetUniformLocation(program, "mboColorFalloff");
      entities = GL20.glGetUniformLocation(program,"mboEntityLightEnabled");
      if (volume >= 0) for (int i = 0; i < 8; i++) { positions[i] = GL20.glGetUniformLocation(program, "mboLightPosition[" + i + "]"); colors[i] = GL20.glGetUniformLocation(program, "mboLightColor[" + i + "]"); }
    }
  }
}
