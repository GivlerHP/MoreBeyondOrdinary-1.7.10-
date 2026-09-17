package ru.givler.mbo.registry;

import cpw.mods.fml.common.registry.EntityRegistry;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.entity.magic.EntityMagicMissile;
import ru.givler.mbo.entity.magic.EntitySpectralArrow;
import ru.givler.mbo.entity.magic.EntityFirebolt;
import ru.givler.mbo.entity.magic.EntityIceShard;
import ru.givler.mbo.entity.magic.EntityThunderbolt;
import ru.givler.mbo.entity.magic.EntityForceOrb;
import ru.givler.mbo.entity.magic.EntityIceCharge;
import ru.givler.mbo.entity.magic.EntityFireOrb;
import ru.givler.mbo.entity.magic.EntityArcaneArrow;
import ru.givler.mbo.entity.magic.EntityMagicBomb;
import ru.givler.mbo.entity.magic.EntitySeekingLightning;
import ru.givler.mbo.entity.magic.EntityMagicSigil;
import ru.givler.mbo.entity.magic.EntityLightningArc;
import ru.givler.mbo.entity.magic.EntityBlackHole;
import ru.givler.mbo.entity.magic.EntityGroundMagicEffect;
import ru.givler.mbo.entity.magic.EntityMagicBubble;
import ru.givler.mbo.entity.magic.EntityMagicShield;
import ru.givler.mbo.entity.magic.EntityMagicStorm;
import ru.givler.mbo.entity.magic.EntityIceSpike;
import ru.givler.mbo.entity.magic.EntityMagicAura;
import ru.givler.mbo.entity.magic.EntityMeteor;
import ru.givler.mbo.entity.magic.EntityMagicConstruct;
import ru.givler.mbo.entity.magic.EntityMagicDecoy;
import ru.givler.mbo.entity.magic.EntitySpiritHorse;

/** Registers entities belonging to the MBO magic subsystem. */
public final class MagicEntityRegistry {
  private MagicEntityRegistry() {}

  public static void registerEntities() {
    EntityRegistry.registerModEntity(
        EntityMagicMissile.class,
        "MagicMissile",
        ModEntityIds.next(),
        MoreBeyondOrdinary.instance,
        128,
        1,
        true);
    EntityRegistry.registerModEntity(
        EntitySpectralArrow.class,
        "SpectralArrow",
        ModEntityIds.next(),
        MoreBeyondOrdinary.instance,
        128,
        1,
        true);
    register(EntityFirebolt.class, "Firebolt");
    register(EntityIceShard.class, "IceShard");
    register(EntityThunderbolt.class, "Thunderbolt");
    register(EntityForceOrb.class, "ForceOrb");
    register(EntityIceCharge.class, "IceCharge");
    register(EntityFireOrb.class, "FireOrb");
    register(EntityArcaneArrow.class, "ArcaneArrow");
    register(EntityMagicBomb.class, "MagicBomb");
    register(EntitySeekingLightning.class, "SeekingLightning");
    register(EntityMagicSigil.class, "MagicSigil");
    register(EntityLightningArc.class, "LightningArc");
    register(EntityBlackHole.class, "BlackHole");
    register(EntityGroundMagicEffect.class, "GroundMagicEffect");
    register(EntityMagicBubble.class, "MagicBubble");
    register(EntityMagicShield.class, "MagicShield");
    register(EntityMagicStorm.class, "MagicStorm");
    register(EntityIceSpike.class, "IceSpike");
    register(EntityMagicAura.class, "MagicAura");
    register(EntityMeteor.class, "Meteor");
    register(EntityMagicConstruct.class, "MagicConstruct");
    register(EntityMagicDecoy.class, "MagicDecoy");
    register(EntitySpiritHorse.class, "SpiritHorse");
  }

  private static void register(Class<? extends net.minecraft.entity.Entity> type, String name) {
    EntityRegistry.registerModEntity(type, name, ModEntityIds.next(), MoreBeyondOrdinary.instance,
        128, 1, true);
  }
}
