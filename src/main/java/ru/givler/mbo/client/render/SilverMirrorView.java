package ru.givler.mbo.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveHandlerMP;
import ru.givler.mbo.mirror.SilverMirrorBlocks;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketSilverMirrorScene;
import ru.givler.mbo.network.packet.PacketSilverMirrorView;
import ru.givler.mbo.tileentity.TileSilverMirror;

/** Detached render data. The normal client chunk provider and entity lists are untouched. */
public final class SilverMirrorView {
    private static World owner;
    private static PacketSilverMirrorScene snapshot;
    private static SceneWorld scene;
    private static TileEntity exit;
    private static long lastRequest, received;

    private SilverMirrorView() {}

    static TileEntity remoteExit(TileSilverMirror source) {
        World world=source.getWorldObj();
        if (owner!=world) {
            clear();
            owner=world;
        }
        int[] link=source.linkPosition();
        if (link==null) return null;
        long now=System.nanoTime();
        boolean ready = snapshot!=null && now-received<4_000_000_000L && matches(source,snapshot);
        if (now-lastRequest>=1_750_000_000L || lastRequest==0) {
            lastRequest=now;
            PacketManager.INSTANCE.sendToServer(new PacketSilverMirrorView(world.provider.dimensionId,
                source.xCoord,source.yCoord,source.zCoord,ready?snapshot.streamId:0));
        }
        if (!ready) return null;
        scene.interpolate(now);
        return exit;
    }

    public static void receive(PacketSilverMirrorScene packet) {
        World world=Minecraft.getMinecraft().theWorld;
        if (world==null || world!=owner || world.provider.dimensionId!=packet.dimension
                || !world.blockExists(packet.sourceX,packet.sourceY,packet.sourceZ)) return;
        TileEntity source=world.getTileEntity(packet.sourceX,packet.sourceY,packet.sourceZ);
        if (!(source instanceof TileSilverMirror) || !matches((TileSilverMirror)source,packet)) return;
        long now=System.nanoTime();
        if (!packet.initial) {
            if (snapshot==null || snapshot.streamId!=packet.streamId || !matches((TileSilverMirror)source,snapshot)) return;
            for (int i=0;i<packet.changes.size();i+=2) {
                SilverMirrorBlocks.apply(scene,packet.exitX,packet.exitY,packet.exitZ,
                    packet.changes.get(i),packet.changes.get(i+1));
            }
            snapshot.facing=packet.facing;
            scene.updateDecor(packet);
            scene.updateActors(packet,now);
            received=now;
            return;
        }
        SceneWorld next=buildScene(world,packet);
        TileEntity view=new ru.givler.mbo.tileentity.TileMirror() {
            @Override public int getBlockMetadata() { return packet.facing; }
        };
        view.xCoord=packet.exitX; view.yCoord=packet.exitY; view.zCoord=packet.exitZ;
        view.setWorldObj(next);
        snapshot=packet; scene=next; exit=view; received=System.nanoTime();
    }

    private static SceneWorld buildScene(World world, PacketSilverMirrorScene packet) {
        SceneWorld next=new SceneWorld(world);
        for (S21PacketChunkData data : packet.chunks) {
            Chunk chunk=next.getChunkProvider().loadChunk(data.func_149273_e(),data.func_149271_f());
            chunk.fillChunk(data.func_149272_d(),data.func_149276_g(),data.func_149270_h(),data.func_149274_i());
        }
        next.updateDecor(packet);
        next.updateActors(packet,System.nanoTime());
        return next;
    }

    static boolean isRemoteScene(TileEntity tile) {
        return scene!=null && tile.getWorldObj()==scene;
    }

    static void clear() {
        owner=null; snapshot=null; scene=null; exit=null; lastRequest=0; received=0;
    }

    private static boolean matches(TileSilverMirror source,PacketSilverMirrorScene packet) {
        int[] link=source.linkPosition();
        return link!=null && source.xCoord==packet.sourceX && source.yCoord==packet.sourceY
            && source.zCoord==packet.sourceZ && link[0]==packet.exitX && link[1]==packet.exitY && link[2]==packet.exitZ;
    }

    private static final class SceneWorld extends World {
        SceneWorld(World original) {
            super(new SaveHandlerMP(),"SilverMirror",WorldProvider.getProviderForDimension(original.provider.dimensionId),
                new WorldSettings(original.getWorldInfo()),new Profiler());
            isRemote=true;
            difficultySetting=original.difficultySetting;
            mapStorage=new net.minecraft.world.storage.MapStorage(null);
            finishSetup();
            setWorldTime(original.getWorldTime());
            skylightSubtracted=original.skylightSubtracted;
        }

        @Override protected IChunkProvider createChunkProvider() { return new MirrorChunkProvider(this); }
        @Override protected int func_152379_p() { return 0; }
        @Override public Entity getEntityByID(int id) { return null; }

        private final java.util.Map<Integer,ActorMotion> motion=new java.util.HashMap<>();

        @SuppressWarnings("unchecked")
        void updateDecor(PacketSilverMirrorScene packet) {
            if (packet.decor == null) return;
            int radius=SilverMirrorBlocks.RADIUS;
            for (int cx=(packet.exitX-radius)>>4;cx<=(packet.exitX+radius)>>4;cx++) {
                for (int cz=(packet.exitZ-radius)>>4;cz<=(packet.exitZ+radius)>>4;cz++) {
                    if (getChunkProvider().chunkExists(cx,cz)) getChunkFromChunkCoords(cx,cz).chunkTileEntityMap.clear();
                }
            }
            for (net.minecraft.nbt.NBTTagCompound tag : packet.decor) {
                if (tag==null) continue;
                int x=tag.getInteger("x"),y=tag.getInteger("y"),z=tag.getInteger("z");
                if (!blockExists(x,y,z)) continue;
                TileEntity tile=getBlock(x,y,z).createTileEntity(this,getBlockMetadata(x,y,z));
                if (tile==null) continue;
                if (tile.getClass()==ru.givler.mbo.tileentity.ModelTileBase.class) tile.readFromNBT(tag);
                else if (!(tile instanceof ru.givler.mbo.tileentity.TileMirror)) continue;
                tile.xCoord=x;tile.yCoord=y;tile.zCoord=z;
                tile.setWorldObj(this);
                getChunkFromChunkCoords(x>>4,z>>4).chunkTileEntityMap.put(new net.minecraft.world.ChunkPosition(x&15,y,z&15),tile);
            }
        }

        @SuppressWarnings("unchecked")
        void updateActors(PacketSilverMirrorScene packet,long now) {
            java.util.Set<Integer> seen=new java.util.HashSet<>();
            for (PacketSilverMirrorScene.Actor actor : packet.actors) {
                seen.add(actor.id);
                ActorMotion movement=motion.get(actor.id);
                if (movement!=null && movement.target.type==actor.type && !(actor.type==0 && actor.sendProfile)) {
                    movement.retarget(actor,now);
                    continue;
                }
                if (movement!=null) {
                    loadedEntityList.remove(movement.entity);
                    motion.remove(actor.id);
                }
                if (!actor.sendEquipment || actor.type==0 && actor.profile==null) continue;
                Entity entity=actor.type==0 ? new EntityOtherPlayerMP(this,actor.profile)
                    : EntityList.createEntityByID(actor.type,this);
                if (!(entity instanceof EntityLivingBase)) continue;
                movement=new ActorMotion((EntityLivingBase)entity,actor,now);
                motion.put(actor.id,movement);
                loadedEntityList.add(entity);
            }
            for (java.util.Iterator<java.util.Map.Entry<Integer,ActorMotion>> it=motion.entrySet().iterator();it.hasNext();) {
                java.util.Map.Entry<Integer,ActorMotion> entry=it.next();
                if (!seen.contains(entry.getKey())) {
                    loadedEntityList.remove(entry.getValue().entity);
                    it.remove();
                }
            }
        }

        void interpolate(long now) {
            for (ActorMotion movement : motion.values()) movement.apply(now);
        }
    }

    private static final class ActorMotion {
        private static final long MIN_INTERVAL=75_000_000L, MAX_INTERVAL=250_000_000L;
        final EntityLivingBase entity;
        PacketSilverMirrorScene.Actor target;
        double x,y,z;
        float yaw,pitch,head;
        float startSwing, stride;
        long started, interval=100_000_000L;

        ActorMotion(EntityLivingBase entity,PacketSilverMirrorScene.Actor actor,long now) {
            this.entity=entity;
            target=actor;
            started=now;
            x=actor.x; y=actor.y; z=actor.z;
            yaw=actor.yaw; pitch=actor.pitch; head=actor.head;
            updateAppearance(actor);
            apply(now);
        }

        void retarget(PacketSilverMirrorScene.Actor actor,long now) {
            apply(now);
            x=entity.posX; y=entity.posY; z=entity.posZ;
            yaw=entity.renderYawOffset; pitch=entity.rotationPitch; head=entity.rotationYawHead;
            startSwing=entity.limbSwing;
            interval=Math.max(MIN_INTERVAL,Math.min(MAX_INTERVAL,now-started));
            started=now;
            double dx=actor.x-x,dz=actor.z-z;
            stride=Math.min(1F, (float)(Math.sqrt(dx*dx+dz*dz)*4));
            if (dx*dx+(actor.y-y)*(actor.y-y)+dz*dz>16) {
                x=actor.x; y=actor.y; z=actor.z;
                stride=0;
            }
            target=actor;
            updateAppearance(actor);
        }

        private void updateAppearance(PacketSilverMirrorScene.Actor actor) {
            if (actor.watched!=null) entity.getDataWatcher().updateWatchedObjectsFromList(actor.watched);
            for (int slot=0;actor.sendEquipment && slot<actor.equipment.length;slot++) {
                net.minecraft.item.ItemStack stack=actor.equipment[slot];
                if (!net.minecraft.item.ItemStack.areItemStacksEqual(stack,entity.getEquipmentInSlot(slot))) {
                    entity.setCurrentItemOrArmor(slot,stack);
                }
            }
            if (entity instanceof net.minecraft.entity.player.EntityPlayer) {
                net.minecraft.entity.player.EntityPlayer player=(net.minecraft.entity.player.EntityPlayer)entity;
                if (actor.useTicks>0 && player.getHeldItem()!=null) {
                    // Clear first so setItemInUse refreshes the animation timer even for the same stack.
                    player.clearItemInUse();
                    player.setItemInUse(player.getHeldItem(),actor.useTicks);
                } else if (player.isUsingItem()) player.clearItemInUse();
            }
            if (entity instanceof EntityAgeable && entity.isChild()!=actor.child) {
                ((EntityAgeable)entity).setGrowingAge(actor.child?-24000:0);
            }
        }

        void apply(long now) {
            float t=(float)Math.max(0,Math.min(1,(now-started)/(double)interval));
            float look=angle(head,target.head,t), body=angle(yaw,target.yaw,t);
            float lookPitch=pitch+(target.pitch-pitch)*t;
            entity.setPositionAndRotation(x+(target.x-x)*t,y+(target.y-y)*t,z+(target.z-z)*t,look,lookPitch);
            entity.lastTickPosX=entity.prevPosX=entity.posX;
            entity.lastTickPosY=entity.prevPosY=entity.posY;
            entity.lastTickPosZ=entity.prevPosZ=entity.posZ;
            entity.prevRotationYaw=look;
            entity.prevRotationPitch=lookPitch;
            entity.renderYawOffset=entity.prevRenderYawOffset=body;
            entity.rotationYawHead=entity.prevRotationYawHead=look;
            entity.prevSwingProgress=entity.swingProgress=target.swing;
            entity.limbSwing=startSwing+stride*t*2;
            entity.prevLimbSwingAmount=entity.limbSwingAmount=stride;
            entity.ticksExisted=(int)(now/50_000_000L&0x7fff);
        }

        private static float angle(float from,float to,float t) {
            return from+net.minecraft.util.MathHelper.wrapAngleTo180_float(to-from)*t;
        }
    }
}
