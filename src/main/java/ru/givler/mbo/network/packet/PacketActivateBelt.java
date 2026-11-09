package ru.givler.mbo.network.packet;
import baubles.api.BaublesApi;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.item.IActivatableBelt;
import ru.givler.mbo.network.EditorPacketAccess;
public final class PacketActivateBelt implements IMessage {
    public void fromBytes(ByteBuf buf) {} public void toBytes(ByteBuf buf) {}
    public static final class Handler implements IMessageHandler<PacketActivateBelt,IMessage> {
        public IMessage onMessage(PacketActivateBelt message, final MessageContext ctx) {
            EditorPacketAccess.schedule(ctx,new Runnable(){public void run(){
                EntityPlayerMP player=ctx.getServerHandler().playerEntity;
                if(player.isDead)return;
                IInventory inv=BaublesApi.getBaubles(player);
                if(inv==null || inv.getSizeInventory()<=3)return;
                ItemStack stack=inv.getStackInSlot(3);
                if(stack==null)return;
                if(stack.getItem() instanceof IActivatableBelt)
                    ((IActivatableBelt)stack.getItem()).activateBelt(player,stack);
                else if(cpw.mods.fml.common.Loader.isModLoaded("minefantasy2")
                        && stack.getItem() instanceof minefantasy.mf2.item.custom.ItemLanternMF)
                    stack.getItem().onItemRightClick(stack,player.worldObj,player);
                else return;
                inv.markDirty();
            }});return null;
        }
    }
}
