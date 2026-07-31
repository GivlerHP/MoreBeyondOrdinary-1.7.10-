package ru.givler.mbo.entity.boat;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketBoatMove;
import ru.givler.mbo.registry.BoatRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.9-style boat physics adapted from Et Futurum Requiem for MBO.
 */
public class EntityMBOBoat extends Entity {
    public static final float VISUAL_Y_OFFSET = 0.1875F;
    private static final int HIT = 17, FORWARD = 18, DAMAGE = 19, TYPE = 21;
    private static final int[] PADDLES = {24, 25};
    private final float[] paddlePositions = new float[2];
    private float momentum, deltaRotation, outOfControlTicks;
    private double waterLevel, lastYd;
    private Status status, previousStatus;
    private boolean leftInput, rightInput, forwardInput, backInput;
    private EntityMBOBoatSeat secondSeat;
    private double lerpX, lerpY, lerpZ, lerpYaw, lerpPitch;
    private int lerpSteps;

    public EntityMBOBoat(World world) {
        super(world);
        preventEntitySpawning = true;
        setSize(1.375F, 0.5625F);
    }

    public EntityMBOBoat(World world, double x, double y, double z, int type) {
        this(world);
        setPosition(x, y, z);
        prevPosX = x; prevPosY = y; prevPosZ = z;
        setBoatType(type);
    }

    protected void entityInit() {
        dataWatcher.addObject(HIT, Integer.valueOf(0));
        dataWatcher.addObject(FORWARD, Integer.valueOf(1));
        dataWatcher.addObject(DAMAGE, Float.valueOf(0));
        dataWatcher.addObject(TYPE, Integer.valueOf(0));
        dataWatcher.addObject(PADDLES[0], Byte.valueOf((byte)0));
        dataWatcher.addObject(PADDLES[1], Byte.valueOf((byte)0));
    }

    public int getBoatType() { return dataWatcher.getWatchableObjectInt(TYPE); }
    public void setBoatType(int type) {
        dataWatcher.updateObject(TYPE, Integer.valueOf(Math.max(0, Math.min(20, type))));
    }
    public int getTimeSinceHit() { return dataWatcher.getWatchableObjectInt(HIT); }
    public void setTimeSinceHit(int value) { dataWatcher.updateObject(HIT, Integer.valueOf(value)); }
    public int getForwardDirection() { return dataWatcher.getWatchableObjectInt(FORWARD); }
    public void setForwardDirection(int value) { dataWatcher.updateObject(FORWARD, Integer.valueOf(value)); }
    public float getDamageTaken() { return dataWatcher.getWatchableObjectFloat(DAMAGE); }
    public void setDamageTaken(float value) { dataWatcher.updateObject(DAMAGE, Float.valueOf(value)); }
    public boolean getPaddleState(int side) {
        return dataWatcher.getWatchableObjectByte(PADDLES[side]) == 1 && riddenByEntity != null;
    }
    public void setPaddleState(boolean left, boolean right) {
        dataWatcher.updateObject(PADDLES[0], Byte.valueOf((byte)(left ? 1 : 0)));
        dataWatcher.updateObject(PADDLES[1], Byte.valueOf((byte)(right ? 1 : 0)));
    }
    public float getRowingTime(int side, float partial) {
        return getPaddleState(side) ? (float)MathHelper.denormalizeClamp(
                paddlePositions[side] - 0.3926991D, paddlePositions[side], partial) : 0;
    }

    protected boolean hasSecondSeat() { return true; }
    public EntityMBOBoatSeat getSecondSeat() { return secondSeat; }
    public void setSecondSeat(EntityMBOBoatSeat seat) { secondSeat = seat; }

    public List<EntityLivingBase> getPassengers() {
        List<EntityLivingBase> passengers = new ArrayList<EntityLivingBase>();
        if (riddenByEntity instanceof EntityLivingBase) passengers.add((EntityLivingBase)riddenByEntity);
        if (secondSeat != null && secondSeat.riddenByEntity instanceof EntityLivingBase)
            passengers.add((EntityLivingBase)secondSeat.riddenByEntity);
        return passengers;
    }

    @Override
    public boolean interactFirst(EntityPlayer player) {
        if (player.isSneaking() || outOfControlTicks >= 60) return true;
        if (riddenByEntity == null) {
            if (!worldObj.isRemote) player.mountEntity(this);
        } else if (hasSecondSeat() && secondSeat != null && secondSeat.riddenByEntity == null) {
            if (!worldObj.isRemote) player.mountEntity(secondSeat);
        }
        return true;
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        if (!worldObj.isRemote && hasSecondSeat() && (secondSeat == null || secondSeat.isDead)) {
            secondSeat = new EntityMBOBoatSeat(worldObj, this);
            secondSeat.copyLocationAndAnglesFrom(this);
            worldObj.spawnEntityInWorld(secondSeat);
        }
    }

    @Override
    public void onUpdate() {
        previousStatus = status;
        status = getBoatStatus();
        if (status == Status.UNDER_WATER || status == Status.UNDER_FLOWING_WATER) outOfControlTicks++;
        else outOfControlTicks = 0;
        if (!worldObj.isRemote && outOfControlTicks >= 60 && riddenByEntity != null) riddenByEntity.mountEntity(null);
        if (getTimeSinceHit() > 0) setTimeSinceHit(getTimeSinceHit() - 1);
        if (getDamageTaken() > 0) setDamageTaken(getDamageTaken() - 1);
        prevPosX = posX; prevPosY = posY; prevPosZ = posZ;
        super.onUpdate();
        tickLerp();

        boolean localDriver = !worldObj.isRemote || riddenByEntity instanceof EntityClientPlayerMP;
        if (localDriver) {
            updateMotion();
            if (riddenByEntity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer)riddenByEntity;
                updateInputs(player.moveStrafing > 0, player.moveStrafing < 0,
                        player.moveForward > 0, player.moveForward < 0);
            } else updateInputs(false, false, false, false);
            controlBoat();
            moveEntity(motionX, motionY, motionZ);
        } else {
            motionX = motionY = motionZ = 0;
            deltaRotation = 0;
        }

        for (int side = 0; side < 2; side++)
            paddlePositions[side] = getPaddleState(side) ? paddlePositions[side] + 0.3926991F : 0;

        spawnWakeParticles();
        func_145775_I();
        if (worldObj.isRemote && riddenByEntity instanceof EntityClientPlayerMP)
            PacketManager.INSTANCE.sendToServer(new PacketBoatMove(this));
    }

    private void spawnWakeParticles() {
        if (!worldObj.isRemote || status != Status.IN_WATER) return;
        double horizontalSpeed = Math.sqrt(motionX * motionX + motionZ * motionZ);
        if (horizontalSpeed < 0.04D) return;

        int count = Math.min(16, 1 + (int)(horizontalSpeed * 45.0D));
        double sin = MathHelper.sin(rotationYaw * (float)Math.PI / 180.0F);
        double cos = MathHelper.cos(rotationYaw * (float)Math.PI / 180.0F);

        for (int i = 0; i < count; ++i) {
            double longitudinal = (rand.nextDouble() - 0.5D) * 1.6D;
            double side = rand.nextBoolean() ? 0.75D : -0.75D;
            double particleX = posX - cos * side + sin * longitudinal;
            double particleZ = posZ - sin * side - cos * longitudinal;
            worldObj.spawnParticle("splash", particleX,
                    boundingBox.minY + 0.15D, particleZ,
                    motionX * 0.25D, 0.05D, motionZ * 0.25D);
        }
    }

    private void tickLerp() {
        if (worldObj.isRemote && lerpSteps > 0 && !(riddenByEntity instanceof EntityClientPlayerMP)) {
            posX += (lerpX - posX) / lerpSteps;
            posY += (lerpY - posY) / lerpSteps;
            posZ += (lerpZ - posZ) / lerpSteps;
            rotationYaw += MathHelper.wrapAngleTo180_double(lerpYaw - rotationYaw) / lerpSteps;
            rotationPitch += (lerpPitch - rotationPitch) / lerpSteps;
            --lerpSteps;
            setPosition(posX, posY, posZ);
        }
    }

    @Override
    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int steps) {
        lerpX = x; lerpY = y; lerpZ = z; lerpYaw = yaw; lerpPitch = pitch; lerpSteps = 5;
    }

    private void updateInputs(boolean left, boolean right, boolean forward, boolean back) {
        leftInput = left; rightInput = right; forwardInput = forward; backInput = back;
    }

    private void controlBoat() {
        if (riddenByEntity == null) { setPaddleState(false, false); return; }
        float acceleration = 0;
        if (leftInput) deltaRotation -= 1;
        if (rightInput) deltaRotation += 1;
        if (rightInput != leftInput && !forwardInput && !backInput) acceleration += 0.005F;
        rotationYaw += deltaRotation;
        if (forwardInput) acceleration += 0.04F;
        if (backInput) acceleration -= 0.005F;
        motionX += MathHelper.sin(-rotationYaw * 0.017453292F) * acceleration;
        motionZ += MathHelper.cos(rotationYaw * 0.017453292F) * acceleration;
        setPaddleState(rightInput || forwardInput, leftInput || forwardInput);
    }

    private void updateMotion() {
        double gravity = -0.04D, buoyancy = 0;
        momentum = 0.05F;
        if (previousStatus == Status.IN_AIR && status != Status.IN_AIR && status != Status.ON_LAND) {
            setPosition(posX, getWaterLevelAbove() - height + 0.101D, posZ);
            motionY = 0; lastYd = 0; status = Status.IN_WATER;
        } else {
            if (status == Status.IN_WATER) {
                buoyancy = (waterLevel - boundingBox.minY) / height;
                momentum = 0.9F;
            } else if (status == Status.UNDER_FLOWING_WATER) {
                gravity = -0.0007D; momentum = 0.9F;
            } else if (status == Status.UNDER_WATER) {
                buoyancy = 0.01D; momentum = 0.45F;
            } else if (status == Status.IN_AIR) momentum = 0.9F;
            else if (status == Status.ON_LAND) momentum = 0.45F;
            motionX *= momentum; motionZ *= momentum; deltaRotation *= momentum; motionY += gravity;
            if (buoyancy > 0) { motionY += buoyancy * 0.06153846D; motionY *= 0.75D; }
        }
    }

    private Status getBoatStatus() {
        Status underwater = getUnderwaterStatus();
        if (underwater != null) { waterLevel = boundingBox.maxY; return underwater; }
        if (checkInWater()) return Status.IN_WATER;
        Block below = worldObj.getBlock(MathHelper.floor_double(posX),
                MathHelper.floor_double(boundingBox.minY - 0.01D), MathHelper.floor_double(posZ));
        return below.getMaterial().isSolid() ? Status.ON_LAND : Status.IN_AIR;
    }

    private boolean checkInWater() {
        int minX = MathHelper.floor_double(boundingBox.minX), maxX = MathHelper.ceiling_double_int(boundingBox.maxX);
        int y = MathHelper.floor_double(boundingBox.minY);
        int minZ = MathHelper.floor_double(boundingBox.minZ), maxZ = MathHelper.ceiling_double_int(boundingBox.maxZ);
        waterLevel = Double.MIN_VALUE;
        for (int x=minX;x<maxX;x++) for (int z=minZ;z<maxZ;z++) if (worldObj.getBlock(x,y,z).getMaterial()==Material.water) {
            float level = liquidHeight(worldObj,x,y,z); waterLevel=Math.max(waterLevel,level);
            if (boundingBox.minY < level) return true;
        }
        return false;
    }

    private Status getUnderwaterStatus() {
        double top = boundingBox.maxY + 0.001D;
        int y = MathHelper.floor_double(boundingBox.maxY);
        for (int x=MathHelper.floor_double(boundingBox.minX);x<MathHelper.ceiling_double_int(boundingBox.maxX);x++)
            for (int z=MathHelper.floor_double(boundingBox.minZ);z<MathHelper.ceiling_double_int(boundingBox.maxZ);z++)
                if (worldObj.getBlock(x,y,z).getMaterial()==Material.water && top < liquidHeight(worldObj,x,y,z))
                    return worldObj.getBlockMetadata(x,y,z)!=0 ? Status.UNDER_FLOWING_WATER : Status.UNDER_WATER;
        return null;
    }

    private float getWaterLevelAbove() {
        int y = MathHelper.floor_double(boundingBox.maxY);
        for (int test=y;test<y+5;test++)
            if (worldObj.getBlock(MathHelper.floor_double(posX),test,MathHelper.floor_double(posZ)).getMaterial()!=Material.water)
                return test;
        return y + 5;
    }

    private static float liquidHeight(World world,int x,int y,int z) {
        int meta=world.getBlockMetadata(x,y,z);
        return y + ((meta%8)==0 && world.getBlock(x,y+1,z).getMaterial()==Material.water
                ? 1F : 1F-BlockLiquid.getLiquidHeightPercent(meta));
    }

    @Override
    public void updateRiderPosition() {
        if (riddenByEntity != null) updatePassenger(riddenByEntity, 0.2F);
    }
    public void updateSecondPassenger(Entity passenger) { updatePassenger(passenger, -0.6F); }
    private void updatePassenger(Entity passenger, float offset) {
        Vec3 vector=Vec3.createVectorHelper(offset,0,0);
        vector.rotateAroundY(-rotationYaw*0.017453292F-(float)Math.PI/2);
        passenger.setPosition(posX+vector.xCoord,
                posY+getMountedYOffset()+passenger.getYOffset()+VISUAL_Y_OFFSET,
                posZ+vector.zCoord);
        passenger.rotationYaw += deltaRotation;
    }
    public double getMountedYOffset() { return 0; }

    /**
     * Точка сущности находится ниже ватерлинии, поэтому стандартный Entity
     * берёт подводное освещение. Для модели лодки свет нужно измерять над
     * поверхностью корпуса.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender(float partialTicks) {
        int x = MathHelper.floor_double(posX);
        int y = MathHelper.floor_double(boundingBox.maxY + 0.75D);
        int z = MathHelper.floor_double(posZ);
        return worldObj.getLightBrightnessForSkyBlocks(x, y, z, 0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getBrightness(float partialTicks) {
        int x = MathHelper.floor_double(posX);
        int y = MathHelper.floor_double(boundingBox.maxY + 0.75D);
        int z = MathHelper.floor_double(posZ);
        return worldObj.getLightBrightness(x, y, z);
    }

    protected boolean canTriggerWalking() { return false; }
    public AxisAlignedBB getCollisionBox(Entity entity) {
        return isBoatPassenger(entity) ? null : entity.boundingBox;
    }
    public AxisAlignedBB getBoundingBox() { return boundingBox; }
    public boolean canBePushed() { return true; }
    public boolean canBeCollidedWith() { return !isDead; }

    private boolean isBoatPassenger(Entity entity) {
        return entity == riddenByEntity
                || entity == secondSeat
                || (secondSeat != null && entity == secondSeat.riddenByEntity)
                || entity instanceof EntityMBOBoatSeat
                || entity.ridingEntity == this
                || (secondSeat != null && entity.ridingEntity == secondSeat);
    }

    @Override
    public void applyEntityCollision(Entity entity) {
        if (!isBoatPassenger(entity)) {
            super.applyEntityCollision(entity);
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (isEntityInvulnerable() || worldObj.isRemote || isDead) return false;
        setForwardDirection(-getForwardDirection()); setTimeSinceHit(10);
        setDamageTaken(getDamageTaken()+amount*10); setBeenAttacked();
        boolean creative=source.getEntity() instanceof EntityPlayer && ((EntityPlayer)source.getEntity()).capabilities.isCreativeMode;
        if (creative || getDamageTaken()>40) {
            if (!creative && worldObj.getGameRules().getGameRuleBooleanValue("doMobLoot"))
                entityDropItem(getBoatDrop(),0);
            setDead();
        }
        return true;
    }

    protected ItemStack getBoatDrop() {
        boolean bop=getBoatType()>=6;
        return new ItemStack(bop?BoatRegistry.bopBoats:BoatRegistry.vanillaBoats,1,bop?getBoatType()-6:getBoatType());
    }

    public void performHurtAnimation() {
        setForwardDirection(-getForwardDirection()); setTimeSinceHit(10); setDamageTaken(getDamageTaken()*11);
    }

    @Override
    public void setDead() {
        if (secondSeat!=null) secondSeat.setDead();
        if (riddenByEntity!=null) riddenByEntity.mountEntity(null);
        super.setDead();
    }

    protected void writeEntityToNBT(NBTTagCompound tag) { tag.setByte("MBOBoatType",(byte)getBoatType()); }
    protected void readEntityFromNBT(NBTTagCompound tag) { setBoatType(tag.getByte("MBOBoatType")&255); }
    protected void updateFallState(double y, boolean onGround) { lastYd=motionY; }

    public enum Status { IN_WATER, UNDER_WATER, UNDER_FLOWING_WATER, ON_LAND, IN_AIR }
}
