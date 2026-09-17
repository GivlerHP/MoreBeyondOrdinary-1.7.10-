package ru.givler.mbo.magic.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.magic.api.Spell;

/** Owns stable spell definitions. Registration is allowed only during startup. */
public final class SpellRegistry {
  private static final Map<String, Spell> SPELLS = new LinkedHashMap<String, Spell>();
  private static boolean frozen;

  private SpellRegistry() {}

  public static ResourceLocation id(String path) {
    return new ResourceLocation(MoreBeyondOrdinary.MODID, path);
  }

  public static synchronized Spell register(Spell spell) {
    if (frozen) throw new IllegalStateException("Spell registry is already frozen");
    String key = spell.id().toString();
    if (SPELLS.containsKey(key)) {
      throw new IllegalArgumentException("Duplicate spell id: " + key);
    }
    SPELLS.put(key, spell);
    return spell;
  }

  public static Spell find(ResourceLocation id) {
    return id == null ? null : SPELLS.get(id.toString());
  }

  public static List<Spell> values() {
    return Collections.unmodifiableList(new ArrayList<Spell>(SPELLS.values()));
  }

  public static synchronized void freeze() {
    frozen = true;
  }
}
