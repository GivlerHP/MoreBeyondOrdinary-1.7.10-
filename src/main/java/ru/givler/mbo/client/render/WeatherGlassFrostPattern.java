package ru.givler.mbo.client.render;

/**
 * Outdoor frost and snow ledge from Weather Glass 0.6.2, adapted to the 1.7.10 atlas.
 * Copyright (c) 2026 DuffoPuffo. See META-INF/LICENSE_Weather Glass.
 */
final class WeatherGlassFrostPattern {
   private WeatherGlassFrostPattern() {}

   static final int TOP = 1, BOTTOM = 2, LEFT = 4, RIGHT = 8;
   private static final int[] DENSE = new int[]{236, 246, 255};
   private static final int[] CRYSTAL = new int[]{200, 220, 255};
   private static final int[] ICE = new int[]{161, 195, 255};
   private static final int[] SPARKLE = new int[]{255, 255, 255};
   private static final int[] SNOW = new int[]{244, 252, 252};
   private static final int[] SNOW_SHADE = new int[]{220, 236, 246};
   private static final int[] SNOW_TOP = new int[]{255, 255, 255};
   private static final int[] SNOW_BODY = new int[]{247, 254, 254};
   private static final int[] SNOW_LOW = new int[]{240, 253, 253};

   static void draw(byte[] out, int size, int mask, float frost, float snow) {
      float k = (float)size / 32.0F;
      float reach = Math.max(0.5F, frost * 6.0F * k);
      float snowHeight = snow * 3.0F * k;

      for(int y = 0; y < size; ++y) {
         for(int x = 0; x < size; ++x) {
            int o = (y * size + x) * 4;
            int[] c = null;
            float a = 0.0F;
            float field = 0.0F;
            if ((mask & 1) != 0) {
               field += falloff(y, reach);
            }

            if ((mask & 2) != 0) {
               field += falloff(size - 1 - y, reach);
            }

            if ((mask & 4) != 0) {
               field += falloff(x, reach);
            }

            if ((mask & 8) != 0) {
               field += falloff(size - 1 - x, reach);
            }

            int full;
            float top;
            float fromBottom;
            if (frost > 0.02F && field > 0.0F) {
               full = size <= 16 ? 1 : 2;
               top = 0.6F * hash(x / full, y / full, 17) + 0.4F * hash(x, y, 91);
               fromBottom = field * (0.55F + 0.9F * top);
               if (fromBottom > 0.6F) {
                  if (fromBottom > 1.25F) {
                     c = DENSE;
                     a = 0.85F;
                  } else if (fromBottom > 0.85F) {
                     c = CRYSTAL;
                     a = 0.65F;
                  } else {
                     c = ICE;
                     a = 0.45F;
                  }

                  if (hash(x, y, 7) > 0.96F) {
                     c = SPARKLE;
                     a = 0.95F;
                  }
               }
            }

            if ((mask & 2) != 0 && size <= 16) {
               full = 2 + (hash(x / 3, 0, 33) > 0.6F ? 1 : 0);
               if ((mask & 4) != 0) {
                  full = Math.max(full, 5 - x / 2);
               }

               if ((mask & 8) != 0) {
                  full = Math.max(full, 5 - (size - 1 - x) / 2);
               }

               int height = Math.round((float)full * snow);
               int snowRow = size - 1 - y;
               if (snowRow < height) {
                  c = snowRow == height - 1 ? SNOW_TOP : (snowRow == 0 && height > 2 ? SNOW_LOW : SNOW_BODY);
                  a = 0.96F;
               }
            } else if ((mask & 2) != 0 && snowHeight > 0.3F) {
               float bump = (hash(x / 2, 0, 33) - 0.5F) * 1.6F * k;
               top = snowHeight + bump;
               fromBottom = (float)(size - 1 - y);
               if (fromBottom < top) {
                  c = fromBottom > top - 1.0F ? SNOW_SHADE : SNOW;
                  a = 0.95F;
               }
            }

            if (c == null) {
               out[o] = -20;
               out[o + 1] = -10;
               out[o + 2] = -1;
               out[o + 3] = 0;
            } else {
               out[o] = (byte)c[0];
               out[o + 1] = (byte)c[1];
               out[o + 2] = (byte)c[2];
               out[o + 3] = (byte)Math.round(a * 255.0F);
            }
         }
      }

   }

   private static float falloff(int distance, float reach) {
      return (float)Math.exp((double)((float)(-distance) / reach));
   }

   private static float hash(int x, int y, int salt) {
      int h = x * 668265261 ^ y * 374761393 ^ salt * -1640531527;
      h ^= h >>> 15;
      h *= -2048144789;
      h ^= h >>> 13;
      return (float)(h & '\uffff') / 65535.0F;
   }
}
