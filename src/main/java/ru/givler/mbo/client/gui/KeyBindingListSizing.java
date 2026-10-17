package ru.givler.mbo.client.gui;
import java.util.Arrays;
import net.minecraft.client.settings.KeyBinding;
/** Count actual category runs: distinct category keys can have identical translated names. */
public final class KeyBindingListSizing {
  private KeyBindingListSizing() {}
  public static int entries(KeyBinding[] bindings) {
    KeyBinding[] sorted=bindings.clone();Arrays.sort(sorted);
    int count=sorted.length;String previous=null;
    for(KeyBinding binding:sorted) {
      String category=binding.getKeyCategory();
      if(!category.equals(previous)){count++;previous=category;}
    }
    return count;
  }
}
