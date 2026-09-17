package ru.givler.mbo.magic.item;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import ru.givler.mbo.potion.SyncedPotionEffects;
import ru.givler.mbo.registry.PotionRegistry;

/** Registry shared by spectral melee weapons and spectral arrows. */
public final class SpectralEffects {
  private static final Map<String, SpectralImpactEffect> EFFECTS =
      new HashMap<String, SpectralImpactEffect>();

  public static final SpectralImpactEffect NONE =
      register(
          new SimpleEffect("none") {
            @Override
            public void apply(EntityLivingBase attacker, EntityLivingBase target) {}
          });

  public static final SpectralImpactEffect IGNITE =
      register(
          new SimpleEffect("ignite") {
            @Override
            public void apply(EntityLivingBase attacker, EntityLivingBase target) {
              target.setFire(4);
            }
          });

  public static final SpectralImpactEffect AXE_IGNITE =
      register(
          new SimpleEffect("axe_ignite") {
            @Override
            public void apply(EntityLivingBase attacker, EntityLivingBase target) {
              target.setFire(8);
            }
          });

  public static final SpectralImpactEffect FREEZE =
      register(
          new SimpleEffect("freeze") {
            @Override
            public void apply(EntityLivingBase attacker, EntityLivingBase target) {
              SyncedPotionEffects.apply(
                  target, new PotionEffect(PotionRegistry.Frost.id, 160, 1, true));
            }
          });

  private SpectralEffects() {}

  public static <T extends SpectralImpactEffect> T register(T effect) {
    if (effect == null || effect.getId() == null || effect.getId().isEmpty()) {
      throw new IllegalArgumentException("A spectral effect must have an id");
    }
    if (EFFECTS.containsKey(effect.getId())) {
      throw new IllegalArgumentException("Duplicate spectral effect: " + effect.getId());
    }
    EFFECTS.put(effect.getId(), effect);
    return effect;
  }

  public static SpectralImpactEffect get(String id) {
    SpectralImpactEffect effect = EFFECTS.get(id);
    return effect == null ? NONE : effect;
  }

  private abstract static class SimpleEffect implements SpectralImpactEffect {
    private final String id;

    private SimpleEffect(String id) {
      this.id = id;
    }

    @Override
    public String getId() {
      return id;
    }
  }
}
