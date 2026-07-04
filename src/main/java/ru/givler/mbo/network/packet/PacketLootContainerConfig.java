package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.givler.mbo.item.ItemBlockLootContainer;
import ru.givler.mbo.lootcontainer.LootContainerConfigValidator;
import ru.givler.mbo.lootcontainer.LootContainerData;
import ru.givler.mbo.tileentity.TileEntityLootContainer;

import java.util.List;

public class PacketLootContainerConfig implements IMessage {
    private static final Logger LOGGER = LogManager.getLogger("MBO.PacketLootContainerConfig");
    private boolean applyToBlock;
    private int x;
    private int y;
    private int z;
    private NBTTagCompound configTag;

    public PacketLootContainerConfig() {}

    public static PacketLootContainerConfig forHeldItem(NBTTagCompound configTag) {
        PacketLootContainerConfig pkt = new PacketLootContainerConfig();
        pkt.applyToBlock = false;
        pkt.configTag = configTag == null ? new NBTTagCompound() : configTag;
        return pkt;
    }

    public static PacketLootContainerConfig forBlock(int x, int y, int z, NBTTagCompound configTag) {
        PacketLootContainerConfig pkt = new PacketLootContainerConfig();
        pkt.applyToBlock = true;
        pkt.x = x;
        pkt.y = y;
        pkt.z = z;
        pkt.configTag = configTag == null ? new NBTTagCompound() : configTag;
        return pkt;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        applyToBlock = buf.readBoolean();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        configTag = ByteBufUtils.readTag(buf);
        if (configTag == null) configTag = new NBTTagCompound();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(applyToBlock);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeTag(buf, configTag);
    }

    public static class Handler implements IMessageHandler<PacketLootContainerConfig, IMessage> {
        @Override
        public IMessage onMessage(PacketLootContainerConfig message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (!ItemBlockLootContainer.isEditor(player)) {
                return null;
            }
            if (message.applyToBlock) {
                applyBlockConfig(player, message);
            } else {
                applyHeldItemConfig(player, message);
            }
            return null;
        }

        private void applyBlockConfig(EntityPlayerMP player, PacketLootContainerConfig message) {
            if (player == null || player.worldObj == null) return;
            TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);
            if (!(te instanceof TileEntityLootContainer)) return;
            LootContainerData data = LootContainerData.fromItemStackNbt(message.configTag);
            if (!validateAndReport(player, data, "block@" + message.x + "," + message.y + "," + message.z)) return;
            ((TileEntityLootContainer) te).applyConfig(data, false);
        }

        private void applyHeldItemConfig(EntityPlayerMP player, PacketLootContainerConfig message) {
            if (player == null) return;
            ItemStack held = player.getCurrentEquippedItem();
            if (held == null || !(held.getItem() instanceof ItemBlockLootContainer)) return;
            LootContainerData data = LootContainerData.fromItemStackNbt(message.configTag);
            if (!validateAndReport(player, data, "held_item")) return;
            ItemBlockLootContainer.writeData(held, data);
            player.inventory.markDirty();
        }

        private boolean validateAndReport(EntityPlayerMP player, LootContainerData data, String target) {
            List<String> errors = LootContainerConfigValidator.validate(data);
            if (errors == null || errors.isEmpty()) return true;
            String message = errors.get(0);
            LOGGER.error("Rejected invalid loot container config from {} for {}: {}",
                    player == null ? "unknown" : player.getCommandSenderName(), target, message);
            if (player != null) {
                player.addChatMessage(new ChatComponentText("§cLootContainer config rejected: " + message));
            }
            return false;
        }
    }
}
