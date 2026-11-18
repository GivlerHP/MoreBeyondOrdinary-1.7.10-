package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.container.ContainerDescriptionWorkbench;
import ru.givler.mbo.item.skin.WeaponSkinData;
import ru.givler.mbo.network.EditorPacketAccess;
import ru.givler.mbo.tileentity.TileEntityDescriptionWorkbench;

public final class PacketWeaponSkin implements IMessage {
    private int window;
    private ItemStack expected;
    private String skinId;

    public PacketWeaponSkin() {}

    public PacketWeaponSkin(int window, ItemStack expected, String skinId) {
        this.window = window;
        this.expected = expected.copy();
        this.skinId = skinId;
    }

    @Override public void fromBytes(ByteBuf buf) {
        window = buf.readInt();
        expected = ByteBufUtils.readItemStack(buf);
        skinId = ByteBufUtils.readUTF8String(buf);
    }

    @Override public void toBytes(ByteBuf buf) {
        buf.writeInt(window);
        ByteBufUtils.writeItemStack(buf, expected);
        ByteBufUtils.writeUTF8String(buf, skinId);
    }

    public static final class Handler implements IMessageHandler<PacketWeaponSkin, IMessage> {
        @Override public IMessage onMessage(PacketWeaponSkin message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().playerEntity;
            EditorPacketAccess.schedule(context, () -> {
                if (!(player.openContainer instanceof ContainerDescriptionWorkbench)
                        || player.openContainer.windowId != message.window
                        || !TileEntityDescriptionWorkbench.canEditWeapon(player)) return;
                ContainerDescriptionWorkbench container = (ContainerDescriptionWorkbench) player.openContainer;
                ItemStack stack = container.tile.getStackInSlot(0);
                if (!container.canInteractWith(player) || stack == null
                        || !ItemStack.areItemStacksEqual(stack, message.expected)
                        || !WeaponSkinData.set(stack, message.skinId)) return;
                container.tile.markDirty();
                container.detectAndSendChanges();
            });
            return null;
        }
    }
}
