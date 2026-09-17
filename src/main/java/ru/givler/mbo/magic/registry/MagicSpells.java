package ru.givler.mbo.magic.registry;

import net.minecraft.item.EnumAction;
import net.minecraft.potion.Potion;
import ru.givler.mbo.entity.magic.EntityArcaneArrow;
import ru.givler.mbo.entity.magic.EntityMagicAura;
import ru.givler.mbo.entity.magic.EntityMagicBomb;
import ru.givler.mbo.entity.magic.EntityMagicConstruct;
import ru.givler.mbo.entity.magic.EntityMagicSigil;
import ru.givler.mbo.entity.magic.EntityMagicStorm;
import ru.givler.mbo.entity.magic.EntitySeekingLightning;
import ru.givler.mbo.magic.api.CastType;
import ru.givler.mbo.magic.api.MagicElement;
import ru.givler.mbo.magic.api.Spell;
import ru.givler.mbo.magic.api.SpellTier;
import ru.givler.mbo.magic.spell.AerialMovementExecutor;
import ru.givler.mbo.magic.spell.ArcaneArrowExecutor;
import ru.givler.mbo.magic.spell.BanishExecutor;
import ru.givler.mbo.magic.spell.BlackHoleExecutor;
import ru.givler.mbo.magic.spell.BlinkExecutor;
import ru.givler.mbo.magic.spell.BubblePrisonExecutor;
import ru.givler.mbo.magic.spell.ChannelledLightningExecutor;
import ru.givler.mbo.magic.spell.CobwebsExecutor;
import ru.givler.mbo.magic.spell.ConjureItemExecutor;
import ru.givler.mbo.magic.spell.CureEffectsExecutor;
import ru.givler.mbo.magic.spell.DecayGroundExecutor;
import ru.givler.mbo.magic.spell.DetonateExecutor;
import ru.givler.mbo.magic.spell.ElementalProjectileExecutor;
import ru.givler.mbo.magic.spell.ElementalRayExecutor;
import ru.givler.mbo.magic.spell.FreezeExecutor;
import ru.givler.mbo.magic.spell.GroupHealExecutor;
import ru.givler.mbo.magic.spell.GrowthAuraExecutor;
import ru.givler.mbo.magic.spell.HealAllyExecutor;
import ru.givler.mbo.magic.spell.HealExecutor;
import ru.givler.mbo.magic.spell.IceAgeExecutor;
import ru.givler.mbo.magic.spell.IceSpikesExecutor;
import ru.givler.mbo.magic.spell.IceStatueExecutor;
import ru.givler.mbo.magic.spell.IgniteExecutor;
import ru.givler.mbo.magic.spell.IntimidateExecutor;
import ru.givler.mbo.magic.spell.InvigoratingPresenceExecutor;
import ru.givler.mbo.magic.spell.InvokeWeatherExecutor;
import ru.givler.mbo.magic.spell.LeapExecutor;
import ru.givler.mbo.magic.spell.LifeDrainExecutor;
import ru.givler.mbo.magic.spell.LightExecutor;
import ru.givler.mbo.magic.spell.LightningArcExecutor;
import ru.givler.mbo.magic.spell.LightningBoltExecutor;
import ru.givler.mbo.magic.spell.LightningPulseExecutor;
import ru.givler.mbo.magic.spell.MagicAuraExecutor;
import ru.givler.mbo.magic.spell.MagicBombExecutor;
import ru.givler.mbo.magic.spell.MagicMissileExecutor;
import ru.givler.mbo.magic.spell.MagicStormExecutor;
import ru.givler.mbo.magic.spell.MeteorExecutor;
import ru.givler.mbo.magic.spell.MindControlExecutor;
import ru.givler.mbo.magic.spell.MindTrickExecutor;
import ru.givler.mbo.magic.spell.PetrifyExecutor;
import ru.givler.mbo.magic.spell.PhaseStepExecutor;
import ru.givler.mbo.magic.spell.PocketFurnaceExecutor;
import ru.givler.mbo.magic.spell.RayEffectExecutor;
import ru.givler.mbo.magic.spell.RemainingSpellExecutors;
import ru.givler.mbo.magic.spell.ReplenishHungerExecutor;
import ru.givler.mbo.magic.spell.RingOfFireExecutor;
import ru.givler.mbo.magic.spell.SeekingLightningExecutor;
import ru.givler.mbo.magic.spell.SelfPotionExecutor;
import ru.givler.mbo.magic.spell.ShadowWardExecutor;
import ru.givler.mbo.magic.spell.ShieldExecutor;
import ru.givler.mbo.magic.spell.ShockwaveExecutor;
import ru.givler.mbo.magic.spell.SigilExecutor;
import ru.givler.mbo.magic.spell.SnareExecutor;
import ru.givler.mbo.magic.spell.SnowballExecutor;
import ru.givler.mbo.magic.spell.SummonSpiritHorseExecutor;
import ru.givler.mbo.magic.spell.ThunderstormExecutor;
import ru.givler.mbo.magic.spell.WallOfFrostExecutor;
import ru.givler.mbo.magic.spell.WeaponEnchantmentExecutor;
import ru.givler.mbo.magic.spell.WhirlwindExecutor;
import ru.givler.mbo.magic.spell.WitherSkullExecutor;
import ru.givler.mbo.registry.MagicItemRegistry;
import ru.givler.mbo.registry.PotionRegistry;

/** Built-in spell definitions owned by MBO. */
public final class MagicSpells {
  public static Spell MAGIC_MISSILE;
  public static Spell SNOWBALL;
  public static Spell HEAL;
  public static Spell HEAL_ALLY;
  public static Spell REPLENISH_HUNGER;
  public static Spell CURE_EFFECTS;
  public static Spell CONJURE_SWORD;
  public static Spell CONJURE_BOW;
  public static Spell CONJURE_PICKAXE;
  public static Spell FLAMING_AXE;
  public static Spell FROST_AXE;
  public static Spell FIREBOLT;
  public static Spell ICE_SHARD;
  public static Spell THUNDERBOLT;
  public static Spell IMBUE_WEAPON;
  public static Spell FLAMING_WEAPON;
  public static Spell FREEZING_WEAPON;
  public static Spell FIRE_RESISTANCE;
  public static Spell FIRESKIN;
  public static Spell ICE_SHROUD;
  public static Spell STATIC_AURA;
  public static Spell WATER_BREATHING;
  public static Spell INVISIBILITY;
  public static Spell SIXTH_SENSE;
  public static Spell TRANSIENCE;
  public static Spell DARKVISION;
  public static Spell OAKFLESH;
  public static Spell IRONFLESH;
  public static Spell DIAMONDFLESH;
  public static Spell IGNITE;
  public static Spell FREEZE;
  public static Spell POISON;
  public static Spell WITHER;
  public static Spell ARCANE_JAMMER;
  public static Spell MIND_TRICK;
  public static Spell INTIMIDATE;
  public static Spell LIFE_DRAIN;
  public static Spell AGILITY;
  public static Spell GREATER_HEAL;
  public static Spell GROUP_HEAL;
  public static Spell LEAP;
  public static Spell WHIRLWIND;
  public static Spell BANISH;
  public static Spell MIND_CONTROL;
  public static Spell FORCE_ORB;
  public static Spell ICE_CHARGE;
  public static Spell FIREBALL;
  public static Spell GREATER_FIREBALL;
  public static Spell DART;
  public static Spell FORCE_ARROW;
  public static Spell ICE_LANCE;
  public static Spell LIGHTNING_ARROW;
  public static Spell DARKNESS_ORB;
  public static Spell FIREBOMB;
  public static Spell POISON_BOMB;
  public static Spell SMOKE_BOMB;
  public static Spell SPARK_BOMB;
  public static Spell HOMING_SPARK;
  public static Spell LIGHTNING_DISC;
  public static Spell WITHER_SKULL;
  public static Spell FIRE_SIGIL;
  public static Spell FROST_SIGIL;
  public static Spell LIGHTNING_SIGIL;
  public static Spell SNARE;
  public static Spell ARC;
  public static Spell CHAIN_LIGHTNING;
  public static Spell LIGHTNING_BOLT;
  public static Spell LIGHTNING_RAY;
  public static Spell LIGHTNING_WEB;
  public static Spell LIGHTNING_PULSE;
  public static Spell FLAME_RAY;
  public static Spell FROST_RAY;
  public static Spell FIRESTORM;
  public static Spell BLINK;
  public static Spell PHASE_STEP;
  public static Spell GLIDE;
  public static Spell LEVITATION;
  public static Spell FLIGHT;
  public static Spell PETRIFY;
  public static Spell BLACK_HOLE;
  public static Spell DETONATE;
  public static Spell POCKET_FURNACE;
  public static Spell INVOKE_WEATHER;
  public static Spell COBWEBS;
  public static Spell WALL_OF_FROST;
  public static Spell RING_OF_FIRE;
  public static Spell DECAY;
  public static Spell BUBBLE;
  public static Spell ENTRAPMENT;
  public static Spell LIGHT;
  public static Spell GROWTH_AURA;
  public static Spell INVIGORATING_PRESENCE;
  public static Spell SHADOW_WARD;
  public static Spell SHOCKWAVE;
  public static Spell SHIELD;
  public static Spell ARROW_RAIN;
  public static Spell HAILSTORM;
  public static Spell BLIZZARD;
  public static Spell ICE_SPIKES;
  public static Spell ICE_STATUE, ICE_AGE;
  public static Spell FORCEFIELD, HEALING_AURA;
  public static Spell THUNDERSTORM;
  public static Spell METEOR;
  public static Spell CLAIRVOYANCE, CURSE_OF_SOULBINDING, DECOY, EARTHQUAKE, FORESTS_CURSE;
  public static Spell LIGHTNING_HAMMER, METAMORPHOSIS, PLAGUE_OF_DARKNESS;
  public static Spell SPECTRAL_PATHWAY, TELEKINESIS, TORNADO;
  public static Spell SUMMON_SPIRIT_HORSE;

  private MagicSpells() {}

  public static void register() {
    MAGIC_MISSILE =
        register(
            "magic_missile",
            MagicElement.ARCANE,
            SpellTier.BASIC,
            5,
            10,
            new MagicMissileExecutor());
    SNOWBALL =
        register("snowball", MagicElement.FROST, SpellTier.BASIC, 1, 1, new SnowballExecutor());
    HEAL = register("heal", MagicElement.HEALING, SpellTier.BASIC, 5, 20, new HealExecutor());
    HEAL_ALLY =
        register(
            "heal_ally",
            MagicElement.HEALING,
            SpellTier.APPRENTICE,
            10,
            20,
            new HealAllyExecutor());
    REPLENISH_HUNGER =
        register(
            "replenish_hunger",
            MagicElement.HEALING,
            SpellTier.APPRENTICE,
            10,
            30,
            new ReplenishHungerExecutor());
    CURE_EFFECTS =
        register(
            "cure_effects",
            MagicElement.HEALING,
            SpellTier.APPRENTICE,
            25,
            40,
            new CureEffectsExecutor());
    CONJURE_SWORD =
        register(
            "conjure_sword",
            MagicElement.SORCERY,
            SpellTier.APPRENTICE,
            25,
            50,
            new ConjureItemExecutor(MagicItemRegistry.spectralSword));
    CONJURE_BOW =
        register(
            "conjure_bow",
            MagicElement.SORCERY,
            SpellTier.APPRENTICE,
            40,
            50,
            new ConjureItemExecutor(MagicItemRegistry.spectralBow));
    CONJURE_PICKAXE =
        register(
            "conjure_pickaxe",
            MagicElement.SORCERY,
            SpellTier.APPRENTICE,
            25,
            50,
            new ConjureItemExecutor(MagicItemRegistry.spectralPickaxe));
    FLAMING_AXE =
        register(
            "flaming_axe",
            MagicElement.FIRE,
            SpellTier.ADVANCED,
            45,
            50,
            new ConjureItemExecutor(MagicItemRegistry.flamingAxe));
    FROST_AXE =
        register(
            "frost_axe",
            MagicElement.FROST,
            SpellTier.ADVANCED,
            45,
            50,
            new ConjureItemExecutor(MagicItemRegistry.frostAxe));
    FIREBOLT =
        register(
            "firebolt",
            MagicElement.FIRE,
            SpellTier.APPRENTICE,
            10,
            10,
            new ElementalProjectileExecutor(ElementalProjectileExecutor.Kind.FIREBOLT));
    ICE_SHARD =
        register(
            "ice_shard",
            MagicElement.FROST,
            SpellTier.APPRENTICE,
            10,
            10,
            new ElementalProjectileExecutor(ElementalProjectileExecutor.Kind.ICE_SHARD));
    THUNDERBOLT =
        register(
            "thunderbolt",
            MagicElement.LIGHTNING,
            SpellTier.BASIC,
            10,
            15,
            new ElementalProjectileExecutor(ElementalProjectileExecutor.Kind.THUNDERBOLT));
    IMBUE_WEAPON =
        register(
            "imbue_weapon",
            MagicElement.SORCERY,
            SpellTier.APPRENTICE,
            20,
            50,
            new WeaponEnchantmentExecutor(WeaponEnchantmentExecutor.Kind.IMBUEMENT));
    FLAMING_WEAPON =
        register(
            "flaming_weapon",
            MagicElement.FIRE,
            SpellTier.ADVANCED,
            35,
            70,
            new WeaponEnchantmentExecutor(WeaponEnchantmentExecutor.Kind.FIRE));
    FREEZING_WEAPON =
        register(
            "freezing_weapon",
            MagicElement.FROST,
            SpellTier.ADVANCED,
            35,
            70,
            new WeaponEnchantmentExecutor(WeaponEnchantmentExecutor.Kind.FROST));
    FIRE_RESISTANCE =
        register(
            "fire_resistance",
            MagicElement.FIRE,
            SpellTier.ADVANCED,
            20,
            80,
            new SelfPotionExecutor(
                Potion.fireResistance, 600, 0, 1.0F, 0.5F, 0.0F, "mbo:heal", false));
    FIRESKIN =
        register(
            "fireskin",
            MagicElement.FIRE,
            SpellTier.ADVANCED,
            40,
            250,
            new SelfPotionExecutor(
                PotionRegistry.Fireskin, 600, 0, 1.0F, 0.2F, 0.0F, "fire.ignite", true));
    ICE_SHROUD =
        register(
            "ice_shroud",
            MagicElement.FROST,
            SpellTier.ADVANCED,
            40,
            250,
            new SelfPotionExecutor(
                PotionRegistry.IceShroud, 600, 0, 0.3F, 0.8F, 1.0F, "mbo:ice", true));
    STATIC_AURA =
        register(
            "static_aura",
            MagicElement.LIGHTNING,
            SpellTier.ADVANCED,
            40,
            250,
            new SelfPotionExecutor(
                PotionRegistry.StaticAura, 600, 0, 0.2F, 0.5F, 1.0F, "mbo:arc", true));
    WATER_BREATHING =
        register(
            "water_breathing",
            MagicElement.EARTH,
            SpellTier.ADVANCED,
            30,
            250,
            new SelfPotionExecutor(
                Potion.waterBreathing, 1200, 0, 0.3F, 0.3F, 1.0F, "mbo:heal", false));
    INVISIBILITY =
        register(
            "invisibility",
            MagicElement.SORCERY,
            SpellTier.ADVANCED,
            35,
            200,
            new SelfPotionExecutor(
                Potion.invisibility, 600, 0, 0.7F, 1.0F, 1.0F, "mbo:heal", false));
    SIXTH_SENSE =
        register(
            "sixth_sense",
            MagicElement.EARTH,
            SpellTier.APPRENTICE,
            20,
            100,
            new SelfPotionExecutor(
                PotionRegistry.SixthSense, 400, 0, 0.5F, 0.8F, 0.3F, "mob.wither.shoot", false));
    TRANSIENCE =
        register(
            "transience",
            MagicElement.HEALING,
            SpellTier.ADVANCED,
            50,
            100,
            new SelfPotionExecutor(
                PotionRegistry.Transience,
                Potion.invisibility,
                400,
                0,
                1.0F,
                0.9F,
                0.6F,
                "mbo:aura",
                true));
    DARKVISION =
        register(
            "darkvision",
            MagicElement.EARTH,
            SpellTier.APPRENTICE,
            20,
            40,
            new SelfPotionExecutor(
                Potion.nightVision, 900, 0, 0.0F, 0.4F, 0.7F, "mbo:heal", false));
    OAKFLESH =
        register(
            "oakflesh",
            MagicElement.HEALING,
            SpellTier.APPRENTICE,
            20,
            50,
            new SelfPotionExecutor(Potion.resistance, 600, 1, 0.6F, 0.5F, 0.4F, "mbo:heal", false));
    IRONFLESH =
        register(
            "ironflesh",
            MagicElement.HEALING,
            SpellTier.ADVANCED,
            30,
            100,
            new SelfPotionExecutor(Potion.resistance, 600, 2, 0.4F, 0.5F, 0.6F, "mbo:heal", false));
    DIAMONDFLESH =
        register(
            "diamondflesh",
            MagicElement.HEALING,
            SpellTier.MASTER,
            100,
            300,
            new SelfPotionExecutor(Potion.resistance, 600, 4, 0.0F, 0.5F, 1.0F, "mbo:heal", false));
    IGNITE = register("ignite", MagicElement.FIRE, SpellTier.BASIC, 5, 10, new IgniteExecutor());
    FREEZE = register("freeze", MagicElement.FROST, SpellTier.BASIC, 5, 10, new FreezeExecutor());
    POISON =
        register(
            "poison",
            MagicElement.EARTH,
            SpellTier.APPRENTICE,
            10,
            20,
            new RayEffectExecutor(RayEffectExecutor.Kind.POISON, Potion.poison, 200, 1));
    WITHER =
        register(
            "wither",
            MagicElement.NECROMANCY,
            SpellTier.APPRENTICE,
            10,
            20,
            new RayEffectExecutor(RayEffectExecutor.Kind.WITHER, Potion.wither, 200, 1));
    ARCANE_JAMMER =
        register(
            "arcane_jammer",
            MagicElement.HEALING,
            SpellTier.ADVANCED,
            30,
            50,
            new RayEffectExecutor(
                RayEffectExecutor.Kind.ARCANE_JAMMER, PotionRegistry.ArcaneJammer, 300, 0));
    MIND_TRICK =
        register(
            "mind_trick",
            MagicElement.NECROMANCY,
            SpellTier.BASIC,
            10,
            40,
            new MindTrickExecutor());
    INTIMIDATE =
        register(
            "intimidate",
            MagicElement.NECROMANCY,
            SpellTier.APPRENTICE,
            20,
            100,
            new IntimidateExecutor());
    LIFE_DRAIN =
        register(
            "life_drain",
            MagicElement.NECROMANCY,
            SpellTier.APPRENTICE,
            CastType.CONTINUOUS,
            10,
            0,
            new LifeDrainExecutor());
    AGILITY =
        register(
            "agility",
            MagicElement.SORCERY,
            SpellTier.APPRENTICE,
            20,
            40,
            new SelfPotionExecutor(
                Potion.moveSpeed, Potion.jump, 600, 1, 0.6F, 0.6F, 1.0F, "mbo:heal", false));
    GREATER_HEAL =
        register(
            "greater_heal",
            MagicElement.HEALING,
            SpellTier.ADVANCED,
            15,
            40,
            new HealExecutor(8.0F));
    GROUP_HEAL =
        register(
            "group_heal",
            MagicElement.HEALING,
            SpellTier.ADVANCED,
            35,
            150,
            new GroupHealExecutor());
    LEAP = register("leap", MagicElement.EARTH, SpellTier.BASIC, 10, 20, new LeapExecutor());
    WHIRLWIND =
        register(
            "whirlwind", MagicElement.EARTH, SpellTier.APPRENTICE, 10, 15, new WhirlwindExecutor());
    BANISH =
        register(
            "banish", MagicElement.NECROMANCY, SpellTier.APPRENTICE, 15, 40, new BanishExecutor());
    MIND_CONTROL =
        register(
            "mind_control",
            MagicElement.NECROMANCY,
            SpellTier.ADVANCED,
            40,
            150,
            new MindControlExecutor());
    FORCE_ORB =
        register(
            "force_orb",
            MagicElement.SORCERY,
            SpellTier.ADVANCED,
            20,
            20,
            new ElementalProjectileExecutor(ElementalProjectileExecutor.Kind.FORCE_ORB));
    ICE_CHARGE =
        register(
            "ice_charge",
            MagicElement.FROST,
            SpellTier.ADVANCED,
            20,
            30,
            new ElementalProjectileExecutor(ElementalProjectileExecutor.Kind.ICE_CHARGE));
    FIREBALL =
        register(
            "fireball",
            MagicElement.FIRE,
            SpellTier.APPRENTICE,
            10,
            15,
            new ElementalProjectileExecutor(ElementalProjectileExecutor.Kind.FIREBALL));
    GREATER_FIREBALL =
        register(
            "greater_fireball",
            MagicElement.FIRE,
            SpellTier.ADVANCED,
            20,
            30,
            new ElementalProjectileExecutor(ElementalProjectileExecutor.Kind.GREATER_FIREBALL));
    DART =
        register(
            "dart",
            MagicElement.EARTH,
            SpellTier.BASIC,
            5,
            10,
            new ArcaneArrowExecutor(EntityArcaneArrow.Kind.DART, 2.0F));
    FORCE_ARROW =
        register(
            "force_arrow",
            MagicElement.SORCERY,
            SpellTier.APPRENTICE,
            15,
            20,
            new ArcaneArrowExecutor(EntityArcaneArrow.Kind.FORCE, 1.0F));
    ICE_LANCE =
        register(
            "ice_lance",
            MagicElement.FROST,
            SpellTier.ADVANCED,
            20,
            20,
            new ArcaneArrowExecutor(EntityArcaneArrow.Kind.ICE_LANCE, 2.0F));
    LIGHTNING_ARROW =
        register(
            "lightning_arrow",
            MagicElement.LIGHTNING,
            SpellTier.APPRENTICE,
            15,
            20,
            new ArcaneArrowExecutor(EntityArcaneArrow.Kind.LIGHTNING, 2.0F));
    DARKNESS_ORB =
        register(
            "darkness_orb",
            MagicElement.NECROMANCY,
            SpellTier.ADVANCED,
            20,
            20,
            new MagicBombExecutor(EntityMagicBomb.Kind.DARKNESS));
    FIREBOMB =
        register(
            "firebomb",
            MagicElement.FIRE,
            SpellTier.APPRENTICE,
            15,
            25,
            new MagicBombExecutor(EntityMagicBomb.Kind.FIRE));
    POISON_BOMB =
        register(
            "poison_bomb",
            MagicElement.EARTH,
            SpellTier.APPRENTICE,
            15,
            25,
            new MagicBombExecutor(EntityMagicBomb.Kind.POISON));
    SMOKE_BOMB =
        register(
            "smoke_bomb",
            MagicElement.FIRE,
            SpellTier.BASIC,
            10,
            20,
            new MagicBombExecutor(EntityMagicBomb.Kind.SMOKE));
    SPARK_BOMB =
        register(
            "spark_bomb",
            MagicElement.LIGHTNING,
            SpellTier.APPRENTICE,
            15,
            25,
            new MagicBombExecutor(EntityMagicBomb.Kind.SPARK));
    HOMING_SPARK =
        register(
            "homing_spark",
            MagicElement.LIGHTNING,
            SpellTier.APPRENTICE,
            10,
            20,
            new SeekingLightningExecutor(EntitySeekingLightning.Kind.SPARK));
    LIGHTNING_DISC =
        register(
            "lightning_disc",
            MagicElement.LIGHTNING,
            SpellTier.ADVANCED,
            25,
            60,
            new SeekingLightningExecutor(EntitySeekingLightning.Kind.DISC));
    WITHER_SKULL =
        register(
            "wither_skull",
            MagicElement.NECROMANCY,
            SpellTier.ADVANCED,
            20,
            30,
            new WitherSkullExecutor());
    FIRE_SIGIL =
        register(
            "fire_sigil",
            MagicElement.FIRE,
            SpellTier.APPRENTICE,
            10,
            20,
            new SigilExecutor(EntityMagicSigil.Kind.FIRE));
    FROST_SIGIL =
        register(
            "frost_sigil",
            MagicElement.FROST,
            SpellTier.APPRENTICE,
            10,
            20,
            new SigilExecutor(EntityMagicSigil.Kind.FROST));
    LIGHTNING_SIGIL =
        register(
            "lightning_sigil",
            MagicElement.LIGHTNING,
            SpellTier.APPRENTICE,
            10,
            20,
            new SigilExecutor(EntityMagicSigil.Kind.LIGHTNING));
    SNARE = register("snare", MagicElement.EARTH, SpellTier.BASIC, 10, 10, new SnareExecutor());
    ARC =
        register(
            "arc",
            MagicElement.LIGHTNING,
            SpellTier.BASIC,
            5,
            15,
            new LightningArcExecutor(LightningArcExecutor.Kind.ARC));
    CHAIN_LIGHTNING =
        register(
            "chain_lightning",
            MagicElement.LIGHTNING,
            SpellTier.ADVANCED,
            25,
            50,
            new LightningArcExecutor(LightningArcExecutor.Kind.CHAIN));
    LIGHTNING_BOLT =
        register(
            "lightning_bolt",
            MagicElement.LIGHTNING,
            SpellTier.ADVANCED,
            40,
            80,
            new LightningBoltExecutor());
    LIGHTNING_RAY =
        register(
            "lightning_ray",
            MagicElement.LIGHTNING,
            SpellTier.APPRENTICE,
            CastType.CONTINUOUS,
            5,
            0,
            new ChannelledLightningExecutor(ChannelledLightningExecutor.Kind.RAY));
    LIGHTNING_WEB =
        register(
            "lightning_web",
            MagicElement.LIGHTNING,
            SpellTier.MASTER,
            CastType.CONTINUOUS,
            15,
            0,
            new ChannelledLightningExecutor(ChannelledLightningExecutor.Kind.WEB));
    LIGHTNING_PULSE =
        register(
            "lightning_pulse",
            MagicElement.LIGHTNING,
            SpellTier.ADVANCED,
            25,
            75,
            new LightningPulseExecutor());
    FLAME_RAY =
        register(
            "flame_ray",
            MagicElement.FIRE,
            SpellTier.APPRENTICE,
            CastType.CONTINUOUS,
            5,
            0,
            new ElementalRayExecutor(ElementalRayExecutor.Kind.FLAME));
    FROST_RAY =
        register(
            "frost_ray",
            MagicElement.FROST,
            SpellTier.APPRENTICE,
            CastType.CONTINUOUS,
            5,
            0,
            new ElementalRayExecutor(ElementalRayExecutor.Kind.FROST));
    FIRESTORM =
        register(
            "firestorm",
            MagicElement.FIRE,
            SpellTier.MASTER,
            CastType.CONTINUOUS,
            15,
            0,
            new ElementalRayExecutor(ElementalRayExecutor.Kind.FIRESTORM));
    BLINK =
        register("blink", MagicElement.SORCERY, SpellTier.APPRENTICE, 15, 25, new BlinkExecutor());
    PHASE_STEP =
        register(
            "phase_step",
            MagicElement.SORCERY,
            SpellTier.ADVANCED,
            35,
            40,
            new PhaseStepExecutor());
    GLIDE =
        register(
            "glide",
            MagicElement.EARTH,
            SpellTier.ADVANCED,
            CastType.CONTINUOUS,
            5,
            0,
            new AerialMovementExecutor(AerialMovementExecutor.Kind.GLIDE));
    LEVITATION =
        register(
            "levitation",
            MagicElement.SORCERY,
            SpellTier.ADVANCED,
            CastType.CONTINUOUS,
            10,
            0,
            new AerialMovementExecutor(AerialMovementExecutor.Kind.LEVITATION));
    FLIGHT =
        register(
            "flight",
            MagicElement.EARTH,
            SpellTier.MASTER,
            CastType.CONTINUOUS,
            10,
            0,
            new AerialMovementExecutor(AerialMovementExecutor.Kind.FLIGHT));
    PETRIFY =
        register(
            "petrify", MagicElement.SORCERY, SpellTier.ADVANCED, 40, 100, new PetrifyExecutor());
    BLACK_HOLE =
        register(
            "black_hole",
            MagicElement.SORCERY,
            SpellTier.MASTER,
            150,
            400,
            new BlackHoleExecutor());
    DETONATE =
        register("detonate", MagicElement.FIRE, SpellTier.ADVANCED, 45, 50, new DetonateExecutor());
    POCKET_FURNACE =
        register(
            "pocket_furnace",
            MagicElement.FIRE,
            SpellTier.APPRENTICE,
            30,
            40,
            new PocketFurnaceExecutor());
    INVOKE_WEATHER =
        register(
            "invoke_weather",
            MagicElement.LIGHTNING,
            SpellTier.ADVANCED,
            30,
            100,
            new InvokeWeatherExecutor());
    COBWEBS =
        register("cobwebs", MagicElement.EARTH, SpellTier.ADVANCED, 30, 70, new CobwebsExecutor());
    WALL_OF_FROST =
        register(
            "wall_of_frost",
            MagicElement.FROST,
            SpellTier.MASTER,
            CastType.CONTINUOUS,
            15,
            0,
            new WallOfFrostExecutor());
    RING_OF_FIRE =
        register(
            "ring_of_fire",
            MagicElement.FIRE,
            SpellTier.ADVANCED,
            30,
            100,
            new RingOfFireExecutor());
    DECAY =
        register(
            "decay",
            MagicElement.NECROMANCY,
            SpellTier.ADVANCED,
            50,
            200,
            new DecayGroundExecutor());
    BUBBLE =
        register(
            "bubble",
            MagicElement.EARTH,
            SpellTier.APPRENTICE,
            15,
            20,
            new BubblePrisonExecutor(false));
    ENTRAPMENT =
        register(
            "entrapment",
            MagicElement.NECROMANCY,
            SpellTier.ADVANCED,
            35,
            75,
            new BubblePrisonExecutor(true));
    LIGHT = register("light", MagicElement.SORCERY, SpellTier.BASIC, 5, 15, new LightExecutor());
    GROWTH_AURA =
        register(
            "growth_aura",
            MagicElement.EARTH,
            SpellTier.APPRENTICE,
            20,
            50,
            new GrowthAuraExecutor());
    INVIGORATING_PRESENCE =
        register(
            "invigorating_presence",
            MagicElement.HEALING,
            SpellTier.APPRENTICE,
            30,
            60,
            new InvigoratingPresenceExecutor());
    SHADOW_WARD =
        register(
            "shadow_ward",
            MagicElement.NECROMANCY,
            SpellTier.ADVANCED,
            CastType.CONTINUOUS,
            EnumAction.block,
            10,
            0,
            new ShadowWardExecutor());
    SHOCKWAVE =
        register(
            "shockwave", MagicElement.SORCERY, SpellTier.MASTER, 65, 150, new ShockwaveExecutor());
    SHIELD =
        register(
            "shield",
            MagicElement.HEALING,
            SpellTier.APPRENTICE,
            CastType.CONTINUOUS,
            EnumAction.block,
            5,
            0,
            new ShieldExecutor());
    ARROW_RAIN =
        register(
            "arrow_rain",
            MagicElement.SORCERY,
            SpellTier.MASTER,
            75,
            300,
            new MagicStormExecutor(EntityMagicStorm.Kind.ARROW_RAIN));
    HAILSTORM =
        register(
            "hailstorm",
            MagicElement.FROST,
            SpellTier.MASTER,
            75,
            300,
            new MagicStormExecutor(EntityMagicStorm.Kind.HAILSTORM));
    BLIZZARD =
        register(
            "blizzard",
            MagicElement.FROST,
            SpellTier.ADVANCED,
            40,
            100,
            new MagicStormExecutor(EntityMagicStorm.Kind.BLIZZARD));
    ICE_SPIKES =
        register(
            "ice_spikes", MagicElement.FROST, SpellTier.ADVANCED, 30, 75, new IceSpikesExecutor());
    ICE_STATUE =
        register(
            "ice_statue",
            MagicElement.FROST,
            SpellTier.APPRENTICE,
            15,
            40,
            new IceStatueExecutor());
    ICE_AGE =
        register("ice_age", MagicElement.FROST, SpellTier.MASTER, 70, 250, new IceAgeExecutor());
    FORCEFIELD =
        register(
            "forcefield",
            MagicElement.HEALING,
            SpellTier.ADVANCED,
            45,
            200,
            new MagicAuraExecutor(EntityMagicAura.Kind.FORCEFIELD));
    HEALING_AURA =
        register(
            "healing_aura",
            MagicElement.HEALING,
            SpellTier.ADVANCED,
            35,
            150,
            new MagicAuraExecutor(EntityMagicAura.Kind.HEALING));
    THUNDERSTORM =
        register(
            "thunderstorm",
            MagicElement.LIGHTNING,
            SpellTier.MASTER,
            100,
            250,
            new ThunderstormExecutor());
    METEOR =
        register("meteor", MagicElement.FIRE, SpellTier.MASTER, 100, 200, new MeteorExecutor());
    CLAIRVOYANCE =
        register(
            "clairvoyance",
            MagicElement.SORCERY,
            SpellTier.APPRENTICE,
            CastType.INSTANT,
            EnumAction.bow,
            20,
            100,
            RemainingSpellExecutors.clairvoyance());
    CURSE_OF_SOULBINDING =
        register(
            "curse_of_soulbinding",
            MagicElement.NECROMANCY,
            SpellTier.ADVANCED,
            35,
            100,
            RemainingSpellExecutors.soulbinding());
    DECOY =
        register(
            "decoy",
            MagicElement.SORCERY,
            SpellTier.ADVANCED,
            CastType.INSTANT,
            EnumAction.bow,
            40,
            200,
            RemainingSpellExecutors.decoy());
    EARTHQUAKE =
        register(
            "earthquake",
            MagicElement.EARTH,
            SpellTier.MASTER,
            75,
            250,
            RemainingSpellExecutors.construct(EntityMagicConstruct.Kind.EARTHQUAKE));
    FORESTS_CURSE =
        register(
            "forests_curse",
            MagicElement.EARTH,
            SpellTier.MASTER,
            CastType.INSTANT,
            EnumAction.bow,
            75,
            200,
            RemainingSpellExecutors.forestsCurse());
    LIGHTNING_HAMMER =
        register(
            "lightning_hammer",
            MagicElement.LIGHTNING,
            SpellTier.MASTER,
            100,
            300,
            RemainingSpellExecutors.construct(EntityMagicConstruct.Kind.HAMMER));
    METAMORPHOSIS =
        register(
            "metamorphosis",
            MagicElement.NECROMANCY,
            SpellTier.APPRENTICE,
            15,
            30,
            RemainingSpellExecutors.metamorphosis());
    PLAGUE_OF_DARKNESS =
        register(
            "plague_of_darkness",
            MagicElement.NECROMANCY,
            SpellTier.MASTER,
            CastType.INSTANT,
            EnumAction.bow,
            75,
            200,
            RemainingSpellExecutors.plague());
    SPECTRAL_PATHWAY =
        register(
            "spectral_pathway",
            MagicElement.SORCERY,
            SpellTier.ADVANCED,
            CastType.INSTANT,
            EnumAction.bow,
            40,
            300,
            RemainingSpellExecutors.spectralPathway());
    TELEKINESIS =
        register(
            "telekinesis",
            MagicElement.SORCERY,
            SpellTier.BASIC,
            5,
            10,
            RemainingSpellExecutors.telekinesis());
    TORNADO =
        register(
            "tornado",
            MagicElement.EARTH,
            SpellTier.ADVANCED,
            35,
            80,
            RemainingSpellExecutors.construct(EntityMagicConstruct.Kind.TORNADO));
    SUMMON_SPIRIT_HORSE =
        register(
            "summon_spirit_horse",
            MagicElement.EARTH,
            SpellTier.ADVANCED,
            CastType.INSTANT,
            EnumAction.bow,
            50,
            150,
            new SummonSpiritHorseExecutor());
    SpellRegistry.freeze();
  }

  private static Spell register(
      String path,
      MagicElement element,
      SpellTier tier,
      int cost,
      int cooldown,
      ru.givler.mbo.magic.api.SpellExecutor executor) {
    return SpellRegistry.register(
        new Spell(
            SpellRegistry.id(path), element, tier, CastType.INSTANT, cost, cooldown, executor));
  }

  private static Spell register(
      String path,
      MagicElement element,
      SpellTier tier,
      CastType castType,
      int cost,
      int cooldown,
      ru.givler.mbo.magic.api.SpellExecutor executor) {
    return SpellRegistry.register(
        new Spell(SpellRegistry.id(path), element, tier, castType, cost, cooldown, executor));
  }

  private static Spell register(
      String path,
      MagicElement element,
      SpellTier tier,
      CastType castType,
      EnumAction action,
      int cost,
      int cooldown,
      ru.givler.mbo.magic.api.SpellExecutor executor) {
    return SpellRegistry.register(
        new Spell(
            SpellRegistry.id(path), element, tier, castType, cost, cooldown, executor, action));
  }
}
