package ru.givler.mbo.integration.angelica.client.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/** Retain only previously drawn objects which are still independently eligible to render. */
public final class VisibleTileEntityHistory<T> {
  private final List<T> previous = new ArrayList<T>();
  private final Set<T> present = Collections.newSetFromMap(new IdentityHashMap<T,Boolean>());
  private Object owner;
  public void clear() { previous.clear(); present.clear(); owner=null; }
  public int complete(Object currentOwner, List<T> frame, Predicate<T> eligible) {
    if (owner != currentOwner) { previous.clear(); owner = currentOwner; }
    if (currentOwner == null) { present.clear(); return 0; }
    int restored = 0;
    try {
      present.addAll(frame);
      for (T value : previous) if (!present.contains(value) && eligible.test(value)) {
        frame.add(value); present.add(value); restored++;
      }
      previous.clear(); previous.addAll(frame);
      return restored;
    } finally { present.clear(); }
  }
}
