package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.dungeon.DungeonAreaRecord;
import ru.givler.mbo.dungeon.DungeonAreaSavedData;
import ru.givler.mbo.editor.AreaSelection;
import ru.givler.mbo.item.ItemDungeonEditor;
import ru.givler.mbo.network.EditorPacketAccess;
import ru.givler.mbo.registry.ItemRegistry;

public final class PacketDungeonEditorSettings implements IMessage {
  private String areaId = "";
  private int type, restoreMode, restoreSeconds;

  public PacketDungeonEditorSettings() {}

  public PacketDungeonEditorSettings(int type, int restoreMode, int restoreSeconds) {
    this.type = type;
    this.restoreMode = restoreMode;
    this.restoreSeconds = restoreSeconds;
  }

  public PacketDungeonEditorSettings(String areaId, int type, int restoreMode, int restoreSeconds) {
    this.areaId = areaId == null ? "" : areaId;
    this.type = type;
    this.restoreMode = restoreMode;
    this.restoreSeconds = restoreSeconds;
  }

  @Override
  public void fromBytes(ByteBuf b) {
    int n = b.readUnsignedShort();
    areaId = b.readBytes(n).toString(io.netty.util.CharsetUtil.UTF_8);
    type = b.readByte();
    restoreMode = b.readByte();
    restoreSeconds = b.readInt();
  }

  @Override
  public void toBytes(ByteBuf b) {
    byte[] v = areaId.getBytes(io.netty.util.CharsetUtil.UTF_8);
    b.writeShort(v.length);
    b.writeBytes(v);
    b.writeByte(type);
    b.writeByte(restoreMode);
    b.writeInt(restoreSeconds);
  }

  public static final class Handler
      implements IMessageHandler<PacketDungeonEditorSettings, IMessage> {
    @Override
    public IMessage onMessage(final PacketDungeonEditorSettings m, MessageContext c) {
      final EntityPlayerMP p = c.getServerHandler().playerEntity;
      EditorPacketAccess.schedule(
          c,
          new Runnable() {
            @Override
            public void run() {
              apply(m, p);
            }
          });
      return null;
    }

    private static void apply(PacketDungeonEditorSettings m, EntityPlayerMP p) {
      ItemStack held = EditorPacketAccess.heldEditor(p);
      if (held == null || held.getItem() != ItemRegistry.DungeonEditor) return;
      int type = Math.max(0, Math.min(3, m.type)),
          restoreMode = Math.max(0, Math.min(2, m.restoreMode)),
          restoreSeconds = Math.max(1, Math.min(86400, m.restoreSeconds));
      DungeonAreaSavedData data = DungeonAreaSavedData.get(p.worldObj);
      if (!m.areaId.isEmpty()) {
        DungeonAreaRecord r = data.byId(m.areaId);
        if (r != null && r.isNear(p, 64D)) {
          r.configure(type, restoreMode, restoreSeconds);
          data.changed(p.worldObj);
          p.addChatMessage(
              new net.minecraft.util.ChatComponentTranslation("mbo.dungeon.area.updated"));
        }
        return;
      }
      if (!ItemDungeonEditor.hasCompleteSelection(held, p.dimension) || !held.hasTagCompound())
        return;
      AreaSelection selection = AreaSelection.read(held, p.dimension);
      if (data.create(p.worldObj, p, selection, type, restoreMode, restoreSeconds) == null) return;
      held.getTagCompound().setInteger("AreaType", type);
      held.getTagCompound().setInteger("RestoreMode", restoreMode);
      held.getTagCompound().setInteger("RestoreSeconds", restoreSeconds);
      p.inventory.markDirty();
    }
  }
}
