package ru.givler.mbo.mirror;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.event.world.WorldEvent;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.magic.possession.PossessionManager;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketSilverMirrorScene;
import ru.givler.mbo.tileentity.TileSilverMirror;

public final class SilverMirrorStreaming {
    public static final SilverMirrorStreaming INSTANCE = new SilverMirrorStreaming();
    private static final int RADIUS = SilverMirrorBlocks.RADIUS;
    private static final int MAX_VIEWERS = 8;
    private static final int RENEW_INTERVAL = 30;
    private static final int TIMEOUT = 75;

    private final Map<UUID,Lease> leases = new HashMap<>();
    private final Map<UUID,Long> lastRequest = new HashMap<>();
    private long tick, nextStreamId;

    private static final class Lease {
        final WorldServer world;
        final ForgeChunkManager.Ticket ticket;
        final EntityPlayerMP player;
        final TileSilverMirror source;
        final int[] link;
        final long streamId;
        final int[] blocks = new int[SilverMirrorBlocks.SIZE];
        final Map<Integer,PacketSilverMirrorScene.Actor> lastActors = new HashMap<>();
        TileSilverMirror exit;
        java.util.List<net.minecraft.nbt.NBTTagCompound> decor;
        long expires;

        Lease(WorldServer world, ForgeChunkManager.Ticket ticket, EntityPlayerMP player,
              TileSilverMirror source, int[] link, long streamId) {
            this.world = world;
            this.ticket = ticket;
            this.player = player;
            this.source = source;
            this.link = link;
            this.streamId = streamId;
        }

        void release() { ForgeChunkManager.releaseTicket(ticket); }
    }

    private SilverMirrorStreaming() {}

    public static void request(EntityPlayerMP player, int dimension, int x, int y, int z, long streamId) {
        INSTANCE.open(player,dimension,x,y,z,streamId);
    }

    private void open(EntityPlayerMP player, int dimension, int x, int y, int z, long streamId) {
        if (player == null || player.isDead || player.dimension != dimension || y < 0 || y > 255) return;
        WorldServer world = player.getServerForPlayer();
        EntityLivingBase viewer = viewerNear(player,world,x,y,z);
        if (viewer == null) return;
        if (!world.blockExists(x,y,z)) return;
        TileEntity candidate = world.getTileEntity(x,y,z);
        if (!(candidate instanceof TileSilverMirror) || candidate.isInvalid()) return;
        TileSilverMirror source = (TileSilverMirror)candidate;
        int facing=source.getBlockMetadata()&3;
        int nx=ru.givler.mbo.block.BlockMirror.NX[facing], nz=ru.givler.mbo.block.BlockMirror.NZ[facing];
        net.minecraft.util.Vec3 aperture=net.minecraft.util.Vec3.createVectorHelper(x+.5-nx*.435,y+1,z+.5-nz*.435);
        net.minecraft.util.Vec3 eye=net.minecraft.util.Vec3.createVectorHelper(viewer.posX,
            viewer.posY+viewer.getEyeHeight(),viewer.posZ);
        if ((eye.xCoord-aperture.xCoord)*nx+(eye.zCoord-aperture.zCoord)*nz <= 0) return;
        net.minecraft.util.MovingObjectPosition hit=world.func_147447_a(eye,aperture,false,true,false);
        if (hit!=null && (hit.blockX!=x || hit.blockZ!=z || (hit.blockY!=y && hit.blockY!=y+1))
                && world.getBlock(hit.blockX,hit.blockY,hit.blockZ).isOpaqueCube()) return;
        int[] link = source.linkPosition();
        if (link == null) return;
        UUID id = player.getUniqueID();
        Long previous = lastRequest.get(id);
        if (previous != null && tick - previous < RENEW_INTERVAL) return;
        lastRequest.put(id,tick);

        Lease lease = leases.get(id);
        if (lease != null && (lease.world != world || lease.source != source || !java.util.Arrays.equals(lease.link,link))) {
            lease.release();
            leases.remove(id);
            lease = null;
        }
        boolean newLease = lease == null;
        if (newLease) {
            if (leases.size() >= MAX_VIEWERS) return;
            ForgeChunkManager.Ticket ticket = ForgeChunkManager.requestTicket(MoreBeyondOrdinary.instance,
                world,ForgeChunkManager.Type.NORMAL);
            if (ticket == null) return;
            lease = new Lease(world,ticket,player,source,link,++nextStreamId);
        }
        boolean keep = !newLease;
        try {
            // Load and validate the other endpoint before loading surrounding chunks.
            if (newLease) load(lease,link[0]>>4,link[2]>>4);
            TileSilverMirror exit = source.destination();
            if (exit == null) {
                if (!newLease) { leases.remove(id); keep=false; }
                return;
            }
            int minX=(exit.xCoord-RADIUS)>>4, maxX=(exit.xCoord+RADIUS)>>4;
            int minZ=(exit.zCoord-RADIUS)>>4, maxZ=(exit.zCoord+RADIUS)>>4;
            if (lease.ticket.getChunkListDepth() < (maxX-minX+1)*(maxZ-minZ+1)) return;
            lease.exit=exit;
            if (newLease || streamId != lease.streamId) {
                int sections = 0;
                for (int section=SilverMirrorBlocks.minY(exit.yCoord)>>4;
                        section<=SilverMirrorBlocks.maxY(exit.yCoord)>>4; section++)
                    sections |= 1<<section;
                PacketSilverMirrorScene scene = packet(lease,true);
                for (int cx=minX;cx<=maxX;cx++) for (int cz=minZ;cz<=maxZ;cz++) {
                    if (newLease && (cx!=link[0]>>4 || cz!=link[2]>>4)) load(lease,cx,cz);
                    scene.addChunk(new S21PacketChunkData(world.getChunkFromChunkCoords(cx,cz),true,sections));
                }
                readBlocks(lease,null);
                readDecor(lease,scene,true);
                PacketManager.INSTANCE.sendTo(scene,player);
            }
            lease.expires = tick + TIMEOUT;
            leases.put(id,lease);
            keep = true;
        } finally {
            if (!keep) lease.release();
        }
    }

    private static EntityLivingBase viewerNear(EntityPlayerMP player, WorldServer world, int x, int y, int z) {
        EntityLivingBase viewer = PossessionManager.view(player);
        return viewer.worldObj == world && viewer.isEntityAlive()
            && viewer.getDistanceSq(x+.5,y+1,z+.5) <= 26*26 ? viewer : null;
    }

    private static PacketSilverMirrorScene packet(Lease lease,boolean initial) {
        PacketSilverMirrorScene scene=new PacketSilverMirrorScene(lease.player.dimension,lease.source,lease.exit);
        scene.initial=initial;
        scene.streamId=lease.streamId;
        scene.collectEntities(lease.world,lease.exit);
        if (initial) lease.lastActors.clear();
        scene.omitUnchangedAppearance(lease.lastActors);
        return scene;
    }

    private static void readBlocks(Lease lease,PacketSilverMirrorScene changes) {
        TileSilverMirror exit=lease.exit;
        int minY=SilverMirrorBlocks.minY(exit.yCoord), maxY=SilverMirrorBlocks.maxY(exit.yCoord);
        boolean sky=!lease.world.provider.hasNoSky;
        for (int dx=-RADIUS;dx<=RADIUS;dx++) for (int dz=-RADIUS;dz<=RADIUS;dz++) {
            int x=exit.xCoord+dx,z=exit.zCoord+dz;
            net.minecraft.world.chunk.Chunk chunk=lease.world.getChunkFromChunkCoords(x>>4,z>>4);
            for (int y=minY;y<=maxY;y++) {
                int index=SilverMirrorBlocks.index(dx,y-minY,dz);
                int state=SilverMirrorBlocks.state(chunk,x&15,y,z&15,sky);
                if (changes!=null && state!=lease.blocks[index]) changes.addChange(index,state);
                lease.blocks[index]=state;
            }
        }
    }

    private static void readDecor(Lease lease,PacketSilverMirrorScene packet,boolean initial) {
        TileSilverMirror exit=lease.exit;
        java.util.List<net.minecraft.nbt.NBTTagCompound> visible=new java.util.ArrayList<>();
        for (int cx=(exit.xCoord-RADIUS)>>4;cx<=(exit.xCoord+RADIUS)>>4;cx++) {
            for (int cz=(exit.zCoord-RADIUS)>>4;cz<=(exit.zCoord+RADIUS)>>4;cz++) {
                for (Object value : lease.world.getChunkFromChunkCoords(cx,cz).chunkTileEntityMap.values()) {
                    TileEntity tile=(TileEntity)value;
                    if (tile.isInvalid() || Math.abs(tile.xCoord-exit.xCoord)>RADIUS
                            || Math.abs(tile.zCoord-exit.zCoord)>RADIUS
                            || tile.yCoord<SilverMirrorBlocks.minY(exit.yCoord)
                            || tile.yCoord>SilverMirrorBlocks.maxY(exit.yCoord)) continue;
                    net.minecraft.nbt.NBTTagCompound tag=new net.minecraft.nbt.NBTTagCompound();
                    if (tile.getClass()==ru.givler.mbo.tileentity.ModelTileBase.class) {
                        tile.writeToNBT(tag);
                    } else if (tile instanceof ru.givler.mbo.tileentity.TileMirror) {
                        // The outside of other mirrors is visible, their links are private to the server.
                        tag.setInteger("x",tile.xCoord);
                        tag.setInteger("y",tile.yCoord);
                        tag.setInteger("z",tile.zCoord);
                    } else continue;
                    visible.add(tag);
                    if (visible.size()==PacketSilverMirrorScene.MAX_DECOR) break;
                }
                if (visible.size()==PacketSilverMirrorScene.MAX_DECOR) break;
            }
            if (visible.size()==PacketSilverMirrorScene.MAX_DECOR) break;
        }
        if (initial || !visible.equals(lease.decor)) {
            lease.decor=visible;
            packet.decor=visible;
        }
    }

    private static void load(Lease lease,int x,int z) {
        ForgeChunkManager.forceChunk(lease.ticket,new ChunkCoordIntPair(x,z));
        lease.world.getChunkProvider().loadChunk(x,z);
    }

    @SubscribeEvent public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        tick++;
        for (Iterator<Lease> it=leases.values().iterator();it.hasNext();) {
            Lease lease=it.next();
            if (lease.expires <= tick || lease.player.worldObj!=lease.world || lease.player.isDead
                    || lease.source.isInvalid() || lease.exit.isInvalid() || lease.source.destination()!=lease.exit
                    || viewerNear(lease.player,lease.world,lease.source.xCoord,lease.source.yCoord,lease.source.zCoord)==null) {
                lease.release();
                it.remove();
                continue;
            }
            if ((tick+lease.streamId)%2==0) {
                PacketSilverMirrorScene update=packet(lease,false);
                if ((tick+lease.streamId)%10==0) {
                    readBlocks(lease,update);
                    readDecor(lease,update,false);
                }
                PacketManager.INSTANCE.sendTo(update,lease.player);
            }
        }
        lastRequest.values().removeIf(last -> tick-last > TIMEOUT);
    }

    @SubscribeEvent public void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id=event.player.getUniqueID();
        Lease lease=leases.remove(id);
        if (lease!=null) lease.release();
        lastRequest.remove(id);
    }

    @SubscribeEvent public void unload(WorldEvent.Unload event) {
        if (event.world.isRemote) return;
        for (Iterator<Lease> it=leases.values().iterator();it.hasNext();) {
            Lease lease=it.next();
            // Forge clears the world's tickets during its own unload hook.
            if (lease.world==event.world) it.remove();
        }
    }
}

