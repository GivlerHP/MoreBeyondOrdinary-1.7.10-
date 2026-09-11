package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import ru.givler.mbo.dungeon.*;
import ru.givler.mbo.editor.AreaSelection;
import ru.givler.mbo.item.ItemDungeonEditor;
import ru.givler.mbo.network.EditorPacketAccess;
import ru.givler.mbo.registry.ItemRegistry;

public final class PacketDungeonTriggerSettings implements IMessage {
  private NBTTagCompound tag = new NBTTagCompound();

  public PacketDungeonTriggerSettings() {}

  public PacketDungeonTriggerSettings(NBTTagCompound tag) {
    this.tag = tag;
  }

  public void fromBytes(ByteBuf b) {
    NBTTagCompound t = ByteBufUtils.readTag(b);
    tag = t == null ? new NBTTagCompound() : t;
  }

  public void toBytes(ByteBuf b) {
    ByteBufUtils.writeTag(b, tag);
  }

  public static final class Handler
      implements IMessageHandler<PacketDungeonTriggerSettings, IMessage> {
    public IMessage onMessage(final PacketDungeonTriggerSettings m, MessageContext c) {
      final EntityPlayerMP p = c.getServerHandler().playerEntity;
      EditorPacketAccess.schedule(
          c,
          new Runnable() {
            public void run() {
              ItemStack held = EditorPacketAccess.heldEditor(p);
              if (held == null || held.getItem() != ItemRegistry.DungeonEditor) return;
              DungeonAreaSavedData data = DungeonAreaSavedData.get(p.worldObj);
              String id = m.tag.getString("Id");
              DungeonAreaRecord r = id.isEmpty() ? null : data.byId(id);
              if (r == null
                  && id.isEmpty()
                  && ItemDungeonEditor.hasCompleteSelection(held, p.dimension)) {
                AreaSelection s = AreaSelection.read(held, p.dimension);
                r = data.create(p.worldObj, p, s, DungeonAreaRecord.TRIGGER, 0, 10);
              }
              if (r == null || r.getType() != DungeonAreaRecord.TRIGGER || !r.isNear(p, 64D))
                return;
              String actions = m.tag.getString("Actions");
              if (actions.length()
                  > ru.givler.mbo.lootcontainer.LootContainerData.MAX_ACTIONS_JSON_LENGTH) return;
              try {
                if (ru.givler.mbo.lootcontainer.action.LootContainerAction.fromJsonList(actions)
                        .size()
                    > ru.givler.mbo.lootcontainer.LootContainerData.MAX_ACTIONS) return;
              } catch (Exception ex) {
                return;
              }
              r.configureTrigger(
                  m.tag.getInteger("RepeatMode"),
                  m.tag.getInteger("CooldownSeconds"),
                  m.tag.getBoolean("Players"),
                  m.tag.getBoolean("Entities"),
                  m.tag.getBoolean("Projectiles"),
                  m.tag.getBoolean("Explosions"),
                  m.tag.getBoolean("Multi"),
                  actions);
              data.changed(p.worldObj);
              p.addChatMessage(
                  new net.minecraft.util.ChatComponentTranslation("mbo.dungeon.area.saved"));
            }
          });
      return null;
    }
  }
}
