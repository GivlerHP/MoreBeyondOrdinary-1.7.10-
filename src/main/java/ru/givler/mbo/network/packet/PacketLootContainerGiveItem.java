package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.givler.mbo.item.ItemBlockLootContainer;
import ru.givler.mbo.lootcontainer.LootContainerConfigValidator;
import ru.givler.mbo.lootcontainer.LootContainerData;
import ru.givler.mbo.registry.ModelRegistry;

import java.util.List;

public class PacketLootContainerGiveItem implements IMessage {
    private static final Logger LOGGER = LogManager.getLogger("MBO.PacketLootContainerGiveItem");

    private NBTTagCompound configTag;

    public PacketLootContainerGiveItem() {}

    public static PacketLootContainerGiveItem of(NBTTagCompound configTag) {
        PacketLootContainerGiveItem pkt = new PacketLootContainerGiveItem();
        pkt.configTag = configTag == null ? new NBTTagCompound() : configTag;
        return pkt;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        configTag = ByteBufUtils.readTag(buf);
        if (configTag == null) configTag = new NBTTagCompound();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, configTag);
    }

    public static class Handler implements IMessageHandler<PacketLootContainerGiveItem, IMessage> {
        @Override
        public IMessage onMessage(PacketLootContainerGiveItem message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (!ItemBlockLootContainer.isEditor(player)) {
                return null;
            }

            LootContainerData data = LootContainerData.fromItemStackNbt(message.configTag);
            List<String> errors = LootContainerConfigValidator.validate(data);
            if (errors != null && !errors.isEmpty()) {
                String error = errors.get(0);
                LOGGER.error("Rejected get-item request from {}: {}",
                        player == null ? "unknown" : player.getCommandSenderName(), error);
                if (player != null) {
                    player.addChatMessage(new ChatComponentText("§cLootContainer config rejected: " + error));
                }
                return null;
            }

            ItemStack stack = new ItemStack(ModelRegistry.LootContainer, 1, 0);
            ItemBlockLootContainer.writeData(stack, data);
            if (!player.inventory.addItemStackToInventory(stack)) {
                player.dropPlayerItemWithRandomChoice(stack, false);
            }
            player.inventory.markDirty();
            return null;
        }
    }
}
