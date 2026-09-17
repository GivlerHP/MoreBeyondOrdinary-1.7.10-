package ru.givler.mbo.magic.api;

/** Stateless gameplay operation behind a registered spell definition. */
public interface SpellExecutor {
  SpellResult cast(SpellContext context);
}
