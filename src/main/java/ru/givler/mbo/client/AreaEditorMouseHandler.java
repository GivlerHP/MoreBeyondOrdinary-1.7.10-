package ru.givler.mbo.client;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.MouseEvent;
import ru.givler.mbo.dungeon.ClientDungeonAreas;
import ru.givler.mbo.dungeon.DungeonAreaRecord;
import ru.givler.mbo.editor.BuilderAccess;
import ru.givler.mbo.item.ItemAreaEditor;
import ru.givler.mbo.item.ItemDungeonEditor;
import ru.givler.mbo.item.ItemPlatformEditor;
import ru.givler.mbo.movingplatform.EntityMovingPlatform;
import ru.givler.mbo.movingplatform.PlatformBlock;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketAreaEditorInteract;
import ru.givler.mbo.network.packet.PacketDungeonAreaOpenRequest;

public final class AreaEditorMouseHandler {
  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onMouse(MouseEvent event) {
    if (event == null || !event.buttonstate) return;
    Minecraft mc = Minecraft.getMinecraft();
    if (mc == null || mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null)
      return;
    ItemStack held = mc.thePlayer.getCurrentEquippedItem();
    if (held == null
        || !(held.getItem() instanceof ItemAreaEditor)
        || !BuilderAccess.canUseTool(mc.thePlayer, held.getItem())) return;
    int key = event.button - 100;
    boolean attack = mc.gameSettings.keyBindAttack.getKeyCode() == key,
        use = mc.gameSettings.keyBindUseItem.getKeyCode() == key;
    if (!attack && !use) return;
    if (held.getItem() instanceof ItemPlatformEditor && use && mc.thePlayer.isSneaking()) {
      EntityMovingPlatform target = findPlatform(mc);
      if (target != null) {
        event.setCanceled(true);
        PacketManager.INSTANCE.sendToServer(
            new PacketAreaEditorInteract(
                PacketAreaEditorInteract.OPEN_ENTITY, target.getEntityId(), 0, 0));
        return;
      }
    }
    if (held.getItem() instanceof ItemDungeonEditor && use && mc.thePlayer.isSneaking()) {
      DungeonAreaRecord target = findDungeonArea(mc);
      if (target != null) {
        event.setCanceled(true);
        PacketManager.INSTANCE.sendToServer(new PacketDungeonAreaOpenRequest(target.idString()));
        return;
      }
    }
    MovingObjectPosition hit = mc.objectMouseOver;
    if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;
    event.setCanceled(true);
    if (held.getItem() instanceof ItemPlatformEditor) {
      ItemPlatformEditor editor = (ItemPlatformEditor) held.getItem();
      if (attack) {
        editor.selectFirst(held, hit.blockX, hit.blockY, hit.blockZ, mc.thePlayer);
        PacketManager.INSTANCE.sendToServer(
            new PacketAreaEditorInteract(
                PacketAreaEditorInteract.FIRST, hit.blockX, hit.blockY, hit.blockZ));
      } else {
        boolean open = mc.thePlayer.isSneaking();
        if (!open)
          editor.selectSecondOrOpen(
              held, mc.thePlayer, mc.theWorld, hit.blockX, hit.blockY, hit.blockZ, false);
        PacketManager.INSTANCE.sendToServer(
            new PacketAreaEditorInteract(
                open ? PacketAreaEditorInteract.OPEN : PacketAreaEditorInteract.SECOND,
                hit.blockX,
                hit.blockY,
                hit.blockZ));
      }
      return;
    }
    ItemDungeonEditor editor = (ItemDungeonEditor) held.getItem();
    int action;
    if (use && mc.thePlayer.isSneaking()) action = PacketAreaEditorInteract.OPEN;
    else {
      action = attack ? PacketAreaEditorInteract.FIRST : PacketAreaEditorInteract.SECOND;
      editor.setPoint(
          held, mc.thePlayer, attack ? "Pos1" : "Pos2", hit.blockX, hit.blockY, hit.blockZ);
    }
    PacketManager.INSTANCE.sendToServer(
        new PacketAreaEditorInteract(action, hit.blockX, hit.blockY, hit.blockZ));
  }

  @SuppressWarnings("unchecked")
  private static EntityMovingPlatform findPlatform(Minecraft mc) {
    Vec3
        eye =
            Vec3.createVectorHelper(
                mc.thePlayer.posX,
                mc.thePlayer.posY + mc.thePlayer.getEyeHeight(),
                mc.thePlayer.posZ),
        look = mc.thePlayer.getLook(1F),
        end = eye.addVector(look.xCoord * 6D, look.yCoord * 6D, look.zCoord * 6D);
    EntityMovingPlatform best = null;
    double distance = Double.MAX_VALUE;
    for (Object value : new java.util.ArrayList(mc.theWorld.loadedEntityList))
      if (value instanceof EntityMovingPlatform) {
        EntityMovingPlatform p = (EntityMovingPlatform) value;
        if (!p.isCollisionActive()) continue;
        for (PlatformBlock block : p.getBlocks()) {
          AxisAlignedBB box =
              AxisAlignedBB.getBoundingBox(
                  p.posX + block.x,
                  p.posY + block.y,
                  p.posZ + block.z,
                  p.posX + block.x + 1D,
                  p.posY + block.y + 1D,
                  p.posZ + block.z + 1D);
          MovingObjectPosition hit = box.calculateIntercept(eye, end);
          if (hit != null && eye.squareDistanceTo(hit.hitVec) < distance) {
            distance = eye.squareDistanceTo(hit.hitVec);
            best = p;
          }
        }
      }
    return best;
  }

  private static DungeonAreaRecord findDungeonArea(Minecraft mc) {
    Vec3
        eye =
            Vec3.createVectorHelper(
                mc.thePlayer.posX,
                mc.thePlayer.posY + mc.thePlayer.getEyeHeight(),
                mc.thePlayer.posZ),
        look = mc.thePlayer.getLook(1F),
        end = eye.addVector(look.xCoord * 6D, look.yCoord * 6D, look.zCoord * 6D);
    DungeonAreaRecord best = null;
    double distance = Double.MAX_VALUE;
    for (DungeonAreaRecord area : ClientDungeonAreas.all()) {
      if (area.getType() == DungeonAreaRecord.TRIGGER) {
        AxisAlignedBB box =
            AxisAlignedBB.getBoundingBox(
                area.getX(),
                area.getY(),
                area.getZ(),
                area.getX() + area.getSizeX(),
                area.getY() + area.getSizeY(),
                area.getZ() + area.getSizeZ());
        MovingObjectPosition hit = box.calculateIntercept(eye, end);
        if (hit != null && eye.squareDistanceTo(hit.hitVec) < distance) {
          distance = eye.squareDistanceTo(hit.hitVec);
          best = area;
        }
        continue;
      }
      for (PlatformBlock block : area.getBlocks()) {
        AxisAlignedBB box =
            AxisAlignedBB.getBoundingBox(
                area.getX() + block.x,
                area.getY() + block.y,
                area.getZ() + block.z,
                area.getX() + block.x + 1D,
                area.getY() + block.y + 1D,
                area.getZ() + block.z + 1D);
        MovingObjectPosition hit = box.calculateIntercept(eye, end);
        if (hit != null && eye.squareDistanceTo(hit.hitVec) < distance) {
          distance = eye.squareDistanceTo(hit.hitVec);
          best = area;
        }
      }
    }
    return best;
  }
}
