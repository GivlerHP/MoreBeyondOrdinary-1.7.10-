package ru.givler.mbo.dungeon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.tileentity.TileEntity;
import ru.givler.mbo.lootcontainer.action.LootContainerAction;
import ru.givler.mbo.tileentity.TileEntityLootContainer;
import ru.givler.mbo.movingplatform.PlatformBlock;

/** Serializable wall state shared by server storage and the client render mirror. */
public final class DungeonAreaRecord {
    public static final int PASSABLE=0,ILLUSORY=1,COLLIDER=2,TRIGGER=3;
    private UUID id=UUID.randomUUID();
    private int dimension,x,y,z,sizeX=1,sizeY=1,sizeZ=1,type,restoreMode,restoreSeconds=10;
    private boolean activated;
    private boolean signalPowered;
    private boolean restoreRequested;
    private int repeatMode,cooldownSeconds=5;
    private boolean triggerPlayers=true,triggerEntities,triggerProjectiles,triggerExplosions,allowMultiaction,triggerConsumed;
    private long nextTriggerTick;
    private String actionsJson="[]";
    private final java.util.HashSet<Integer> insideEntities=new java.util.HashSet<Integer>();
    private int dirtySerial;
    private long activationTime;
    private final List<PlatformBlock> blocks=new ArrayList<PlatformBlock>();
    private int[] lightField=new int[0];

    public DungeonAreaRecord(){}
    public DungeonAreaRecord(int dimension,int x,int y,int z,int sx,int sy,int sz,int type,int restoreMode,int restoreSeconds,List<PlatformBlock> blocks,int[] light){
        this.dimension=dimension;this.x=x;this.y=y;this.z=z;sizeX=sx;sizeY=sy;sizeZ=sz;this.type=type;this.restoreMode=restoreMode;this.restoreSeconds=restoreSeconds;this.blocks.addAll(blocks);lightField=light;
    }
    public UUID getId(){return id;} public String idString(){return id.toString();}
    public int getDimension(){return dimension;} public int getX(){return x;} public int getY(){return y;} public int getZ(){return z;}
    public int getSizeX(){return sizeX;} public int getSizeY(){return sizeY;} public int getSizeZ(){return sizeZ;}
    public int getType(){return type;} public int getRestoreMode(){return restoreMode;} public int getRestoreSeconds(){return restoreSeconds;}
    public int getRepeatMode(){return repeatMode;} public int getCooldownSeconds(){return cooldownSeconds;} public boolean triggersPlayers(){return triggerPlayers;} public boolean triggersEntities(){return triggerEntities;} public boolean triggersProjectiles(){return triggerProjectiles;} public boolean triggersExplosions(){return triggerExplosions;} public boolean allowsMultiaction(){return allowMultiaction;} public String getActionsJson(){return actionsJson;}
    public boolean isActivated(){return activated;} public List<PlatformBlock> getBlocks(){return Collections.unmodifiableList(blocks);} public int[] getLightField(){return lightField;}
    public int runtimeState(){return (activated?1:0)|(signalPowered?2:0)|(restoreRequested?4:0)|(dirtySerial<<3);}
    public void configure(int type,int restoreMode,int restoreSeconds){this.restoreMode=Math.max(0,Math.min(2,restoreMode));this.restoreSeconds=Math.max(1,Math.min(86400,restoreSeconds));}
    public void configureTrigger(int repeatMode,int cooldownSeconds,boolean players,boolean entities,boolean projectiles,boolean explosions,boolean multi,String actions){this.repeatMode=Math.max(0,Math.min(2,repeatMode));this.cooldownSeconds=Math.max(1,Math.min(86400,cooldownSeconds));triggerPlayers=players;triggerEntities=entities;triggerProjectiles=projectiles;triggerExplosions=explosions;allowMultiaction=multi;actionsJson=actions==null?"[]":actions.substring(0,Math.min(actions.length(),65536));triggerConsumed=false;nextTriggerTick=0L;insideEntities.clear();}
    public boolean occupies(int wx,int wy,int wz){for(PlatformBlock b:blocks)if(x+b.x==wx&&y+b.y==wy&&z+b.z==wz)return true;return false;}
    public boolean overlaps(DungeonAreaRecord other){for(PlatformBlock b:blocks)if(other.occupies(x+b.x,y+b.y,z+b.z))return true;return false;}
    public boolean containsPoint(double px,double py,double pz){if(type==TRIGGER)return px>x&&px<x+sizeX&&py>y&&py<y+sizeY&&pz>z&&pz<z+sizeZ;for(PlatformBlock b:blocks)if(px>x+b.x&&px<x+b.x+1D&&py>y+b.y&&py<y+b.y+1D&&pz>z+b.z&&pz<z+b.z+1D)return true;return false;}
    public boolean isNear(EntityPlayer player,double maxSq){if(type==TRIGGER)return player.getDistanceSq(x+sizeX*.5D,y+sizeY*.5D,z+sizeZ*.5D)<=maxSq+sizeX*sizeX+sizeY*sizeY+sizeZ*sizeZ;for(PlatformBlock b:blocks)if(player.getDistanceSq(x+b.x+.5D,y+b.y+.5D,z+b.z+.5D)<=maxSq)return true;return false;}
    public boolean shouldRender(){return type==PASSABLE||(type==ILLUSORY&&activated);}
    public float fadeAlpha(World world,float partial){if(type!=ILLUSORY||!activated)return 1F;return Math.max(0F,1F-((world.getTotalWorldTime()-activationTime)+partial)/20F);}
    public boolean activate(World world){if(type!=ILLUSORY||activated)return false;for(PlatformBlock b:blocks)if(world.getBlock(x+b.x,y+b.y,z+b.z)!=b.block||world.getBlockMetadata(x+b.x,y+b.y,z+b.z)!=b.meta)return false;signalPowered=isPowered(world);for(PlatformBlock b:blocks)world.setBlock(x+b.x,y+b.y,z+b.z,Blocks.air,0,3);activated=true;activationTime=world.getTotalWorldTime();return true;}
    /** @return true when the record should be deleted. */
    public boolean tick(World world){if(type==TRIGGER){tickTrigger(world);return false;}if(type!=ILLUSORY||!activated||world.getTotalWorldTime()-activationTime<20)return false;if(restoreMode==0)return true;if(restoreMode==1&&world.getTotalWorldTime()-activationTime>=20L+restoreSeconds*20L&&restore(world))activated=false;if(restoreMode==2){boolean powered=isPowered(world);if(powered&&!signalPowered)restoreRequested=true;signalPowered=powered;if(restoreRequested&&restore(world)){restoreRequested=false;activated=false;}}return false;}
    @SuppressWarnings("unchecked") private void tickTrigger(World world){if(triggerConsumed&&repeatMode==0)return;AxisAlignedBB box=AxisAlignedBB.getBoundingBox(x,y,z,x+sizeX,y+sizeY,z+sizeZ);java.util.List<Entity> found=world.getEntitiesWithinAABB(Entity.class,box);java.util.HashSet<Integer> now=new java.util.HashSet<Integer>();Entity candidate=null;for(Entity e:found)if(matchesTriggerEntity(e)){now.add(e.getEntityId());if(candidate==null||!insideEntities.contains(e.getEntityId()))candidate=e;}long time=world.getTotalWorldTime();if(repeatMode==2){if(!found.isEmpty()&&time>=nextTriggerTick){for(Entity e:found)if(matchesTriggerEntity(e)){fireTrigger(world,e);nextTriggerTick=time+cooldownSeconds*20L;break;}}}else if(candidate!=null){fireTrigger(world,candidate);if(repeatMode==0)triggerConsumed=true;}insideEntities.clear();insideEntities.addAll(now);}
    private boolean matchesTriggerEntity(Entity e){if(e==null||e.isDead)return false;if(e instanceof EntityPlayer)return triggerPlayers;if(e instanceof EntityArrow||e instanceof net.minecraft.entity.IProjectile)return triggerProjectiles;return triggerEntities&&e instanceof EntityLivingBase;}
    public boolean triggerExplosion(World world,double ex,double ey,double ez){if(type!=TRIGGER||!triggerExplosions||triggerConsumed&&repeatMode==0||!containsPoint(ex,ey,ez)||repeatMode==2&&world.getTotalWorldTime()<nextTriggerTick)return false;fireTrigger(world,null);if(repeatMode==0)triggerConsumed=true;if(repeatMode==2)nextTriggerTick=world.getTotalWorldTime()+cooldownSeconds*20L;return true;}
    private void fireTrigger(World world,Entity source){DungeonTriggerExecutor.execute(world,x+sizeX/2,y,z+sizeZ/2,source,actionsJson,allowMultiaction);dirtySerial++;}
    private boolean restore(World world){for(PlatformBlock b:blocks){int wx=x+b.x,wy=y+b.y,wz=z+b.z;if(world.getBlock(wx,wy,wz)!=Blocks.air||hasEntity(world,wx,wy,wz))return false;}for(PlatformBlock b:blocks)world.setBlock(x+b.x,y+b.y,z+b.z,b.block,b.meta,3);return true;}
    private boolean hasEntity(World world,int wx,int wy,int wz){AxisAlignedBB box=AxisAlignedBB.getBoundingBox(wx,wy,wz,wx+1D,wy+1D,wz+1D).contract(0.001D,0.001D,0.001D);for(Object value:world.getEntitiesWithinAABBExcludingEntity(null,box))if(value instanceof Entity&&!((Entity)value).isDead)return true;return false;}
    private boolean isPowered(World world){for(PlatformBlock b:blocks)if(world.isBlockIndirectlyGettingPowered(x+b.x,y+b.y,z+b.z))return true;return false;}
    public void deleteBlocks(World world){if(type!=ILLUSORY||activated)return;for(PlatformBlock b:blocks){int wx=x+b.x,wy=y+b.y,wz=z+b.z;if(world.getBlock(wx,wy,wz)==b.block&&world.getBlockMetadata(wx,wy,wz)==b.meta)world.setBlock(wx,wy,wz,Blocks.air,0,3);}}
    public NBTTagCompound write(){NBTTagCompound tag=new NBTTagCompound();tag.setString("Id",id.toString());tag.setInteger("Dimension",dimension);tag.setInteger("X",x);tag.setInteger("Y",y);tag.setInteger("Z",z);tag.setInteger("SizeX",sizeX);tag.setInteger("SizeY",sizeY);tag.setInteger("SizeZ",sizeZ);tag.setInteger("Type",type);tag.setInteger("RestoreMode",restoreMode);tag.setInteger("RestoreSeconds",restoreSeconds);tag.setBoolean("Activated",activated);tag.setBoolean("SignalPowered",signalPowered);tag.setBoolean("RestoreRequested",restoreRequested);tag.setLong("ActivationTime",activationTime);tag.setInteger("RepeatMode",repeatMode);tag.setInteger("CooldownSeconds",cooldownSeconds);tag.setBoolean("TriggerPlayers",triggerPlayers);tag.setBoolean("TriggerEntities",triggerEntities);tag.setBoolean("TriggerProjectiles",triggerProjectiles);tag.setBoolean("TriggerExplosions",triggerExplosions);tag.setBoolean("AllowMultiaction",allowMultiaction);tag.setBoolean("TriggerConsumed",triggerConsumed);tag.setLong("NextTriggerTick",nextTriggerTick);tag.setString("Actions",actionsJson);tag.setIntArray("LightField",lightField);NBTTagList list=new NBTTagList();for(PlatformBlock b:blocks)list.appendTag(b.write());tag.setTag("Blocks",list);return tag;}
    public static DungeonAreaRecord read(NBTTagCompound tag){DungeonAreaRecord r=new DungeonAreaRecord();try{r.id=UUID.fromString(tag.getString("Id"));}catch(Exception ignored){}r.dimension=tag.getInteger("Dimension");r.x=tag.getInteger("X");r.y=tag.getInteger("Y");r.z=tag.getInteger("Z");r.sizeX=Math.max(1,tag.getInteger("SizeX"));r.sizeY=Math.max(1,tag.getInteger("SizeY"));r.sizeZ=Math.max(1,tag.getInteger("SizeZ"));r.type=tag.getInteger("Type");r.restoreMode=tag.getInteger("RestoreMode");r.restoreSeconds=Math.max(1,tag.getInteger("RestoreSeconds"));r.activated=tag.getBoolean("Activated");r.signalPowered=tag.getBoolean("SignalPowered");r.restoreRequested=tag.getBoolean("RestoreRequested");r.activationTime=tag.getLong("ActivationTime");r.repeatMode=tag.getInteger("RepeatMode");r.cooldownSeconds=Math.max(1,tag.getInteger("CooldownSeconds"));r.triggerPlayers=!tag.hasKey("TriggerPlayers")||tag.getBoolean("TriggerPlayers");r.triggerEntities=tag.getBoolean("TriggerEntities");r.triggerProjectiles=tag.getBoolean("TriggerProjectiles");r.triggerExplosions=tag.getBoolean("TriggerExplosions");r.allowMultiaction=tag.getBoolean("AllowMultiaction");r.triggerConsumed=tag.getBoolean("TriggerConsumed");r.nextTriggerTick=tag.getLong("NextTriggerTick");r.actionsJson=tag.hasKey("Actions")?tag.getString("Actions"):"[]";r.lightField=tag.getIntArray("LightField");NBTTagList list=tag.getTagList("Blocks",10);for(int i=0;i<list.tagCount();i++){PlatformBlock b=PlatformBlock.read(list.getCompoundTagAt(i));if(b!=null)r.blocks.add(b);}return r;}
}
