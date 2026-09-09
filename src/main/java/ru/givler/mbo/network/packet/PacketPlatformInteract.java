package ru.givler.mbo.network.packet;



import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.item.ItemPlatformEditor;
import ru.givler.mbo.registry.ItemRegistry;

public class PacketPlatformInteract implements IMessage {
    public static final int FIRST=0, SECOND=1, OPEN=2, OPEN_ENTITY=3;
    private int action,x,y,z;
    public PacketPlatformInteract(){}
    public PacketPlatformInteract(int action,int x,int y,int z){this.action=action;this.x=x;this.y=y;this.z=z;}
    @Override public void fromBytes(ByteBuf buf){action=buf.readByte();x=buf.readInt();y=buf.readInt();z=buf.readInt();}
    @Override public void toBytes(ByteBuf buf){buf.writeByte(action);buf.writeInt(x);buf.writeInt(y);buf.writeInt(z);}
    public static class Handler implements IMessageHandler<PacketPlatformInteract,IMessage>{
        @Override public IMessage onMessage(PacketPlatformInteract message,MessageContext context){
            EntityPlayerMP player=context.getServerHandler().playerEntity;ItemStack held=player.getCurrentEquippedItem();
            if(held==null||held.getItem()!=ItemRegistry.PlatformEditor)return null;
            ItemPlatformEditor editor=(ItemPlatformEditor)held.getItem();
            if(message.action==OPEN_ENTITY){
                net.minecraft.entity.Entity entity=player.worldObj.getEntityByID(message.x);
                if(entity instanceof ru.givler.mbo.movingplatform.EntityMovingPlatform&&player.getDistanceSqToEntity(entity)<=64D)
                    ItemPlatformEditor.open(player,(ru.givler.mbo.movingplatform.EntityMovingPlatform)entity,held);
            }else if(message.action==FIRST)editor.selectFirst(held,message.x,message.y,message.z,player);
            else editor.selectSecondOrOpen(held,player,player.worldObj,message.x,message.y,message.z,message.action==OPEN);
            return null;
        }
    }
}
