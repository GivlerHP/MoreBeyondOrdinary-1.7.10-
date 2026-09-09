package ru.givler.mbo.client;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import ru.givler.mbo.movingplatform.EntityMovingPlatform;
import ru.givler.mbo.movingplatform.PlatformBlock;
import net.minecraftforge.client.event.MouseEvent;
import ru.givler.mbo.item.ItemPlatformEditor;
import ru.givler.mbo.movingplatform.PlatformAccess;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketPlatformInteract;
import ru.givler.mbo.registry.ItemRegistry;

/** Consumes the physical attack/use click before creative mode can instantly remove a block. */
public final class PlatformEditorMouseHandler {
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void onMouse(MouseEvent event) {
        if(event==null||!event.buttonstate)return;
        Minecraft mc=Minecraft.getMinecraft();
        if(mc==null||mc.thePlayer==null||mc.theWorld==null||mc.currentScreen!=null)return;
        ItemStack held=mc.thePlayer.getCurrentEquippedItem();
        if(held==null||held.getItem()!=ItemRegistry.PlatformEditor||!PlatformAccess.canEdit(mc.thePlayer))return;
        int mouseKey=event.button-100;
        boolean attack=mc.gameSettings.keyBindAttack.getKeyCode()==mouseKey;
        boolean use=mc.gameSettings.keyBindUseItem.getKeyCode()==mouseKey;
        if(!attack&&!use)return;
        if(use&&mc.thePlayer.isSneaking()){
            EntityMovingPlatform target=findPlatform(mc);
            if(target!=null){event.setCanceled(true);PacketManager.INSTANCE.sendToServer(new PacketPlatformInteract(PacketPlatformInteract.OPEN_ENTITY,target.getEntityId(),0,0));return;}
        }
        MovingObjectPosition hit=mc.objectMouseOver;
        if(hit==null||hit.typeOfHit!=MovingObjectPosition.MovingObjectType.BLOCK)return;
        event.setCanceled(true);
        ItemPlatformEditor editor=(ItemPlatformEditor)held.getItem();
        if(attack){
            editor.selectFirst(held,hit.blockX,hit.blockY,hit.blockZ,mc.thePlayer);
            PacketManager.INSTANCE.sendToServer(new PacketPlatformInteract(PacketPlatformInteract.FIRST,hit.blockX,hit.blockY,hit.blockZ));
        }else{
            boolean open=mc.thePlayer.isSneaking();
            if(!open)editor.selectSecondOrOpen(held,mc.thePlayer,mc.theWorld,hit.blockX,hit.blockY,hit.blockZ,false);
            PacketManager.INSTANCE.sendToServer(new PacketPlatformInteract(open?PacketPlatformInteract.OPEN:PacketPlatformInteract.SECOND,hit.blockX,hit.blockY,hit.blockZ));
        }
    }

    @SuppressWarnings("unchecked") private static EntityMovingPlatform findPlatform(Minecraft mc){
        Vec3 eye=Vec3.createVectorHelper(mc.thePlayer.posX,mc.thePlayer.posY+mc.thePlayer.getEyeHeight(),mc.thePlayer.posZ);
        Vec3 look=mc.thePlayer.getLook(1F);double reach=6D;Vec3 end=eye.addVector(look.xCoord*reach,look.yCoord*reach,look.zCoord*reach);
        EntityMovingPlatform best=null;double bestDistance=Double.MAX_VALUE;
        for(Object object:new java.util.ArrayList(mc.theWorld.loadedEntityList))if(object instanceof EntityMovingPlatform){
            EntityMovingPlatform platform=(EntityMovingPlatform)object;if(!platform.isCollisionActive())continue;
            for(PlatformBlock block:platform.getBlocks()){
                AxisAlignedBB box=AxisAlignedBB.getBoundingBox(platform.posX+block.x,platform.posY+block.y,platform.posZ+block.z,platform.posX+block.x+1D,platform.posY+block.y+1D,platform.posZ+block.z+1D);
                MovingObjectPosition hit=box.calculateIntercept(eye,end);if(hit==null)continue;double distance=eye.squareDistanceTo(hit.hitVec);
                if(distance<bestDistance){bestDistance=distance;best=platform;}
            }
        }
        return best;
    }
}
