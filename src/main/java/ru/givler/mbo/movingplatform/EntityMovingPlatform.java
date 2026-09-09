package ru.givler.mbo.movingplatform;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

public class EntityMovingPlatform extends Entity implements IEntityAdditionalSpawnData {
    public static final int STOPPED_A = 0, MOVING_TO_B = 1, STOPPED_B = 2, MOVING_TO_A = 3;
    private final List<PlatformBlock> blocks = new ArrayList<PlatformBlock>();
    private final List<PlatformBlock> outerBlocks=new ArrayList<PlatformBlock>();
    private final List<PlatformBlock> topBlocks=new ArrayList<PlatformBlock>();
    private final List<PlatformBlock> bottomBlocks=new ArrayList<PlatformBlock>();
    private UUID platformId = UUID.randomUUID();
    private UUID ownerId;
    private int sizeX = 1, sizeY = 1, sizeZ = 1;
    private int direction;
    private int distance = 3;
    private int durationTicks = 60;
    private int state = STOPPED_A;
    private int motionTick;
    private int movementStartTick;
    private int returnMode = 2, delayTicks = 20, waitTicks;
    private boolean configured;
    private boolean virtualized;
    private boolean pendingConfiguration;
    private int pendingDirection, pendingDistance, pendingDurationTicks, pendingReturnMode, pendingDelayTicks;
    private double homeX, homeY, homeZ;
    private double lerpX, lerpY, lerpZ;
    private int lerpSteps;

    public EntityMovingPlatform(World world) {
        super(world);
        noClip = true;
        ignoreFrustumCheck = true;
        preventEntitySpawning = false;
        setSize(1F, 1F);
    }

    @Override protected void entityInit() {
        dataWatcher.addObject(20, Integer.valueOf(STOPPED_A));
        dataWatcher.addObject(21, Integer.valueOf(0));
        dataWatcher.addObject(22, Integer.valueOf(3));
        dataWatcher.addObject(23, Integer.valueOf(60));
        dataWatcher.addObject(24, Integer.valueOf(0));
        dataWatcher.addObject(25, Byte.valueOf((byte)0));
    }

    public static EntityMovingPlatform create(World world, EntityPlayer owner,
                                               int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        EntityMovingPlatform entity = new EntityMovingPlatform(world);
        entity.homeX = minX; entity.homeY = minY; entity.homeZ = minZ;
        entity.sizeX = maxX - minX + 1; entity.sizeY = maxY - minY + 1; entity.sizeZ = maxZ - minZ + 1;
        entity.ownerId = owner == null ? null : owner.getUniqueID();
        entity.setPosition(minX, minY, minZ);
        return entity.rebuild(owner) ? entity : null;
    }

    public boolean rebuild(EntityPlayer feedback) {
        if (isMoving()) return false;
        List<PlatformBlock> found = new ArrayList<PlatformBlock>();
        int ox = floor(posX), oy = floor(posY), oz = floor(posZ);
        for (int x = 0; x < sizeX; x++) for (int y = 0; y < sizeY; y++) for (int z = 0; z < sizeZ; z++) {
            int wx = ox + x, wy = oy + y, wz = oz + z;
            Block block = worldObj.getBlock(wx, wy, wz);
            if (block == Blocks.air) continue;
            TileEntity tile = worldObj.getTileEntity(wx, wy, wz);
            if (tile != null || block.getBlockHardness(worldObj, wx, wy, wz) < 0F) {
                if (feedback != null) feedback.addChatMessage(new ChatComponentTranslation("mbo.platform.error.unsupported", wx, wy, wz));
                return false;
            }
            found.add(new PlatformBlock(x, y, z, block, worldObj.getBlockMetadata(wx, wy, wz)));
        }
        if (found.isEmpty()) {
            if (feedback != null) feedback.addChatMessage(new ChatComponentTranslation("mbo.platform.error.empty"));
            return false;
        }
        blocks.clear(); blocks.addAll(found);rebuildCollisionCache();
        return true;
    }

    public boolean start(boolean toB, EntityPlayer feedback) {
        if (!configured) {
            if (feedback != null) feedback.addChatMessage(new ChatComponentTranslation("mbo.platform.error.notConfigured"));
            return false;
        }
        if (isMoving() || (toB && state == STOPPED_B) || (!toB && state == STOPPED_A)) return false;
        int targetOffset = toB ? distance : 0;
        if (!routeIsClear(targetOffset)) {
            if (feedback != null) feedback.addChatMessage(new ChatComponentTranslation("mbo.platform.error.blocked"));
            return false;
        }
        removeMaterializedBlocks();
        setVirtualized(true);
        state = toB ? MOVING_TO_B : MOVING_TO_A;
        dataWatcher.updateObject(20, Integer.valueOf(state));
        movementStartTick = worldTick();
        dataWatcher.updateObject(24, Integer.valueOf(movementStartTick));
        motionTick = 0;
        return true;
    }

    private boolean routeIsClear(int targetOffset) {
        PlatformDirection d = getDirection();
        int currentOffset = state == STOPPED_B ? distance : 0;
        int step = targetOffset >= currentOffset ? 1 : -1;
        for (int offset = currentOffset + step; offset != targetOffset + step; offset += step) {
            for (PlatformBlock saved : blocks) {
                int x = floor(homeX) + saved.x + d.x * offset;
                int y = floor(homeY) + saved.y + d.y * offset;
                int z = floor(homeZ) + saved.z + d.z * offset;
                if (!worldObj.blockExists(x, y, z)) return false;
                if (worldObj.getBlock(x, y, z) != Blocks.air && !isCurrentPlatformBlock(x,y,z)) return false;
            }
        }
        return true;
    }

    private boolean isCurrentPlatformBlock(int x,int y,int z){
        int ox=floor(posX),oy=floor(posY),oz=floor(posZ);
        for(PlatformBlock b:blocks)if(x==ox+b.x&&y==oy+b.y&&z==oz+b.z&&worldObj.getBlock(x,y,z)==b.block&&worldObj.getBlockMetadata(x,y,z)==b.meta)return true;
        return false;
    }

    private void removeMaterializedBlocks() {
        int ox = floor(posX), oy = floor(posY), oz = floor(posZ);
        for (PlatformBlock saved : blocks) worldObj.setBlock(ox + saved.x, oy + saved.y, oz + saved.z, Blocks.air, 0, 2);
    }

    private void materialize() {
        int ox = floor(posX), oy = floor(posY), oz = floor(posZ);
        for (PlatformBlock saved : blocks) worldObj.setBlock(ox + saved.x, oy + saved.y, oz + saved.z, saved.block, saved.meta, 2);
        setVirtualized(false);
    }

    public boolean reset(EntityPlayer feedback) {
        if (isMoving()) return false;
        int ox = floor(posX), oy = floor(posY), oz = floor(posZ);
        for (int x = 0; x < sizeX; x++) for (int y = 0; y < sizeY; y++) for (int z = 0; z < sizeZ; z++)
            worldObj.setBlock(ox + x, oy + y, oz + z, Blocks.air, 0, 2);
        for (int x = 0; x < sizeX; x++) for (int y = 0; y < sizeY; y++) for (int z = 0; z < sizeZ; z++)
            worldObj.setBlock(floor(homeX) + x, floor(homeY) + y, floor(homeZ) + z, Blocks.air, 0, 2);
        setPosition(homeX, homeY, homeZ);
        state = STOPPED_A; dataWatcher.updateObject(20, Integer.valueOf(state)); motionTick = 0; materialize(); return true;
    }

    public boolean stopAndReturn(EntityPlayer feedback){
        if(!isMoving())return reset(feedback);
        int ox=floor(homeX),oy=floor(homeY),oz=floor(homeZ);
        for(PlatformBlock saved:blocks)if(worldObj.getBlock(ox+saved.x,oy+saved.y,oz+saved.z)!=Blocks.air){
            if(feedback!=null)feedback.addChatMessage(new ChatComponentTranslation("mbo.platform.error.blocked"));return false;
        }
        setPosition(homeX,homeY,homeZ);state=STOPPED_A;motionTick=0;waitTicks=0;pendingConfiguration=false;
        dataWatcher.updateObject(20,Integer.valueOf(state));materialize();return true;
    }

    @Override public void onUpdate() {
        MovingPlatformTickHandler.track(this);
        prevPosX = posX; prevPosY = posY; prevPosZ = posZ;
        if (worldObj.isRemote) {
            state = dataWatcher.getWatchableObjectInt(20);
            direction = dataWatcher.getWatchableObjectInt(21);
            distance = dataWatcher.getWatchableObjectInt(22);
            durationTicks = dataWatcher.getWatchableObjectInt(23);
            movementStartTick = dataWatcher.getWatchableObjectInt(24);
            virtualized = dataWatcher.getWatchableObjectByte(25) != 0;
            tickLerp();
            return;
        }
        if (!isMoving()) {
            if (!worldObj.isRemote && returnMode != 2 && ++waitTicks >= (returnMode == 0 ? 1 : delayTicks))
                start(state == STOPPED_A, null);
            return;
        }
        motionTick = Math.max(0, worldTick() - movementStartTick);
        double t = Math.min(1D, motionTick / (double)Math.max(1, durationTicks));
        double eased = t * t * (3D - 2D * t);
        double offset = state == MOVING_TO_B ? distance * eased : distance * (1D - eased);
        PlatformDirection d = getDirection();
        setPosition(homeX + d.x * offset, homeY + d.y * offset, homeZ + d.z * offset);
        if (t >= 1D && !worldObj.isRemote) {
            if(pendingConfiguration){
                applyPendingConfiguration();
                if(returnMode==2)materialize();
                return;
            }
            if(returnMode==0){
                state=state==MOVING_TO_B?MOVING_TO_A:MOVING_TO_B;
                dataWatcher.updateObject(20,Integer.valueOf(state));
                movementStartTick=worldTick();dataWatcher.updateObject(24,Integer.valueOf(movementStartTick));
                motionTick=0;waitTicks=0;return;
            }
            state = state == MOVING_TO_B ? STOPPED_B : STOPPED_A;
            dataWatcher.updateObject(20, Integer.valueOf(state));
            motionTick = 0;
            waitTicks = 0;
            if(returnMode==2)materialize();
            applyPendingConfiguration();
        }
    }

    public boolean dismantle(EntityPlayer feedback){
        if(isMoving()&&!stopAndReturn(feedback))return false;
        for(Object object:new java.util.ArrayList(worldObj.loadedEntityList))if(object instanceof EntityMovingPlatform){
            EntityMovingPlatform other=(EntityMovingPlatform)object;if(platformId.equals(other.platformId))other.setDead();
        }
        return true;
    }

    public boolean contains(int x, int y, int z) {
        if (isMoving()) return false;
        int ox=floor(posX), oy=floor(posY), oz=floor(posZ);
        return x>=ox && x<ox+sizeX && y>=oy && y<oy+sizeY && z>=oz && z<oz+sizeZ;
    }
    public boolean isMoving() { return state == MOVING_TO_A || state == MOVING_TO_B; }
    public boolean isCollisionActive(){return virtualized||isMoving()||(worldObj!=null&&worldObj.isRemote&&lerpSteps>0);}
    public PlatformDirection getDirection() { return PlatformDirection.byOrdinal(direction); }
    public List<PlatformBlock> getBlocks() { return blocks; }
    public List<PlatformBlock> getOuterBlocks(){return outerBlocks;}
    public List<PlatformBlock> getTopBlocks(){return topBlocks;}
    public List<PlatformBlock> getBottomBlocks(){return bottomBlocks;}
    public int getSizeX(){return sizeX;} public int getSizeY(){return sizeY;} public int getSizeZ(){return sizeZ;}
    public int getDirectionIndex(){return pendingConfiguration?pendingDirection:direction;} public int getDistance(){return pendingConfiguration?pendingDistance:distance;}
    public int getDurationTicks(){return pendingConfiguration?pendingDurationTicks:durationTicks;} public int getState(){return state;}
    public int getReturnMode(){return pendingConfiguration?pendingReturnMode:returnMode;} public int getDelayTicks(){return pendingConfiguration?pendingDelayTicks:delayTicks;}
    public UUID getPlatformId(){return platformId;}
    public boolean isConfigured(){return configured;}
    public void confirmConfiguration(){configured=true;}
    public void configure(int direction, int distance, int seconds, int returnMode, int delaySeconds) {
        int newDirection=PlatformDirection.byOrdinal(direction).ordinal();
        int newDistance=Math.max(1,Math.min(256,distance));
        int newDuration=Math.max(1,Math.min(72000,seconds*20));
        int newReturnMode=Math.max(0,Math.min(2,returnMode));
        int newDelay=Math.max(0,Math.min(72000,delaySeconds*20));
        if(isMoving()){
            pendingConfiguration=true;pendingDirection=newDirection;pendingDistance=newDistance;
            pendingDurationTicks=newDuration;pendingReturnMode=newReturnMode;pendingDelayTicks=newDelay;return;
        }
        this.direction=newDirection;this.distance=newDistance;this.durationTicks=newDuration;
        this.returnMode=newReturnMode;this.delayTicks=newDelay;
        dataWatcher.updateObject(21, Integer.valueOf(this.direction));
        dataWatcher.updateObject(22, Integer.valueOf(this.distance));
        dataWatcher.updateObject(23, Integer.valueOf(this.durationTicks));
    }

    private void applyPendingConfiguration(){
        if(!pendingConfiguration)return;
        homeX=posX;homeY=posY;homeZ=posZ;state=STOPPED_A;motionTick=0;waitTicks=0;
        direction=pendingDirection;distance=pendingDistance;durationTicks=pendingDurationTicks;
        returnMode=pendingReturnMode;delayTicks=pendingDelayTicks;pendingConfiguration=false;
        dataWatcher.updateObject(20,Integer.valueOf(state));dataWatcher.updateObject(21,Integer.valueOf(direction));
        dataWatcher.updateObject(22,Integer.valueOf(distance));dataWatcher.updateObject(23,Integer.valueOf(durationTicks));
    }

    public NBTTagCompound writePlatformTag() {
        NBTTagCompound tag = new NBTTagCompound(); writeEntityToNBT(tag); return tag;
    }
    public void readPlatformTag(NBTTagCompound tag) { readEntityFromNBT(tag); }

    @Override protected void readEntityFromNBT(NBTTagCompound tag) {
        platformId = UUID.fromString(tag.getString("PlatformId"));
        if (tag.hasKey("OwnerId")) ownerId = UUID.fromString(tag.getString("OwnerId"));
        homeX=tag.getDouble("HomeX"); homeY=tag.getDouble("HomeY"); homeZ=tag.getDouble("HomeZ");
        sizeX=tag.getInteger("SizeX"); sizeY=tag.getInteger("SizeY"); sizeZ=tag.getInteger("SizeZ");
        direction=tag.getInteger("Direction"); distance=tag.getInteger("Distance");
        durationTicks=tag.getInteger("Duration"); state=tag.getInteger("State"); motionTick=tag.getInteger("MotionTick");movementStartTick=tag.getInteger("MovementStart");
        returnMode=tag.hasKey("ReturnMode")?tag.getInteger("ReturnMode"):2;delayTicks=tag.hasKey("Delay")?tag.getInteger("Delay"):20;
        configured=tag.getBoolean("Configured");
        virtualized=tag.getBoolean("Virtualized");
        pendingConfiguration=tag.getBoolean("PendingConfiguration");
        pendingDirection=tag.getInteger("PendingDirection");pendingDistance=tag.getInteger("PendingDistance");
        pendingDurationTicks=tag.getInteger("PendingDuration");pendingReturnMode=tag.getInteger("PendingReturnMode");
        pendingDelayTicks=tag.getInteger("PendingDelay");
        blocks.clear(); NBTTagList list=tag.getTagList("Blocks",10);
        for(int i=0;i<list.tagCount();i++){PlatformBlock b=PlatformBlock.read(list.getCompoundTagAt(i));if(b!=null)blocks.add(b);}
        rebuildCollisionCache();
        // Never mutate blocks while AnvilChunkLoader is still constructing this entity.
        // A platform saved in transit already owns its block snapshot and resumes normally
        // from the persisted world tick on the first regular server update.
        dataWatcher.updateObject(20, Integer.valueOf(state));dataWatcher.updateObject(21, Integer.valueOf(direction));
        dataWatcher.updateObject(22, Integer.valueOf(distance));dataWatcher.updateObject(23, Integer.valueOf(durationTicks));
        dataWatcher.updateObject(24, Integer.valueOf(movementStartTick));
        dataWatcher.updateObject(25,Byte.valueOf((byte)(virtualized?1:0)));
    }
    @Override protected void writeEntityToNBT(NBTTagCompound tag) {
        tag.setString("PlatformId",platformId.toString()); if(ownerId!=null)tag.setString("OwnerId",ownerId.toString());
        tag.setDouble("HomeX",homeX);tag.setDouble("HomeY",homeY);tag.setDouble("HomeZ",homeZ);
        tag.setInteger("SizeX",sizeX);tag.setInteger("SizeY",sizeY);tag.setInteger("SizeZ",sizeZ);
        tag.setInteger("Direction",direction);tag.setInteger("Distance",distance);tag.setInteger("Duration",durationTicks);
        tag.setInteger("State",state);tag.setInteger("MotionTick",motionTick);
        tag.setInteger("MovementStart",movementStartTick);
        tag.setInteger("ReturnMode",returnMode);tag.setInteger("Delay",delayTicks);
        tag.setBoolean("Configured",configured);
        tag.setBoolean("Virtualized",virtualized);
        tag.setBoolean("PendingConfiguration",pendingConfiguration);tag.setInteger("PendingDirection",pendingDirection);
        tag.setInteger("PendingDistance",pendingDistance);tag.setInteger("PendingDuration",pendingDurationTicks);
        tag.setInteger("PendingReturnMode",pendingReturnMode);tag.setInteger("PendingDelay",pendingDelayTicks);
        NBTTagList list=new NBTTagList();for(PlatformBlock b:blocks)list.appendTag(b.write());tag.setTag("Blocks",list);
    }
    private void setEndpointPosition(){PlatformDirection d=getDirection();int o=state==STOPPED_B?distance:0;setPosition(homeX+d.x*o,homeY+d.y*o,homeZ+d.z*o);}
    private static int floor(double v){return (int)Math.floor(v+1.0E-5D);}
    private int worldTick(){return (int)(worldObj.getTotalWorldTime()&0x7fffffffL);}
    private void tickLerp(){
        if(lerpSteps<=0)return;
        posX+=(lerpX-posX)/lerpSteps;posY+=(lerpY-posY)/lerpSteps;posZ+=(lerpZ-posZ)/lerpSteps;
        --lerpSteps;setPosition(posX,posY,posZ);
    }
    private void setVirtualized(boolean value){virtualized=value;dataWatcher.updateObject(25,Byte.valueOf((byte)(value?1:0)));}
    private void rebuildCollisionCache(){
        outerBlocks.clear();topBlocks.clear();bottomBlocks.clear();HashSet<Long> occupied=new HashSet<Long>();
        for(PlatformBlock b:blocks)occupied.add(blockKey(b.x,b.y,b.z));
        for(PlatformBlock b:blocks){
            boolean top=!occupied.contains(blockKey(b.x,b.y+1,b.z)),bottom=!occupied.contains(blockKey(b.x,b.y-1,b.z));
            if(top)topBlocks.add(b);if(bottom)bottomBlocks.add(b);
            if(top||bottom||!occupied.contains(blockKey(b.x+1,b.y,b.z))||!occupied.contains(blockKey(b.x-1,b.y,b.z))||!occupied.contains(blockKey(b.x,b.y,b.z+1))||!occupied.contains(blockKey(b.x,b.y,b.z-1)))outerBlocks.add(b);
        }
    }
    private static long blockKey(int x,int y,int z){return ((long)(x&0x1fffff)<<42)|((long)(y&0x1fffff)<<21)|(long)(z&0x1fffff);}
    @Override public void setPositionAndRotation2(double x,double y,double z,float yaw,float pitch,int steps){
        lerpX=x;lerpY=y;lerpZ=z;lerpSteps=5;
    }
    @Override public void writeSpawnData(ByteBuf buf) { try { byte[] bytes=net.minecraft.nbt.CompressedStreamTools.compress(writePlatformTag());buf.writeInt(bytes.length);buf.writeBytes(bytes); } catch(java.io.IOException e) { buf.writeInt(0); } }
    @Override public void readSpawnData(ByteBuf buf) { try{int length=buf.readInt();if(length<=0)return;byte[] bytes=new byte[length];buf.readBytes(bytes);readPlatformTag(net.minecraft.nbt.CompressedStreamTools.func_152457_a(bytes,new net.minecraft.nbt.NBTSizeTracker(2097152L)));}catch(Exception ignored){} }
    @Override public AxisAlignedBB getCollisionBox(Entity other){return null;}
    @Override public AxisAlignedBB getBoundingBox(){return null;}
    @Override public boolean canBeCollidedWith(){return false;}
    @Override public void setDead(){MovingPlatformTickHandler.untrack(this);super.setDead();}
}
