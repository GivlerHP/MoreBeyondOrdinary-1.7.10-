package ru.givler.mbo.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import ru.givler.mbo.handler.HangingEvents;
import ru.givler.mbo.handler.HangingRules;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;

/** Invisible, stationary riding link. It is intentionally never saved. */
public final class EntityHangingAnchor extends Entity implements IEntityAdditionalSpawnData {
    private int sourceX, sourceY, sourceZ, mode;
    private int attempts, lastAttempt = -20;
    private int lastDamageTick = -60;

    public EntityHangingAnchor(World world) {
        super(world);
        noClip = true;
        setSize(.1F, .1F);
        setInvisible(true);
    }

    public EntityHangingAnchor(World world, int x, int y, int z, int mode,
            double px, double py, double pz) {
        this(world);
        sourceX = x; sourceY = y; sourceZ = z; this.mode = mode;
        dataWatcher.updateObject(20, (byte) mode);
        setPosition(px, py, pz);
    }

    @Override protected void entityInit() {
        dataWatcher.addObject(20, (byte) 0);
    }
    public boolean isSuffocating() { return dataWatcher.getWatchableObjectByte(20) == 2; }
    public void clampView(Entity rider) {
        if (rider.ridingEntity != this) return;
        float yaw = isSuffocating() ? rotationYaw : rotationYaw + net.minecraft.util.MathHelper.clamp_float(
            net.minecraft.util.MathHelper.wrapAngleTo180_float(rider.rotationYaw - rotationYaw), -90F, 90F);
        float pitch = isSuffocating() ? rotationPitch
            : net.minecraft.util.MathHelper.clamp_float(rider.rotationPitch, -75F, 75F);
        rider.prevRotationYaw += net.minecraft.util.MathHelper.wrapAngleTo180_float(yaw - rider.rotationYaw);
        rider.prevRotationPitch += pitch - rider.rotationPitch;
        rider.rotationYaw = yaw;
        rider.rotationPitch = pitch;
        if (isSuffocating()) {
            rider.prevRotationYaw = yaw;
            rider.prevRotationPitch = pitch;
        }
        if (rider instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) rider;
            living.renderYawOffset = living.prevRenderYawOffset = rotationYaw;
            living.rotationYawHead = yaw;
            if (isSuffocating()) living.prevRotationYawHead = yaw;
        }
    }
    public boolean belongsTo(int x, int y, int z) {
        return sourceX == x && sourceY == y && sourceZ == z;
    }

    @Override public void writeSpawnData(ByteBuf data) {
        data.writeInt(sourceX);
        data.writeInt(sourceY);
        data.writeInt(sourceZ);
        data.writeByte(mode);
    }

    @Override public void readSpawnData(ByteBuf data) {
        sourceX = data.readInt();
        sourceY = data.readInt();
        sourceZ = data.readInt();
        mode = data.readByte();
        dataWatcher.updateObject(20, (byte) mode);
    }

    @Override public void onUpdate() {
        super.onUpdate();
        motionX = motionY = motionZ = 0;
        if (worldObj.isRemote) return;
        if (riddenByEntity == null || riddenByEntity.isDead
                || riddenByEntity.worldObj != worldObj
                || HangingEvents.mode(worldObj, sourceX, sourceY, sourceZ) != mode) {
            release();
            return;
        }
        riddenByEntity.fallDistance = 0;
        if (mode == 1) damageFromHook();
    }

    public void damageFromHook() {
        if (riddenByEntity != null && ticksExisted - lastDamageTick >= 60) {
            lastDamageTick = ticksExisted;
            riddenByEntity.attackEntityFrom(DamageSource.generic, 1);
        }
    }

    public void attemptEscape(EntityPlayer player) {
        if (worldObj.isRemote || player != riddenByEntity || ticksExisted - lastAttempt < 20) return;
        lastAttempt = ticksExisted;
        attempts++;
        if (mode == 1) {
            // Struggling hurts even when it succeeds, independently of periodic hook damage.
            player.attackEntityFrom(DamageSource.generic, 2);
            lastDamageTick = ticksExisted;
            if (!player.isEntityAlive() || HangingRules.hookEscapes(rand.nextInt(100))) release();
        } else if (HangingRules.escapes(true, attempts, rand.nextBoolean())) release();
    }

    public void release() {
        Entity rider = riddenByEntity;
        if (rider != null) {
            if (rider instanceof EntityPlayer && isSuffocating()) ((EntityPlayer) rider).setAir(300);
            rider.mountEntity(null);
            rider.fallDistance = 0;
        }
        setDead();
    }

    @Override public void updateRiderPosition() {
        if (riddenByEntity != null) {
            clampView(riddenByEntity);
            riddenByEntity.setPosition(posX, HangingRules.passengerY(posY, riddenByEntity.yOffset), posZ);
            riddenByEntity.motionX = riddenByEntity.motionY = riddenByEntity.motionZ = 0;
            // A stationary mount must not interpolate between gravity/knockback and its attachment.
            riddenByEntity.prevPosX = riddenByEntity.lastTickPosX = riddenByEntity.posX;
            riddenByEntity.prevPosY = riddenByEntity.lastTickPosY = riddenByEntity.posY;
            riddenByEntity.prevPosZ = riddenByEntity.lastTickPosZ = riddenByEntity.posZ;
        }
    }
    @Override public void setPositionAndRotation2(double x, double y, double z,
            float yaw, float pitch, int ticks) {
        setPosition(x, y, z);
        setRotation(yaw, pitch);
    }
    @Override public boolean shouldDismountInWater(Entity rider) { return false; }
    @Override public boolean shouldRiderSit() { return false; }
    @Override public boolean canBeCollidedWith() { return false; }
    @Override public boolean writeToNBTOptional(NBTTagCompound tag) { return false; }
    @Override public boolean writeMountToNBT(NBTTagCompound tag) { return false; }
    @Override protected void readEntityFromNBT(NBTTagCompound tag) { setDead(); }
    @Override protected void writeEntityToNBT(NBTTagCompound tag) {}
}
