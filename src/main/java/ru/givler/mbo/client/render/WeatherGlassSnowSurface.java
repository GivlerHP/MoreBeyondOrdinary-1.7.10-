package ru.givler.mbo.client.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** Snowflake simulation from Weather Glass 0.6.2. Copyright (c) 2026 DuffoPuffo. */
final class WeatherGlassSnowSurface {
   private static final float DT = 0.05F;
   private static final int WHITE = 16777215;
   private static final int POWDER = 15531003;
   private static final int ICE = 12375295;
   private static final int[][][] SHAPES = new int[][][]{{{0, 0, 16777215, 90}}, {{0, 0, 16777215, 90}, {1, 1, 15531003, 75}}, {{0, 0, 16777215, 95}, {-1, 0, 15531003, 80}, {1, 0, 15531003, 80}, {0, -1, 15531003, 80}, {0, 1, 15531003, 80}}, {{0, 0, 16777215, 95}, {-1, 0, 16777215, 85}, {1, 0, 16777215, 85}, {0, -1, 16777215, 85}, {0, 1, 16777215, 85}, {-2, 0, 12375295, 70}, {2, 0, 12375295, 70}, {0, -2, 12375295, 70}, {0, 2, 12375295, 70}, {-1, -1, 12375295, 60}, {1, -1, 12375295, 60}, {-1, 1, 12375295, 60}, {1, 1, 12375295, 60}}};
   private final int width;
   private final int height;
   private final float k;
   private final float[] kinds;
   public static final int STRIPS = 8;
   private final WeatherGlassSnowSurface.Strip[] strips = new WeatherGlassSnowSurface.Strip[8];
   private boolean empty = true;

   public WeatherGlassSnowSurface(int width) {
      this.width = width;
      this.height = width * 4;
      this.k = (float)width / 32.0F;
      this.kinds = width >= 64 ? new float[]{0.45F, 0.25F, 0.2F, 0.1F} : (width <= 16 ? new float[]{0.8F, 0.15F, 0.05F, 0.0F} : new float[]{0.6F, 0.25F, 0.12F, 0.03F});

      for(int i = 0; i < this.strips.length; ++i) {
         this.strips[i] = new WeatherGlassSnowSurface.Strip(new Random(23055L + 104729L * (long)i + (long)width));
      }

   }

   public int width() {
      return this.width;
   }

   public boolean tick(float snow, float amount) {
      if (amount <= 0.0F) {
         if (this.empty) {
            return false;
         }

         this.clear();
      }

      boolean any = false;
      boolean flakes = false;
      WeatherGlassSnowSurface.Strip[] var5 = this.strips;
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         WeatherGlassSnowSurface.Strip strip = var5[var7];
         any |= strip.tick(snow, amount);
         flakes |= !strip.flakes.isEmpty();
      }

      this.empty = !flakes;
      return any;
   }

   public boolean changed(int strip) {
      return this.strips[strip].changed;
   }

   public void clear() {
      WeatherGlassSnowSurface.Strip[] var1 = this.strips;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         WeatherGlassSnowSurface.Strip strip = var1[var3];
         strip.flakes.clear();
         strip.fresh = true;
      }

   }

   public byte[] pixels(int strip) {
      return this.strips[strip].rgba;
   }

   private final class Strip {
      final Random random;
      final List<Flake> flakes = new ArrayList<>();
      final float[] alpha;
      final int[] color;
      final byte[] rgba;
      float carry;
      boolean renderedEmpty;
      boolean changed;
      boolean fresh;

      Strip(Random random) {
         this.alpha = new float[WeatherGlassSnowSurface.this.width * WeatherGlassSnowSurface.this.height];
         this.color = new int[this.alpha.length];
         this.rgba = new byte[this.alpha.length * 4];
         this.fresh = true;
         this.random = random;
      }

      boolean tick(float snow, float amount) {
         this.changed = false;
         boolean dirty = this.fresh;
         boolean snowing = snow > 0.05F;
         if (snowing) {
            float strength = Math.min(snow, 1.4F);
            this.carry += 6.0F * WeatherGlassSnowSurface.this.k * WeatherGlassSnowSurface.this.k * amount * strength * 0.05F;
            int cap = (int)(140.0F * WeatherGlassSnowSurface.this.k * WeatherGlassSnowSurface.this.k * amount * strength);

            while(this.carry >= 1.0F) {
               --this.carry;
               if (this.flakes.size() < cap) {
                  this.land();
                  dirty = true;
               }
            }
         }

         for(int i = this.flakes.size() - 1; i >= 0; --i) {
            WeatherGlassSnowSurface.Flake flake = this.flakes.get(i);
            dirty |= fading(flake);
            if (snowing) {
               flake.life = -1;
            } else if (flake.life < 0) {
               flake.lifeTotal = 20 * (20 + this.random.nextInt(41));
               flake.life = flake.lifeTotal;
            } else if (--flake.life <= 0) {
               this.flakes.remove(i);
               dirty = true;
               continue;
            }

            dirty |= fading(flake);
         }

         if (this.flakes.isEmpty()) {
            if (this.renderedEmpty) {
               return false;
            }

            this.renderedEmpty = true;
            dirty = true;
         } else {
            this.renderedEmpty = false;
         }

         if (!dirty) {
            return false;
         } else {
            this.fresh = false;
            this.render();
            this.changed = true;
            return true;
         }
      }

      private boolean fading(WeatherGlassSnowSurface.Flake flake) {
         return flake.life >= 0 && (float)flake.life < (float)flake.lifeTotal * 0.4F;
      }

      void land() {
         float r = this.random.nextFloat();
         int kind = 0;

         for(float sum = WeatherGlassSnowSurface.this.kinds[0]; kind < WeatherGlassSnowSurface.this.kinds.length - 1 && r >= sum; sum += WeatherGlassSnowSurface.this.kinds[kind]) {
            ++kind;
         }

         WeatherGlassSnowSurface.Flake flake = new WeatherGlassSnowSurface.Flake();
         flake.kind = kind;
         int margin = kind >= 3 ? 3 : 2;
         flake.x = margin + this.random.nextInt(WeatherGlassSnowSurface.this.width - 2 * margin);
         flake.y = this.random.nextInt(WeatherGlassSnowSurface.this.height);
         this.flakes.add(flake);
      }

      boolean render() {
         Arrays.fill(this.alpha, 0.0F);
         int w = WeatherGlassSnowSurface.this.width;
         int h = WeatherGlassSnowSurface.this.height;


         float fade;
         int kind;
         for (Flake flake : flakes) {
            fade = 1.0F;
            kind = flake.kind;
            if (flake.life >= 0) {
               float left = (float)flake.life / (float)flake.lifeTotal;
               fade = Math.min(1.0F, left / 0.4F);
               if (left < 0.25F) {
                  kind = Math.min(kind, 1);
               }
            }

            int[][] var16 = WeatherGlassSnowSurface.SHAPES[kind];
            int var8 = var16.length;

            for(int var9 = 0; var9 < var8; ++var9) {
               int[] cell = var16[var9];
               int x = flake.x + cell[0];
               if (x >= 0 && x < w) {
                  int i = Math.floorMod(flake.y + cell[1], h) * w + x;
                  float a = (float)cell[3] / 100.0F * fade;
                  if (a > this.alpha[i]) {
                     this.alpha[i] = a;
                     this.color[i] = cell[2];
                  }
               }
            }
         }

         boolean any = false;

         for(int ix = 0; ix < this.alpha.length; ++ix) {
            fade = this.alpha[ix];
            kind = fade > 0.0F ? this.color[ix] : 16777215;
            int o = ix * 4;
            this.rgba[o] = (byte)(kind >> 16);
            this.rgba[o + 1] = (byte)(kind >> 8);
            this.rgba[o + 2] = (byte)kind;
            this.rgba[o + 3] = (byte)Math.round(fade * 255.0F);
            any |= fade > 0.0F;
         }

         return any;
      }
   }

   private static final class Flake {
      int x;
      int y;
      int kind;
      int life = -1;
      int lifeTotal;
   }
}
