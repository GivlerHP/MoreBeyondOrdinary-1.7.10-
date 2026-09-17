package ru.givler.mbo.particles;

public enum EnumParticleType {
  SACRED,
  DARK_MAGIC,
  DUST,
  ICE,
  MAGIC_FIRE,
  SNOW,
  BLIZZARD,
  SPARK,
  SPARKLE,
  LEAF,
  PATH,

  VANILLA_WITCH_MAGIC,
  VANILLA_FLAME,
  VANILLA_SMOKE,
  VANILLA_LARGE_EXPLODE,
  VANILLA_MAGICCRIT,
  VANILLA_HEART,
  VANILLA_SPLASH,
  VANILLA_BUBBLE;

  /** Возвращает строку для world.spawnParticle(), только для VANILLA_* типов */
  public String getVanillaName() {
    switch (this) {
      case VANILLA_WITCH_MAGIC:
        return "witchMagic";
      case VANILLA_FLAME:
        return "flame";
      case VANILLA_SMOKE:
        return "smoke";
      case VANILLA_LARGE_EXPLODE:
        return "largeexplode";
      case VANILLA_MAGICCRIT:
        return "magicCrit";
      case VANILLA_HEART:
        return "heart";
      case VANILLA_SPLASH:
        return "splash";
      case VANILLA_BUBBLE:
        return "bubble";

      default:
        return null;
    }
  }

  public boolean isVanilla() {
    return getVanillaName() != null;
  }

  public static EnumParticleType byOrdinal(int ordinal) {
    EnumParticleType[] values = values();
    if (ordinal < 0 || ordinal >= values.length) return SACRED;
    return values[ordinal];
  }
}
