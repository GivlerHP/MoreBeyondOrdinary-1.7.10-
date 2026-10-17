package ru.givler.mbo.lighting;
import net.minecraft.client.settings.KeyBinding;
import ru.givler.mbo.client.gui.KeyBindingListSizing;
public final class ControlsSizingSmoke {
  public static void run() {
    KeyBinding[] bindings={new OrderedKey("4","cat.b"),new OrderedKey("1","cat.a"),new OrderedKey("3","cat.a"),new OrderedKey("2","cat.b")};
    KeyBinding first=bindings[0];
    if(KeyBindingListSizing.entries(bindings)!=8)throw new AssertionError("Repeated translated-category runs underallocated");
    if(bindings[0]!=first)throw new AssertionError("Settings bindings reordered");
    if(KeyBindingListSizing.entries(new KeyBinding[0])!=0)throw new AssertionError("Empty bindings");
    KeyBinding[] one={new OrderedKey("6","cat.a"),new OrderedKey("5","cat.a")};
    if(KeyBindingListSizing.entries(one)!=3)throw new AssertionError("Ordinary category size");
    System.out.println("Controls list: interleaved categories size exactly, empty/ordinary lists and settings order preserved");
  }
  static final class OrderedKey extends KeyBinding {
    OrderedKey(String name,String category){super(name,0,category);}
    @Override public int compareTo(KeyBinding other){return getKeyDescription().compareTo(other.getKeyDescription());}
  }
}
