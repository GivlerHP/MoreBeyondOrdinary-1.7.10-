package ru.givler.mbo.core;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.tileentity.TileEntity;
import ru.givler.mbo.entity.EntityHangingAnchor;
import ru.givler.mbo.tileentity.TileEntityModelCollision;

/** Restricts attachment ray exclusions to camera positioning, never block interaction. */
public final class HangingCameraContext {
    private static final ThreadLocal<Entity> VIEW = new ThreadLocal<Entity>();
    public static void begin(Entity viewer) { VIEW.set(viewer); }
    public static void end() { VIEW.remove(); }
    public static boolean ignore(World world, int x, int y, int z) {
        Entity viewer = VIEW.get();
        if (viewer == null || viewer.worldObj != world
                || !(viewer.ridingEntity instanceof EntityHangingAnchor)) return false;
        EntityHangingAnchor anchor = (EntityHangingAnchor) viewer.ridingEntity;
        if (anchor.belongsTo(x, y, z)) return true;
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileEntityModelCollision)) return false;
        TileEntityModelCollision part = (TileEntityModelCollision) tile;
        return anchor.belongsTo(part.getOwnerX(), part.getOwnerY(), part.getOwnerZ());
    }
}
