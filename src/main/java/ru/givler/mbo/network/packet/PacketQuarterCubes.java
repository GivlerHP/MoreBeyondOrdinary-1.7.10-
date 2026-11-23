package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.data.world.QuarterCubeData;
import ru.givler.mbo.network.EditorPacketAccess;
import ru.givler.mbo.network.PacketManager;

public final class PacketQuarterCubes implements IMessage {
    private int dimension, chunkX, chunkZ;
    private boolean replace;
    private Map<Integer, Integer> positions;

    public PacketQuarterCubes() {}

    private PacketQuarterCubes(World world, int x, int z, boolean replace, Map<Integer, Integer> positions) {
        dimension = world.provider.dimensionId;
        chunkX = x;
        chunkZ = z;
        this.replace = replace;
        this.positions = positions;
    }

    public static void broadcast(World world, int x, int z, int position, int mask) {
        if (!(world instanceof WorldServer)) return;
        WorldServer server = (WorldServer) world;
        PacketQuarterCubes packet = new PacketQuarterCubes(world, x, z, false, Collections.singletonMap(position, mask));
        for (Object entity : world.playerEntities) {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            if (server.getPlayerManager().isPlayerWatchingChunk(player, x, z)) PacketManager.INSTANCE.sendTo(packet, player);
        }
    }

    public static void sendChunk(EntityPlayerMP player, int x, int z, Map<Integer, Integer> positions) {
        PacketManager.INSTANCE.sendTo(new PacketQuarterCubes(player.worldObj, x, z, true, new HashMap<>(positions)), player);
    }

    @Override public void toBytes(ByteBuf buffer) {
        buffer.writeInt(dimension);
        buffer.writeInt(chunkX);
        buffer.writeInt(chunkZ);
        buffer.writeBoolean(replace);
        buffer.writeInt(positions.size());
        for (Map.Entry<Integer, Integer> entry : positions.entrySet()) {
            buffer.writeInt(entry.getKey() << 8 | entry.getValue());
        }
    }

    @Override public void fromBytes(ByteBuf buffer) {
        dimension = buffer.readInt();
        chunkX = buffer.readInt();
        chunkZ = buffer.readInt();
        replace = buffer.readBoolean();
        int count = buffer.readInt();
        if (count < 0 || count > buffer.readableBytes() / 4) return;
        positions = new HashMap<>();
        for (int index = 0; index < count; index++) {
            int entry = buffer.readInt();
            positions.put(entry >> 8, entry & 255);
        }
    }

    public static final class Handler implements IMessageHandler<PacketQuarterCubes, IMessage> {
        @Override public IMessage onMessage(PacketQuarterCubes message, MessageContext context) {
            EditorPacketAccess.scheduleClient(() -> {
                World world = MoreBeyondOrdinary.proxy.getClientWorld();
                if (world != null && world.provider.dimensionId == message.dimension && message.positions != null) {
                    QuarterCubeData.apply(world, message.chunkX, message.chunkZ, message.replace, message.positions);
                }
            });
            return null;
        }
    }
}
