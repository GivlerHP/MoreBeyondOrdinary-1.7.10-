package ru.givler.mbo.handler;

import baubles.api.BaublesApi;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketLanternBeltVisual;

/** Sends only cosmetic presence/light state; fuel and equipment remain server-owned. */
public final class LanternBeltVisualSync {
  public static int state(EntityPlayer player) {
    net.minecraft.inventory.IInventory inventory=BaublesApi.getBaubles(player);
    ItemStack stack=inventory==null || inventory.getSizeInventory()<=3?null:inventory.getStackInSlot(3);
    if(stack==null || !String.valueOf(Item.itemRegistry.getNameForObject(stack.getItem())).startsWith("minefantasy2:lantern_"))return 0;
    return stack.hasTagCompound() && stack.getTagCompound().getBoolean("lit") && stack.getTagCompound().getInteger("fuel")>0?2:1;
  }
  @SubscribeEvent public void tick(TickEvent.PlayerTickEvent event) {
    EntityPlayer p=event.player;
    if(event.phase!=TickEvent.Phase.END || p.worldObj.isRemote || p.ticksExisted%10!=0)return;
    int state=state(p);
    if(p.ticksExisted%100!=0 && p.getEntityData().getInteger("MBOBeltVisual")==state)return;
    p.getEntityData().setInteger("MBOBeltVisual",state);
    PacketManager.INSTANCE.sendToAllAround(new PacketLanternBeltVisual(p.getEntityId(),state),
        new NetworkRegistry.TargetPoint(p.dimension,p.posX,p.posY,p.posZ,96));
  }
  @SubscribeEvent public void tracking(PlayerEvent.StartTracking event) {
    if(event.target instanceof EntityPlayer && event.entityPlayer instanceof EntityPlayerMP)
      PacketManager.INSTANCE.sendTo(new PacketLanternBeltVisual(event.target.getEntityId(),state((EntityPlayer)event.target)),(EntityPlayerMP)event.entityPlayer);
  }
}
