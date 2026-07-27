package ru.givler.mbo.entity.boat;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityMBOBoatSeat extends Entity {
    private EntityMBOBoat boat;

    public EntityMBOBoatSeat(World world) {
        super(world);
        setSize(0, 0);
        noClip = true;
        setInvisible(true);
    }

    public EntityMBOBoatSeat(World world, EntityMBOBoat boat) {
        this(world);
        setBoat(boat);
    }

    protected void entityInit() { dataWatcher.addObject(17, Integer.valueOf(0)); }

    public void setBoat(EntityMBOBoat value) {
        boat = value;
        if (value != null) {
            dataWatcher.updateObject(17, Integer.valueOf(value.getEntityId()));
            value.setSecondSeat(this);
        }
    }

    @Override
    public void onUpdate() {
        if (boat == null) {
            Entity entity = worldObj.getEntityByID(dataWatcher.getWatchableObjectInt(17));
            if (entity instanceof EntityMBOBoat) setBoat((EntityMBOBoat)entity);
        }
        if (boat == null || boat.isDead || boat instanceof EntityMBOChestBoat) {
            setDead();
            return;
        }
        copyLocationAndAnglesFrom(boat);
        prevPosX = boat.prevPosX;
        prevPosY = boat.prevPosY;
        prevPosZ = boat.prevPosZ;
        prevRotationYaw = boat.prevRotationYaw;
        prevRotationPitch = boat.prevRotationPitch;
        rotationYaw = boat.rotationYaw;
    }

    /**
     * Не используем стандартную позицию пассажира Entity: она ставит его в
     * центр невидимого сиденья и конфликтует со смещением второго места лодки.
     */
    @Override
    public void updateRiderPosition() {
        if (boat != null && riddenByEntity != null) {
            boat.updateSecondPassenger(riddenByEntity);
        }
    }

    /**
     * Сиденье всегда следует за лодкой и не должно отдельно интерполироваться
     * пакетами EntityTracker.
     */
    @Override
    public void setPositionAndRotation2(double x, double y, double z,
                                        float yaw, float pitch, int steps) {
        if (boat != null) {
            copyLocationAndAnglesFrom(boat);
            prevPosX = boat.prevPosX;
            prevPosY = boat.prevPosY;
            prevPosZ = boat.prevPosZ;
            rotationYaw = boat.rotationYaw;
            rotationPitch = boat.rotationPitch;
            prevRotationYaw = boat.prevRotationYaw;
            prevRotationPitch = boat.prevRotationPitch;
        }
    }

    public boolean canBeCollidedWith() { return false; }
    protected void readEntityFromNBT(NBTTagCompound tag) { }
    protected void writeEntityToNBT(NBTTagCompound tag) { }
}
