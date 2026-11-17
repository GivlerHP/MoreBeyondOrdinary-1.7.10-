package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.container.ContainerDescriptionWorkbench;
import ru.givler.mbo.item.ItemDescription;
import ru.givler.mbo.network.EditorPacketAccess;

public final class PacketItemDescription implements IMessage {
    private int window, color;
    private ItemStack expected;
    private final List<String> lines = new ArrayList<String>();
    private final List<int[]> colors = new ArrayList<int[]>();
    private boolean valid = true;
    public PacketItemDescription() {}
    public PacketItemDescription(int window, ItemStack expected, List<String> lines, int color) {
        this.window = window; this.expected = expected; this.lines.addAll(lines); this.color = color;
    }
    public PacketItemDescription(int window, ItemStack expected, List<String> lines, List<int[]> colors, int color) {
        this(window, expected, lines, color); this.colors.addAll(colors);
    }
    @Override public void toBytes(ByteBuf buf) {
        buf.writeInt(window); buf.writeInt(color); ByteBufUtils.writeItemStack(buf, expected);
        buf.writeByte(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i); ByteBufUtils.writeUTF8String(buf, line);
            int[] style = i < colors.size() ? colors.get(i) : new int[0];
            buf.writeByte(style.length);
            for (int rgb : style) buf.writeInt(rgb);
        }
    }
    @Override public void fromBytes(ByteBuf buf) {
        window = buf.readInt(); color = buf.readInt(); expected = ByteBufUtils.readItemStack(buf);
        int count = buf.readUnsignedByte();
        if (count > ItemDescription.MAX_LINES) { valid = false; return; }
        for (int i = 0; i < count; i++) {
            String line = ByteBufUtils.readUTF8String(buf);
            if (line.length() > ItemDescription.MAX_LINE_LENGTH) valid = false;
            lines.add(line);
            int length = buf.readUnsignedByte();
            if (length > ItemDescription.MAX_LINE_LENGTH) { valid = false; return; }
            int[] style = new int[length];
            for (int j = 0; j < length; j++) style[j] = buf.readInt() & 0xffffff;
            colors.add(style);
        }
    }
    public static final class Handler implements IMessageHandler<PacketItemDescription, IMessage> {
        @Override public IMessage onMessage(final PacketItemDescription message, MessageContext context) {
            final EntityPlayerMP player = context.getServerHandler().playerEntity;
            EditorPacketAccess.schedule(context, new Runnable() { public void run() {
                if (!message.valid || !(player.openContainer instanceof ContainerDescriptionWorkbench)
                        || player.openContainer.windowId != message.window) return;
                ContainerDescriptionWorkbench container = (ContainerDescriptionWorkbench) player.openContainer;
                ItemStack item = container.tile.getStackInSlot(0);
                if (!container.canInteractWith(player) || item == null
                        || !ItemStack.areItemStacksEqual(item, message.expected)) return;
                ItemDescription.write(item, message.lines, message.colors, message.color);
                container.tile.markDirty();
                container.detectAndSendChanges();
            }});
            return null;
        }
    }
}
