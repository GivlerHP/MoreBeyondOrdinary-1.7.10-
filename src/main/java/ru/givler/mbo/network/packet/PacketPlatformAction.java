package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import ru.givler.mbo.movingplatform.EntityMovingPlatform;
import ru.givler.mbo.movingplatform.PlatformAccess;

public class PacketPlatformAction implements IMessage {
    public static final int SAVE=0, REBUILD=1, RESET=2, GO_A=3, GO_B=4, GET_A=5, GET_B=6, DELETE=7, STOP=8;
    private int entityId, action, direction, distance, seconds, returnMode, delaySeconds;
    public PacketPlatformAction(){}
    public PacketPlatformAction(int entityId,int action,int direction,int distance,int seconds,int returnMode,int delaySeconds){this.entityId=entityId;this.action=action;this.direction=direction;this.distance=distance;this.seconds=seconds;this.returnMode=returnMode;this.delaySeconds=delaySeconds;}
    @Override public void fromBytes(ByteBuf b){entityId=b.readInt();action=b.readInt();direction=b.readInt();distance=b.readInt();seconds=b.readInt();returnMode=b.readInt();delaySeconds=b.readInt();}
    @Override public void toBytes(ByteBuf b){b.writeInt(entityId);b.writeInt(action);b.writeInt(direction);b.writeInt(distance);b.writeInt(seconds);b.writeInt(returnMode);b.writeInt(delaySeconds);}
    public static class Handler implements IMessageHandler<PacketPlatformAction,IMessage>{
        @Override public IMessage onMessage(PacketPlatformAction m,MessageContext ctx){
            EntityPlayerMP player=ctx.getServerHandler().playerEntity;if(!PlatformAccess.canEdit(player))return null;
            Entity e=player.worldObj.getEntityByID(m.entityId);if(!(e instanceof EntityMovingPlatform))return null;
            EntityMovingPlatform p=(EntityMovingPlatform)e;
            if(m.action==SAVE){if(p.isMoving()&&!p.stopAndReturn(player))return null;p.configure(m.direction,m.distance,m.seconds,m.returnMode,m.delaySeconds);p.confirmConfiguration();ru.givler.mbo.network.PacketManager.INSTANCE.sendToDimension(new PacketPlatformSync(p),player.dimension);player.addChatMessage(new net.minecraft.util.ChatComponentTranslation("mbo.platform.saved"));}
            else if(m.action==REBUILD){if(p.rebuild(player))ru.givler.mbo.network.PacketManager.INSTANCE.sendToDimension(new PacketPlatformSync(p),player.dimension);}else if(m.action==RESET)p.reset(player);else if(m.action==GO_A)p.start(false,player);else if(m.action==GO_B)p.start(true,player);
            else if(m.action==GET_A||m.action==GET_B){net.minecraft.item.ItemStack stack=new net.minecraft.item.ItemStack(ru.givler.mbo.registry.BlockRegistry.PlatformStation);stack.setTagCompound(new net.minecraft.nbt.NBTTagCompound());stack.getTagCompound().setString("PlatformId",p.getPlatformId().toString());stack.getTagCompound().setBoolean("TargetB",m.action==GET_B);if(!player.inventory.addItemStackToInventory(stack))player.dropPlayerItemWithRandomChoice(stack,false);}
            else if(m.action==STOP)p.stopAndReturn(player);
            else if(m.action==DELETE){java.util.UUID id=p.getPlatformId();if(p.dismantle(player))ru.givler.mbo.network.PacketManager.INSTANCE.sendToDimension(new PacketPlatformRemove(id),player.dimension);}
            return null;
        }
    }
}
