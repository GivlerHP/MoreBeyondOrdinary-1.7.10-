package ru.givler.mbo.magic.possession;

/** Access to native protected movement rules, injected into EntityLivingBase. */
public interface PossessionAnimal {
  boolean mbo$movementBlocked();
  void mbo$jump();
}
