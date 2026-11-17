package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.container.ContainerDescriptionWorkbench;
import ru.givler.mbo.item.WeaponSettings;
import ru.givler.mbo.network.EditorPacketAccess;
import ru.givler.mbo.tileentity.TileEntityDescriptionWorkbench;

public final class PacketWeaponSettings implements IMessage {
    private int window;
    private ItemStack expected;
    private boolean reset;
    private double[] values=new double[7];
    public PacketWeaponSettings() {}
    public PacketWeaponSettings(int window,ItemStack expected,double[] values,boolean reset) {
        this.window=window; this.expected=expected; this.values=values.clone(); this.reset=reset;
    }
    public void fromBytes(ByteBuf buf) {
        window=buf.readInt(); expected=ByteBufUtils.readItemStack(buf); reset=buf.readBoolean();
        for(int i=0;i<7;i++) values[i]=buf.readDouble();
    }
    public void toBytes(ByteBuf buf) {
        buf.writeInt(window); ByteBufUtils.writeItemStack(buf,expected); buf.writeBoolean(reset);
        for(double value:values) buf.writeDouble(value);
    }
    public static final class Handler implements IMessageHandler<PacketWeaponSettings,IMessage> {
        public IMessage onMessage(final PacketWeaponSettings message,MessageContext context) {
            final EntityPlayerMP player=context.getServerHandler().playerEntity;
            EditorPacketAccess.schedule(context,new Runnable() { public void run() {
                if(!(player.openContainer instanceof ContainerDescriptionWorkbench)
                        || player.openContainer.windowId!=message.window
                        || !TileEntityDescriptionWorkbench.canEditWeapon(player)) return;
                ContainerDescriptionWorkbench container=(ContainerDescriptionWorkbench)player.openContainer;
                ItemStack stack=container.tile.getStackInSlot(0);
                if(!container.canInteractWith(player) || !WeaponSettings.supports(stack)
                        || !ItemStack.areItemStacksEqual(stack,message.expected)) return;
                if(message.reset) WeaponSettings.reset(stack);
                else {
                    if(!WeaponSettings.valid(message.values)) return;
                    if(!cpw.mods.fml.common.Loader.isModLoaded("attackdelay")) {
                        message.values[2]=100; message.values[3]=100;
                    }
                    WeaponSettings.write(stack,message.values);
                }
                container.tile.markDirty(); container.detectAndSendChanges();
            }});
            return null;
        }
    }
}
