package ru.givler.mbo.lighting;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.block.BlockColourFire;
import ru.givler.mbo.registry.BlockRegistry;
import ru.givler.mbo.config.ColoredLightSources;

/** Register block providers during initialization. TileEntities can implement Emitter directly. */
public final class ColoredLightApi {
  public interface Provider {
    /** Packed level/RGB from emission(), or zero for an inactive source. */
    int getEmission(IBlockAccess world, int x, int y, int z);
  }
  public interface Emitter {
    int getColoredLightEmission();
  }
  public interface ItemProvider { int getEmission(ItemStack stack, Entity holder); }
  public interface EntityProvider { int getEmission(Entity entity); }
  private static volatile Map<Item, ItemProvider> ITEMS = new IdentityHashMap<>();
  private static volatile Map<Class<?>, EntityProvider> ENTITIES = new IdentityHashMap<>();
  private static volatile Map<Block, Provider> PROVIDERS = new IdentityHashMap<>();
  private ColoredLightApi() {}

  public static synchronized void register(Block block, Provider provider) {
    if (block == null || provider == null) throw new IllegalArgumentException("Block and provider required");
    Map<Block, Provider> next = new IdentityHashMap<>(PROVIDERS); next.put(block, provider); PROVIDERS = next;
  }

  public static int emission(int rgb, int level) {
    if (level <= 0 || (rgb & 0xFFFFFF) == 0) return 0;
    return Math.min(15, level) << 24 | rgb & 0xFFFFFF;
  }

  public static synchronized void registerItem(Item item, ItemProvider provider) {
    if (item == null || provider == null) throw new IllegalArgumentException("Item and provider required");
    Map<Item, ItemProvider> next = new IdentityHashMap<>(ITEMS); next.put(item, provider); ITEMS = next;
  }
  public static synchronized void registerEntity(Class<? extends Entity> type, EntityProvider provider) {
    if (type == null || provider == null) throw new IllegalArgumentException("Entity type and provider required");
    Map<Class<?>, EntityProvider> next = new IdentityHashMap<>(ENTITIES); next.put(type, provider); ENTITIES = next;
  }
  public static int itemEmission(ItemStack stack, Entity holder) {
    if (stack == null) return 0;
    ColoredLightRule rule = ColoredLightSources.item(stack);
    Block block = stack.getItem() instanceof ItemBlock ? Block.getBlockFromItem(stack.getItem()) : null;
    if (rule == null && block != null) rule = ColoredLightSources.block(block, stack.getItemDamage() & 15);
    if (rule != null) {
      int level = rule.brightness < 0 ? ru.givler.mbo.client.lighting.ColoredLightAuto.itemLevel(stack, holder) : 0;
      return rule.emission(level);
    }
    ItemProvider provider = ITEMS.get(stack.getItem());
    if (provider != null) return provider.getEmission(stack, holder);
    if (block == BlockRegistry.ColourFire) return emission(BlockColourFire.COLORS[stack.getItemDamage() & 15], 15);
    return 0;
  }
  public static int entityEmission(Entity entity) {
    String name = entityId(entity);
    if (name != null) {
      ColoredLightRule rule = ColoredLightSources.entity(name);
      if (rule != null) return rule.emission(15);
    }
    for (Class<?> type = entity.getClass(); type != null; type = type.getSuperclass()) {
      EntityProvider provider = ENTITIES.get(type); if (provider != null) return provider.getEmission(entity);
    }
    return entity instanceof Emitter ? ((Emitter) entity).getColoredLightEmission() : 0;
  }
  public static String entityId(Entity entity) {
    String name = EntityList.getEntityString(entity);
    if (name == null) return null;
    if (name.indexOf(':') < 0) { int dot = name.indexOf('.'); name = dot < 0 ? "minecraft:" + name : name.substring(0, dot) + ":" + name.substring(dot + 1); }
    return name.toLowerCase(java.util.Locale.ROOT);
  }

  public static int sample(Block block, IBlockAccess world, int x, int y, int z) {
    Provider provider = PROVIDERS.get(block);
    int packed = 0;
    ColoredLightRule rule = ColoredLightSources.block(block, world.getBlockMetadata(x, y, z));
    if (rule != null) packed = rule.emission(rule.brightness < 0 ? block.getLightValue(world, x, y, z) : 0);
    else if (provider != null) packed = provider.getEmission(world, x, y, z);
    else if (block == BlockRegistry.ColourFire) {
      packed = emission(BlockColourFire.COLORS[world.getBlockMetadata(x, y, z) & 15], 15);
    } else if (block.hasTileEntity(world.getBlockMetadata(x, y, z))) {
      TileEntity tile = world.getTileEntity(x, y, z);
      if (tile instanceof Emitter) packed = ((Emitter) tile).getColoredLightEmission();
    }
    return rgb(packed);
  }

  public static int rgb(int packed) {
    int level = Math.min(15, packed >>> 24);
    int r = packed >>> 16 & 255, g = packed >>> 8 & 255, b = packed & 255;
    int maximum = Math.max(r, Math.max(g, b));
    if (maximum == 0 || level == 0) return 0;
    // Palette brightness is not light strength: every admin-fire hue has level 15.
    return (r * 255 * level / (maximum * 15)) << 16
        | (g * 255 * level / (maximum * 15)) << 8 | b * 255 * level / (maximum * 15);
  }
}
