package ru.givler.mbo.client.lighting;

import java.lang.reflect.Method;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.lighting.ColoredLightApi;

/** Reads existing MF2/OptiFine light semantics without modifying MF2's transformer. */
public final class ColoredLightAuto {
  private static Method optifineItemLevel, mf2ItemLevel, baubles;
  private static Class<?> lanternType, battleInventoryType;
  private static Method lanternLevel, offhand, twoHanded;
  private static boolean resolved;
  private ColoredLightAuto() {}
  private static void resolve() {
    if (resolved) return; resolved = true;
    try { optifineItemLevel = Class.forName("DynamicLights").getMethod("getLightLevel", ItemStack.class); } catch (ReflectiveOperationException | LinkageError ignored) {}
    try { mf2ItemLevel = Class.forName("mods.battlegear2.client.utils.DynamicLightHook").getDeclaredMethod("getStackLightLevel", ItemStack.class); mf2ItemLevel.setAccessible(true); } catch (ReflectiveOperationException | LinkageError ignored) {}
    try {
      lanternType = Class.forName("minefantasy.mf2.item.custom.ItemLanternMF");
      lanternLevel = lanternType.getMethod("getLightLevel", ItemStack.class);
      battleInventoryType = Class.forName("mods.battlegear2.api.core.InventoryPlayerBattle");
      offhand = battleInventoryType.getMethod("getCurrentOffhandWeapon");
      twoHanded = Class.forName("mods.battlegear2.api.core.BattlegearUtils").getMethod("isMainHandTwoHanded", EntityPlayer.class);
    } catch (ReflectiveOperationException | LinkageError ignored) {}
    try { baubles = Class.forName("baubles.api.BaublesApi").getMethod("getBaubles", EntityPlayer.class); } catch (ReflectiveOperationException | LinkageError ignored) {}
  }
  public static int itemLevel(ItemStack stack, Entity holder) {
    if (stack == null) return 0;
    resolve();
    if (lanternType != null && lanternLevel != null && lanternType.isInstance(stack.getItem())) return invokeLevel(lanternLevel, stack);
    int level = 0;
    level = Math.max(invokeLevel(optifineItemLevel, stack), invokeLevel(mf2ItemLevel, stack));
    if (stack.getItem() instanceof ItemBlock) {
      Block block = Block.getBlockFromItem(stack.getItem());
      level = Math.max(level, block.getLightValue(new StackAccess(block, stack.getItemDamage() & 15), 0, 64, 0));
    }
    return level > 0 ? Math.min(15, level) : stack.getItem() instanceof ItemBlock ? 0 : 15;
  }
  public static int heldEmission(EntityLivingBase entity) {
    int packed = ColoredLightApi.itemEmission(entity.getHeldItem(), entity);
    if (entity instanceof EntityPlayer) {
      EntityPlayer player = (EntityPlayer) entity;
      resolve();
      if (battleInventoryType != null && offhand != null && twoHanded != null && battleInventoryType.isInstance(player.inventory)) try {
        if (!Boolean.TRUE.equals(twoHanded.invoke(null,player)))
          packed = merge(packed, ColoredLightApi.itemEmission((ItemStack) offhand.invoke(player.inventory), player));
      } catch (ReflectiveOperationException | LinkageError ignored) {}
      if (baubles != null) try {
        Object inventory = baubles.invoke(null, player);
        if (inventory instanceof IInventory) for (int i = 0; i < ((IInventory) inventory).getSizeInventory(); i++)
          packed = merge(packed, ColoredLightApi.itemEmission(((IInventory) inventory).getStackInSlot(i), player));
      } catch (ReflectiveOperationException | LinkageError ignored) {}
    }
    return packed;
  }
  private static int invokeLevel(Method method, ItemStack stack) {
    if (method == null) return 0;
    try { return ((Number) method.invoke(null, stack)).intValue(); } catch (ReflectiveOperationException | LinkageError ignored) { return 0; }
  }
  private static final class StackAccess implements net.minecraft.world.IBlockAccess {
    final Block block; final int metadata;
    StackAccess(Block block, int metadata) { this.block = block; this.metadata = metadata; }
    @Override public Block getBlock(int x, int y, int z) { return block; }
    @Override public int getBlockMetadata(int x, int y, int z) { return metadata; }
    @Override public net.minecraft.tileentity.TileEntity getTileEntity(int x, int y, int z) { return null; }
    @Override public int getLightBrightnessForSkyBlocks(int x, int y, int z, int minimum) { return minimum << 4; }
    @Override public boolean isAirBlock(int x, int y, int z) { return block.getMaterial() == net.minecraft.block.material.Material.air; }
    @Override public net.minecraft.world.biome.BiomeGenBase getBiomeGenForCoords(int x, int z) { return net.minecraft.world.biome.BiomeGenBase.plains; }
    @Override public int getHeight() { return 256; }
    @Override public int isBlockProvidingPowerTo(int x, int y, int z, int side) { return 0; }
    @Override public boolean extendedLevelsInChunkCache() { return false; }
    @Override public boolean isSideSolid(int x, int y, int z, net.minecraftforge.common.util.ForgeDirection side, boolean fallback) { return fallback; }
  }
  public static int merge(int a, int b) {
    return ColoredLightApi.emission(ru.givler.mbo.lighting.ColoredLightVolume.merge(ColoredLightApi.rgb(a), ColoredLightApi.rgb(b)), Math.max(a >>> 24, b >>> 24));
  }
  /** OptiFine remains responsible for the white dynamic level; preserve stronger existing light. */
  public static int optifineLevel(int original, Entity entity) {
    if (!ru.givler.mbo.config.ColoredLightConfig.enabled || !ru.givler.mbo.config.ColoredLightConfig.dynamicLights) return original;
    int packed = ColoredLightApi.entityEmission(entity);
    if (entity instanceof EntityLivingBase) packed = merge(packed, heldEmission((EntityLivingBase) entity));
    if (entity instanceof net.minecraft.entity.item.EntityItem) packed = merge(packed, ColoredLightApi.itemEmission(((net.minecraft.entity.item.EntityItem) entity).getEntityItem(), entity));
    return Math.max(original, packed >>> 24);
  }
}
