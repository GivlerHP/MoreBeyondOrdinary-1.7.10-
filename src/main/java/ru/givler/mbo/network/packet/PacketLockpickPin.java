package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.lockable.ILockableTile;
import ru.givler.mbo.lockable.LockableAccess;

public class PacketLockpickPin implements IMessage {
    private int x, y, z, pin, order;
    public PacketLockpickPin() {}
    public PacketLockpickPin(int x, int y, int z, int pin, int order) { this.x=x; this.y=y; this.z=z; this.pin=pin; this.order=order; }
    @Override public void fromBytes(ByteBuf b) { x=b.readInt(); y=b.readInt(); z=b.readInt(); pin=b.readInt(); order=b.readInt(); }
    @Override public void toBytes(ByteBuf b) { b.writeInt(x); b.writeInt(y); b.writeInt(z); b.writeInt(pin); b.writeInt(order); }

    public static class Handler implements IMessageHandler<PacketLockpickPin, PacketLockpickResult> {
        @Override public PacketLockpickResult onMessage(PacketLockpickPin m, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            ILockableTile tile = LockableAccess.get(player.worldObj, m.x, m.y, m.z);
            if (tile == null || !tile.getLockData().isLocked() || !LockableAccess.hasLockpick(player)
                    || player.getDistanceSq(m.x + .5, m.y + .5, m.z + .5) > 64.0) return new PacketLockpickResult(false, m.pin, true, false, true);
            boolean correct = tile.getLockData().checkPin(player.getUniqueID(), m.pin, m.order);
            boolean complete = correct && m.order + 1 >= tile.getLockData().getDifficulty().pinCount;
            player.worldObj.playSoundEffect(m.x + .5, m.y + .5, m.z + .5,
                    correct ? "mbo:lock_pin_match" : "mbo:lock_pin_fail", 1F, .9F + player.getRNG().nextFloat() * .2F);
            boolean broke = !correct && player.getRNG().nextDouble() >= 0.7D;
            boolean close = false;
            if (broke) {
                close = consume(player);
                tile.getLockData().lock(player.worldObj.rand);
                player.worldObj.playSoundAtEntity(player, "random.break", .8F, .85F + player.getRNG().nextFloat() * .2F);
            }
            tile.asTileEntity().markDirty();
            return new PacketLockpickResult(correct, m.pin, broke, complete, close);
        }
        private boolean consume(EntityPlayerMP player) {
            ItemStack held = player.getCurrentEquippedItem();
            if (held == null) return true;
            if (--held.stackSize <= 0) { player.inventory.setInventorySlotContents(player.inventory.currentItem, null); return true; }
            return false;
        }
    }
}
