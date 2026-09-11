package ru.givler.mbo.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import ru.givler.mbo.editor.AreaSelection;
import ru.givler.mbo.movingplatform.EntityMovingPlatform;
import ru.givler.mbo.movingplatform.PlatformAccess;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketPlatformOpen;

public class ItemPlatformEditor extends ItemAreaEditor {
  public ItemPlatformEditor() {
    super("PlatformEditor");
  }

  public void selectFirst(ItemStack stack, int x, int y, int z, EntityPlayer player) {
    if (!PlatformAccess.canEdit(player)) return;
    selectPoint(stack, player, AreaSelection.FIRST, x, y, z, 4096L, "mbo.platform.pos1");
  }

  public void selectSecondOrOpen(
      ItemStack stack, EntityPlayer player, World world, int x, int y, int z) {
    selectSecondOrOpen(stack, player, world, x, y, z, player.isSneaking());
  }

  public void selectSecondOrOpen(
      ItemStack stack, EntityPlayer player, World world, int x, int y, int z, boolean open) {
    if (!PlatformAccess.canEdit(player)) return;
    if (open) {
      if (!world.isRemote) openOrCreate(stack, player, world, x, y, z);
      return;
    }
    selectPoint(stack, player, AreaSelection.SECOND, x, y, z, 4096L, "mbo.platform.pos2");
  }

  @SuppressWarnings("unchecked")
  private void openOrCreate(
      ItemStack stack, EntityPlayer player, World world, int x, int y, int z) {
    java.util.UUID linked = getLinkedPlatform(stack);
    if (linked != null)
      for (Object object : new java.util.ArrayList(world.loadedEntityList)) {
        if (object instanceof EntityMovingPlatform
            && linked.equals(((EntityMovingPlatform) object).getPlatformId())) {
          open(player, (EntityMovingPlatform) object, stack);
          return;
        }
      }
    for (Object object : new java.util.ArrayList(world.loadedEntityList)) {
      if (!(object instanceof EntityMovingPlatform)) continue;
      EntityMovingPlatform platform = (EntityMovingPlatform) object;
      if (platform.contains(x, y, z)) {
        open(player, platform, stack);
        return;
      }
    }
    int[] a = getPoint(stack, "Pos1", world.provider.dimensionId),
        b = getPoint(stack, "Pos2", world.provider.dimensionId);
    if (a == null || b == null) {
      player.addChatMessage(new ChatComponentTranslation("mbo.platform.error.selection"));
      return;
    }
    int minX = Math.min(a[0], b[0]), minY = Math.min(a[1], b[1]), minZ = Math.min(a[2], b[2]);
    int maxX = Math.max(a[0], b[0]), maxY = Math.max(a[1], b[1]), maxZ = Math.max(a[2], b[2]);
    if ((long) (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1) > 4096L) {
      player.addChatMessage(new ChatComponentTranslation("mbo.platform.error.tooLarge"));
      return;
    }
    EntityMovingPlatform platform =
        EntityMovingPlatform.create(world, player, minX, minY, minZ, maxX, maxY, maxZ);
    if (platform != null && world.spawnEntityInWorld(platform)) open(player, platform, stack);
  }

  public static void open(EntityPlayer player, EntityMovingPlatform platform, ItemStack stack) {
    if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
    stack.getTagCompound().setString("PlatformId", platform.getPlatformId().toString());
    if (player instanceof net.minecraft.entity.player.EntityPlayerMP)
      PacketManager.INSTANCE.sendTo(
          new PacketPlatformOpen(platform), (net.minecraft.entity.player.EntityPlayerMP) player);
  }

  public static int[] getPoint(ItemStack stack, String key, int dimension) {
    return AreaSelection.getPoint(stack, key, dimension);
  }

  public static java.util.UUID getLinkedPlatform(ItemStack stack) {
    if (stack == null || !stack.hasTagCompound() || !stack.getTagCompound().hasKey("PlatformId"))
      return null;
    try {
      return java.util.UUID.fromString(stack.getTagCompound().getString("PlatformId"));
    } catch (Exception ignored) {
      return null;
    }
  }

  @Override
  protected void beforePointStored(ItemStack stack, String key) {
    if (AreaSelection.FIRST.equals(key) && stack.hasTagCompound())
      stack.getTagCompound().removeTag("PlatformId");
  }

  @Override
  protected String tooLargeMessageKey() {
    return "mbo.platform.error.tooLarge";
  }

  @Override
  public int selectionColor() {
    return 0x33CCFFFF;
  }
}
