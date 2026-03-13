package ru.givler.mbo;

public enum EnumParticleType {

    SACRED,
    DARK_MAGIC,
    DUST,
    ICE,
    LEAF,
    MAGIC_BUBBLE,
    MAGIC_FIRE,
    PATH,
    SNOW,
    SPARK,
    SPARKLE,


    VANILLA_WITCH_MAGIC,
    VANILLA_PORTAL,
    VANILLA_FLAME,
    VANILLA_SMOKE,
    VANILLA_CRIT,
    VANILLA_MAGICCRIT,
    VANILLA_SPELL,
    VANILLA_NOTE,
    VANILLA_HEART,
    VANILLA_SPLASH,
    VANILLA_BUBBLE,
    VANILLA_EXPLODE;

    /** Возвращает строку для world.spawnParticle(), только для VANILLA_* типов */
    public String getVanillaName() {
        switch (this) {
            case VANILLA_WITCH_MAGIC: return "witchMagic";
            case VANILLA_PORTAL:      return "portal";
            case VANILLA_FLAME:       return "flame";
            case VANILLA_SMOKE:       return "smoke";
            case VANILLA_CRIT:        return "crit";
            case VANILLA_MAGICCRIT:   return "magicCrit";
            case VANILLA_SPELL:       return "spell";
            case VANILLA_NOTE:        return "note";
            case VANILLA_HEART:       return "heart";
            case VANILLA_SPLASH:      return "splash";
            case VANILLA_BUBBLE:      return "bubble";
            case VANILLA_EXPLODE:     return "explode";

            default: return null;
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