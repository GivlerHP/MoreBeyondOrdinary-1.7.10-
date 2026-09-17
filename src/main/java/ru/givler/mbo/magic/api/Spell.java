package ru.givler.mbo.magic.api;

import net.minecraft.item.EnumAction;
import net.minecraft.util.ResourceLocation;

/** Immutable definition registered under a stable MBO resource identifier. */
public final class Spell {
  private final ResourceLocation id;
  private final MagicElement element;
  private final CastType castType;
  private final SpellTier tier;
  private final int baseCost;
  private final int cooldownTicks;
  private final SpellExecutor executor;
  private final EnumAction useAction;

  public Spell(
      ResourceLocation id,
      MagicElement element,
      SpellTier tier,
      CastType castType,
      int baseCost,
      int cooldownTicks,
      SpellExecutor executor) {
    this(
        id,
        element,
        tier,
        castType,
        baseCost,
        cooldownTicks,
        executor,
        castType == CastType.CONTINUOUS ? EnumAction.bow : EnumAction.none);
  }

  public Spell(
      ResourceLocation id,
      MagicElement element,
      SpellTier tier,
      CastType castType,
      int baseCost,
      int cooldownTicks,
      SpellExecutor executor,
      EnumAction useAction) {
    if (id == null) throw new IllegalArgumentException("id must not be null");
    if (element == null) throw new IllegalArgumentException("element must not be null");
    if (castType == null) throw new IllegalArgumentException("castType must not be null");
    if (tier == null) throw new IllegalArgumentException("tier must not be null");
    if (executor == null) throw new IllegalArgumentException("executor must not be null");
    if (baseCost < 0) throw new IllegalArgumentException("baseCost must not be negative");
    if (cooldownTicks < 0) throw new IllegalArgumentException("cooldownTicks must not be negative");
    this.id = id;
    this.element = element;
    this.tier = tier;
    this.castType = castType;
    this.baseCost = baseCost;
    this.cooldownTicks = cooldownTicks;
    this.executor = executor;
    this.useAction = useAction == null ? EnumAction.none : useAction;
  }

  public ResourceLocation id() {
    return id;
  }

  public MagicElement element() {
    return element;
  }

  public CastType castType() {
    return castType;
  }

  public SpellTier tier() {
    return tier;
  }

  public String nameKey() {
    return "mbo.spell." + id.getResourcePath();
  }

  public String descriptionKey() {
    return nameKey() + ".desc";
  }

  public int baseCost() {
    return baseCost;
  }

  public int cooldownTicks() {
    return cooldownTicks;
  }

  public EnumAction useAction() {
    return useAction;
  }

  public SpellResult cast(SpellContext context) {
    return executor.cast(context);
  }
}
