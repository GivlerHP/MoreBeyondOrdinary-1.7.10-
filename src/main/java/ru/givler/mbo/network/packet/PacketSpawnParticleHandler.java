package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;

@SideOnly(Side.CLIENT)
public class PacketSpawnParticleHandler implements IMessageHandler<PacketSpawnParticle, IMessage> {

    @Override
    public IMessage onMessage(PacketSpawnParticle pkt, MessageContext ctx) {
        World world = Minecraft.getMinecraft().theWorld;
        if (world == null) return null;

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

        return null;
    }
}