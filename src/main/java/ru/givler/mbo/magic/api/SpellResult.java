package ru.givler.mbo.magic.api;

/** Result used by the caller to decide whether resources and cooldown are consumed. */
public enum SpellResult {
  SUCCESS(true),
  PASS(false),
  BLOCKED(false),
  ON_COOLDOWN(false),
  INVALID(false);

  private final boolean consumesResources;

  SpellResult(boolean consumesResources) {
    this.consumesResources = consumesResources;
  }

  public boolean consumesResources() {
    return consumesResources;
  }
}
