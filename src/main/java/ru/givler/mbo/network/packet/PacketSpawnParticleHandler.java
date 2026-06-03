package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;

public class PacketSpawnParticleHandler implements IMessageHandler<PacketSpawnParticle, IMessage> {

    @Override
    public IMessage onMessage(final PacketSpawnParticle pkt, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            ClientHandler.handle(pkt);
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    private static class ClientHandler {
        static void handle(final PacketSpawnParticle pkt) {
            final Minecraft mc = Minecraft.getMinecraft();
            mc.func_152344_a(new Runnable() {
                @Override
                public void run() {
                    World world = mc.theWorld;
                    if (world == null) return;

                    if (pkt.getType().isVanilla()) {
                        world.spawnParticle(
                                pkt.getType().getVanillaName(),
                                pkt.getX(), pkt.getY(), pkt.getZ(),
                                pkt.getMotX(), pkt.getMotY(), pkt.getMotZ()
                        );
                    } else {
                        MoreBeyondOrdinary.proxy.spawnParticle(
                                pkt.getType(), world,
                                pkt.getX(), pkt.getY(), pkt.getZ(),
                                pkt.getMotX(), pkt.getMotY(), pkt.getMotZ()
                        );
                    }
                }
            });
        }
    }
}
