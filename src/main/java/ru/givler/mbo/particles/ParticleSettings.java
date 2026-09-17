package ru.givler.mbo.particles;

/** Complete visual parameters for an MBO particle. */
public final class ParticleSettings {
  public final int lifetime;
  public final float red;
  public final float green;
  public final float blue;
  public final float scale;
  public final boolean gravity;

  public ParticleSettings(
      int lifetime, float red, float green, float blue, float scale, boolean gravity) {
    this.lifetime = Math.max(1, lifetime);
    this.red = red;
    this.green = green;
    this.blue = blue;
    this.scale = scale;
    this.gravity = gravity;
  }

  public static ParticleSettings defaults(EnumParticleType type) {
    switch (type) {
      case SPARK:
        return new ParticleSettings(4, 0.65F, 0.85F, 1.0F, 1.3F, false);
      case ICE:
        return new ParticleSettings(24, 0.7F, 0.9F, 1.0F, 0.8F, true);
      case SNOW:
        return new ParticleSettings(45, 1, 1, 1, 0.6F, true);
      case MAGIC_FIRE:
        return new ParticleSettings(12, 1, 1, 1, 1.0F, false);
      case LEAF:
        return new ParticleSettings(45, 1.0F, 1.0F, 1.0F, 1.4F, false);
      default:
        return new ParticleSettings(24, 1, 1, 1, 1.0F, false);
    }
  }
}
