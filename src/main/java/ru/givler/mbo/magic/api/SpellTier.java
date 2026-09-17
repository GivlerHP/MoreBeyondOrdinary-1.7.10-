package ru.givler.mbo.magic.api;

/** Progression tier displayed by MBO spell containers. */
public enum SpellTier {
  BASIC("\u00a7f"),
  APPRENTICE("\u00a7b"),
  ADVANCED("\u00a71"),
  MASTER("\u00a75");

  private final String color;

  SpellTier(String color) {
    this.color = color;
  }

  public String translationKey() {
    return "mbo.spellTier." + name().toLowerCase();
  }

  public String color() {
    return color;
  }
}
