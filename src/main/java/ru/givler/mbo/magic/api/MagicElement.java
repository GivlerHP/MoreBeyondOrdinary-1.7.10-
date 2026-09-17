package ru.givler.mbo.magic.api;

/** Gameplay classification used by spells, damage and resistances. */
public enum MagicElement {
  ARCANE("\u00a77"),
  FIRE("\u00a74"),
  FROST("\u00a7b"),
  LIGHTNING("\u00a73"),
  NECROMANCY("\u00a75"),
  EARTH("\u00a72"),
  SORCERY("\u00a7a"),
  HEALING("\u00a7e");

  private final String color;

  MagicElement(String color) {
    this.color = color;
  }

  public String color() {
    return color;
  }
}
