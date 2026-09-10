package ru.givler.mbo.movingplatform;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

/** Broad-phase platform collision pass with cached active platforms and surface blocks. */
public final class MovingPlatformTickHandler {
    private static final Map<World,Set<EntityMovingPlatform>> ACTIVE=new WeakHashMap<World,Set<EntityMovingPlatform>>();
    private static final Map<World,Map<UUID,Integer>> CARRIERS=new WeakHashMap<World,Map<UUID,Integer>>();
    private static final Map<World,Map<Entity,Integer>> ENTITY_CARRIERS=new WeakHashMap<World,Map<Entity,Integer>>();
    private static final Map<World,Map<Entity,RideOffset>> CLIENT_OFFSETS=new WeakHashMap<World,Map<Entity,RideOffset>>();

    public static void track(EntityMovingPlatform platform){
        if(platform.worldObj==null)return;Set<EntityMovingPlatform> set=ACTIVE.get(platform.worldObj);
        if(set==null){set=Collections.newSetFromMap(new IdentityHashMap<EntityMovingPlatform,Boolean>());ACTIVE.put(platform.worldObj,set);}set.add(platform);
    }
    public static void untrack(EntityMovingPlatform platform){
        World world=platform.worldObj;if(world==null)return;
        Set<EntityMovingPlatform> set=ACTIVE.get(world);if(set!=null)set.remove(platform);
        int entityId=platform.getEntityId();
        Map<UUID,Integer> players=CARRIERS.get(world);if(players!=null)removeCarrierId(players,entityId);
        Map<Entity,Integer> entities=ENTITY_CARRIERS.get(world);if(entities!=null)removeCarrierId(entities,entityId);
        Map<Entity,RideOffset> offsets=CLIENT_OFFSETS.get(world);if(offsets!=null){
            for(Iterator<Map.Entry<Entity,RideOffset>> it=offsets.entrySet().iterator();it.hasNext();)if(it.next().getValue().platformId==entityId)it.remove();
        }
    }

    private static <K> void removeCarrierId(Map<K,Integer> map,int entityId){
        for(Iterator<Map.Entry<K,Integer>> it=map.entrySet().iterator();it.hasNext();)if(it.next().getValue().intValue()==entityId)it.remove();
    }

    @SubscribeEvent public void onPlayerLogout(cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent event){
        if(event.player==null||event.player.worldObj==null)return;
        Map<UUID,Integer> carriers=CARRIERS.get(event.player.worldObj);
        if(carriers!=null)carriers.remove(event.player.getUniqueID());
    }

    @SubscribeEvent public void onServerTick(TickEvent.ServerTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;MinecraftServer server=MinecraftServer.getServer();
        if(server==null||server.worldServers==null)return;for(WorldServer world:server.worldServers)if(world!=null)processWorld(world);
    }

    public static void processWorld(World world){
        if(world.isRemote)return;
        Set<EntityMovingPlatform> platforms=ACTIVE.get(world);if(platforms==null||platforms.isEmpty())return;
        Map<UUID,Integer> carriers=CARRIERS.get(world);if(carriers==null){carriers=new java.util.HashMap<UUID,Integer>();CARRIERS.put(world,carriers);}
        Map<Entity,Integer> entityCarriers=entityCarriers(world);
        for(Iterator<EntityMovingPlatform> it=platforms.iterator();it.hasNext();){EntityMovingPlatform platform=it.next();if(platform==null||platform.isDead||platform.worldObj!=world){it.remove();continue;}if(!platform.isCollisionActive())continue;
            AxisAlignedBB broad=bounds(platform).expand(1.5D,2.5D,1.5D);
            for(Object object:world.getEntitiesWithinAABBExcludingEntity(platform,broad)){if(!(object instanceof Entity))continue;Entity entity=(Entity)object;
                if(entity instanceof EntityMovingPlatform||entity.isDead||entity.boundingBox==null||!entity.boundingBox.intersectsWith(broad))continue;
                if(entity instanceof IProjectile){stopProjectile(entity,platform);continue;}
                if(entity instanceof EntityPlayer)processPlayer((EntityPlayer)entity,platform,carriers);else processEntity(entity,platform,entityCarriers,null,false);
            }
        }
    }

    public static void processClientPlayer(World world,EntityPlayer player){
        if(world==null||player==null)return;Set<EntityMovingPlatform> platforms=ACTIVE.get(world);if(platforms==null||platforms.isEmpty())return;
        Map<UUID,Integer> carriers=CARRIERS.get(world);if(carriers==null){carriers=new java.util.HashMap<UUID,Integer>();CARRIERS.put(world,carriers);}
        for(EntityMovingPlatform platform:platforms)if(platform!=null&&!platform.isDead&&platform.isCollisionActive()&&player.boundingBox.intersectsWith(bounds(platform).expand(1.5D,2.5D,1.5D)))processPlayer(player,platform,carriers);
        Map<Entity,Integer> entityCarriers=entityCarriers(world);
        Map<Entity,RideOffset> offsets=clientOffsets(world);
        for(EntityMovingPlatform platform:platforms)if(platform!=null&&!platform.isDead&&platform.isCollisionActive()){
            AxisAlignedBB broad=bounds(platform).expand(1.5D,2.5D,1.5D);
            for(Object object:world.getEntitiesWithinAABBExcludingEntity(platform,broad))if(object instanceof Entity&&!(object instanceof EntityPlayer)&&!(object instanceof EntityMovingPlatform)&&!(object instanceof IProjectile)){
                Entity entity=(Entity)object;if(!entity.isDead&&entity.boundingBox!=null&&entity.boundingBox.intersectsWith(broad))processEntity(entity,platform,entityCarriers,offsets,true);
            }
        }
    }

    private static Map<Entity,Integer> entityCarriers(World world){Map<Entity,Integer> map=ENTITY_CARRIERS.get(world);if(map==null){map=new WeakHashMap<Entity,Integer>();ENTITY_CARRIERS.put(world,map);}return map;}
    private static Map<Entity,RideOffset> clientOffsets(World world){Map<Entity,RideOffset> map=CLIENT_OFFSETS.get(world);if(map==null){map=new WeakHashMap<Entity,RideOffset>();CLIENT_OFFSETS.put(world,map);}return map;}

    private static AxisAlignedBB bounds(EntityMovingPlatform p){
        double minX=Math.min(p.prevPosX,p.posX),minY=Math.min(p.prevPosY,p.posY),minZ=Math.min(p.prevPosZ,p.posZ);
        double maxX=Math.max(p.prevPosX,p.posX)+p.getSizeX(),maxY=Math.max(p.prevPosY,p.posY)+p.getSizeY(),maxZ=Math.max(p.prevPosZ,p.posZ)+p.getSizeZ();
        return AxisAlignedBB.getBoundingBox(minX,minY,minZ,maxX,maxY,maxZ);
    }

    private static void stopProjectile(Entity projectile,EntityMovingPlatform platform){
        Vec3 from=Vec3.createVectorHelper(projectile.prevPosX,projectile.prevPosY,projectile.prevPosZ),to=Vec3.createVectorHelper(projectile.posX,projectile.posY,projectile.posZ);
        for(PlatformBlock block:platform.getOuterBlocks()){AxisAlignedBB box=blockBox(platform,block).expand(.15D,.15D,.15D);
            if(box.isVecInside(to)||box.calculateIntercept(from,to)!=null){projectile.setDead();return;}
        }
    }

    private static void processEntity(Entity entity,EntityMovingPlatform platform,Map<Entity,Integer> carriers,Map<Entity,RideOffset> offsets,boolean clientOnly){
        double dx=platform.posX-platform.prevPosX,dy=platform.posY-platform.prevPosY,dz=platform.posZ-platform.prevPosZ;
        Integer previous=carriers.get(entity);if(previous!=null&&previous.intValue()==platform.getEntityId()&&entity.motionY>.1D){carriers.remove(entity);if(offsets!=null)offsets.remove(entity);return;}
        RideOffset ride=offsets==null?null:offsets.get(entity);
        if(clientOnly&&previous!=null&&previous.intValue()==platform.getEntityId()&&ride!=null&&ride.platformId==platform.getEntityId()){
            ride.x+=entity.motionX;ride.z+=entity.motionZ;entity.setPosition(platform.posX+ride.x,entity.posY,platform.posZ+ride.z);
        }
        double feet=feet(entity);
        for(PlatformBlock block:platform.getTopBlocks()){double oldTop=platform.prevPosY+block.y+1D,newTop=platform.posY+block.y+1D;
            if(!overlapXZ(entity,platform.posX+block.x,platform.posZ+block.z))continue;double oldGap=previousFeet(entity)-oldTop,newGap=feet-newTop;
            boolean remembered=previous!=null&&previous.intValue()==platform.getEntityId()&&newGap>=-.65D-Math.abs(dy)&&newGap<=.3D+Math.abs(dy);
            boolean landing=oldGap>=-.12D&&newGap<=.18D&&newGap>=-.55D&&entity.motionY<=.2D;
            boolean resting=newGap>=-.35D&&newGap<=.12D&&entity.motionY<=.05D;
            if(remembered||landing||resting){snapFeet(entity,oldTop);entity.moveEntity(clientOnly?0D:dx,dy,clientOnly?0D:dz);if(entity.motionY<0D)entity.motionY=0D;entity.onGround=true;entity.fallDistance=0;carriers.put(entity,platform.getEntityId());if(clientOnly&&(ride==null||ride.platformId!=platform.getEntityId()))offsets.put(entity,new RideOffset(platform.getEntityId(),entity.posX-platform.posX,entity.posZ-platform.posZ));return;}}
        if(previous!=null&&previous.intValue()==platform.getEntityId()){carriers.remove(entity);if(offsets!=null)offsets.remove(entity);}
        if(clientOnly)return;
        if(dx==0D&&dy==0D&&dz==0D)return;
        for(PlatformBlock block:platform.getOuterBlocks()){AxisAlignedBB box=blockBox(platform,block);if(!entity.boundingBox.intersectsWith(box))continue;
            double beforeX=entity.posX,beforeY=entity.posY,beforeZ=entity.posZ;
            double pushX=dx>0?box.maxX-entity.boundingBox.minX:dx<0?box.minX-entity.boundingBox.maxX:0D;
            double pushY=dy>0?box.maxY-entity.boundingBox.minY:dy<0?box.minY-entity.boundingBox.maxY:0D;
            double pushZ=dz>0?box.maxZ-entity.boundingBox.minZ:dz<0?box.minZ-entity.boundingBox.maxZ:0D;
            entity.moveEntity(pushX,pushY,pushZ);
            boolean blocked=Math.abs(entity.posX-beforeX-pushX)>.02D||Math.abs(entity.posY-beforeY-pushY)>.02D||Math.abs(entity.posZ-beforeZ-pushZ)>.02D;
            if(blocked&&entity instanceof EntityLivingBase&&entity.ticksExisted%10==0)entity.attackEntityFrom(DamageSource.inWall,4F);
            if(dy>0){entity.onGround=true;entity.fallDistance=0;}return;
        }
    }

    private static void processPlayer(EntityPlayer player,EntityMovingPlatform platform,Map<UUID,Integer> carriers){
        UUID id=player.getUniqueID();Integer previous=carriers.get(id);double feet=feet(player),head=player.boundingBox.maxY;
        double dx=platform.posX-platform.prevPosX,dy=platform.posY-platform.prevPosY,dz=platform.posZ-platform.prevPosZ;
        if(previous!=null&&previous.intValue()==platform.getEntityId()&&player.motionY>.05D){carriers.remove(id);return;}
        for(PlatformBlock block:platform.getBottomBlocks()){double x=platform.posX+block.x,z=platform.posZ+block.z,bottom=platform.posY+block.y,oldBottom=platform.prevPosY+block.y;
            if(overlapXZ(player,x,z)&&oldBottom-(head+player.prevPosY-player.posY)>=-.08D&&bottom-head<=.001D&&player.boundingBox.minY<bottom){snapHead(player,bottom-.001D);if(player.motionY>0)player.motionY=0;carriers.remove(id);return;}}
        PlatformBlock chosen=null;double best=Double.MAX_VALUE;
        for(PlatformBlock block:platform.getTopBlocks()){double oldTop=platform.prevPosY+block.y+1D,newTop=platform.posY+block.y+1D;
            if(!overlapXZ(player,platform.posX+block.x,platform.posZ+block.z))continue;double oldGap=previousFeet(player)-oldTop,newGap=feet-newTop;
            boolean descendingCrossing=oldGap>=-.08D&&newGap<=.08D&&player.motionY<=0D;
            boolean remembered=previous!=null&&previous.intValue()==platform.getEntityId()&&player.motionY<=.05D&&newGap>=-.55D-Math.abs(dy)&&newGap<=.12D+Math.abs(dy);
            boolean contact=newGap>=-.38D&&newGap<=.12D&&player.motionY<=0D;
            if((descendingCrossing||remembered||contact)&&Math.abs(newGap)<best){chosen=block;best=Math.abs(newGap);}}
        if(chosen!=null){snapFeet(player,platform.prevPosY+chosen.y+1D);player.moveEntity(dx,dy,dz);if(player.motionY<0)player.motionY=0;player.onGround=true;player.fallDistance=0;carriers.put(id,platform.getEntityId());}
        else if(previous!=null&&previous.intValue()==platform.getEntityId())carriers.remove(id);
    }

    private static AxisAlignedBB blockBox(EntityMovingPlatform p,PlatformBlock b){return AxisAlignedBB.getBoundingBox(p.posX+b.x,p.posY+b.y,p.posZ+b.z,p.posX+b.x+1D,p.posY+b.y+1D,p.posZ+b.z+1D);}
    private static boolean overlapXZ(Entity p,double x,double z){return p.boundingBox.maxX>x+.02D&&p.boundingBox.minX<x+.98D&&p.boundingBox.maxZ>z+.02D&&p.boundingBox.minZ<z+.98D;}
    private static double feet(Entity p){return p.boundingBox.minY;}
    private static double previousFeet(Entity p){return feet(p)+(p.prevPosY-p.posY);}
    private static void snapFeet(Entity p,double target){double d=target-feet(p);if(Math.abs(d)>1E-9)p.setPosition(p.posX,p.posY+d,p.posZ);}
    private static void snapHead(Entity p,double target){double d=target-p.boundingBox.maxY;if(Math.abs(d)>1E-9)p.setPosition(p.posX,p.posY+d,p.posZ);}
    private static final class RideOffset{final int platformId;double x,z;RideOffset(int platformId,double x,double z){this.platformId=platformId;this.x=x;this.z=z;}}
}
