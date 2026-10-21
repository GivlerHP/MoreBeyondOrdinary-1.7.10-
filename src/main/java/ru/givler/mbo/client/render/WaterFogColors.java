package ru.givler.mbo.client.render;

import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/** Vanilla's 3x3 water biome tint, interpolated between block centres. */
public final class WaterFogColors {
  private WaterFogColors() {}

  public static float[] sample(World world, Vec3 camera) {
    double x = camera.xCoord - .5D, z = camera.zCoord - .5D;
    int bx = MathHelper.floor_double(x), bz = MathHelper.floor_double(z);
    double fx = x - bx, fz = z - bz;
    float[] rgb = new float[3];
    // Four overlapping 3x3 kernels have only 16 unique biome queries.
    for (int dx = -1; dx <= 2; dx++) for (int dz = -1; dz <= 2; dz++) {
      double wx = (dx <= 1 ? 1D - fx : 0D) + (dx >= 0 ? fx : 0D);
      double wz = (dz <= 1 ? 1D - fz : 0D) + (dz >= 0 ? fz : 0D);
      double weight = wx * wz / (9D * 255D);
      if (weight == 0D) continue;
      int color = world.getBiomeGenForCoords(bx + dx, bz + dz).getWaterColorMultiplier();
      rgb[0] += ((color >> 16) & 255) * weight;
      rgb[1] += ((color >> 8) & 255) * weight;
      rgb[2] += (color & 255) * weight;
    }
    // The water texture supplies its blue base; biome water colour is a tint.
    // Preserve the previous clear-water fog brightness for the neutral tint.
    rgb[0] *= .055F;
    rgb[1] *= .145F;
    rgb[2] *= .235F;
    return rgb;
  }
}
