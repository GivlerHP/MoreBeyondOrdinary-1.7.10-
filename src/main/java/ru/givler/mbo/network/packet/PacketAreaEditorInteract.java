package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.item.ItemDungeonEditor;
import ru.givler.mbo.item.ItemPlatformEditor;
import ru.givler.mbo.movingplatform.EntityMovingPlatform;
import ru.givler.mbo.network.EditorPacketAccess;
import ru.givler.mbo.network.PacketManager;

public final class PacketAreaEditorInteract implements IMessage {
  public static final int FIRST = 0, SECOND = 1, OPEN = 2, SAMPLE = 3, OPEN_ENTITY = 4;
  private int action, x, y, z;

  public PacketAreaEditorInteract() {}

  public PacketAreaEditorInteract(int action, int x, int y, int z) {
    this.action = action;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  @Override
  public void fromBytes(ByteBuf b) {
    action = b.readByte();
    x = b.readInt();
    y = b.readInt();
    z = b.readInt();
  }

  @Override
  public void toBytes(ByteBuf b) {
    b.writeByte(action);
    b.writeInt(x);
    b.writeInt(y);
    b.writeInt(z);
  }

  public static final class Handler implements IMessageHandler<PacketAreaEditorInteract, IMessage> {
    @Override
    public IMessage onMessage(final PacketAreaEditorInteract m, MessageContext c) {
      final EntityPlayerMP p = c.getServerHandler().playerEntity;
      EditorPacketAccess.schedule(
          c,
          new Runnable() {
            @Override
            public void run() {
              process(m, p);
            }
          });
      return null;
    }

    private static void process(PacketAreaEditorInteract m, EntityPlayerMP p) {
      ItemStack held = EditorPacketAccess.heldEditor(p);
      if (held == null) return;
      if (held.getItem() instanceof ItemPlatformEditor) {
        ItemPlatformEditor editor = (ItemPlatformEditor) held.getItem();
        if (m.action == OPEN_ENTITY) {
          Entity e = p.worldObj.getEntityByID(m.x);
          if (e instanceof EntityMovingPlatform && p.getDistanceSqToEntity(e) <= 64D)
            ItemPlatformEditor.open(p, (EntityMovingPlatform) e, held);
        } else if (m.action == FIRST) editor.selectFirst(held, m.x, m.y, m.z, p);
        else editor.selectSecondOrOpen(held, p, p.worldObj, m.x, m.y, m.z, m.action == OPEN);
      } else if (held.getItem() instanceof ItemDungeonEditor) {
        ItemDungeonEditor editor = (ItemDungeonEditor) held.getItem();
        if (m.action == OPEN) {
          if (ItemDungeonEditor.hasCompleteSelection(held, p.dimension))
            PacketManager.INSTANCE.sendTo(
                new PacketDungeonEditorOpen(
                    "",
                    held.hasTagCompound() ? held.getTagCompound().getInteger("AreaType") : 0,
                    held.hasTagCompound() ? held.getTagCompound().getInteger("RestoreMode") : 0,
                    held.hasTagCompound() && held.getTagCompound().hasKey("RestoreSeconds")
                        ? held.getTagCompound().getInteger("RestoreSeconds")
                        : 10),
                p);
          else
            p.addChatMessage(
                new net.minecraft.util.ChatComponentTranslation("mbo.dungeon.settings.incomplete"));
        } else editor.setPoint(held, p, m.action == FIRST ? "Pos1" : "Pos2", m.x, m.y, m.z);
      }
      p.inventory.markDirty();
    }
  }
}
