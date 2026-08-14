package ru.givler.mbo.core;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Placement orientation shared by the vanilla trapdoor ASM patch. */
public final class TrapdoorPlacementHooks {
    private TrapdoorPlacementHooks() {}

    public static void placedBy(World world,int x,int y,int z,EntityLivingBase placer){
        int old=world.getBlockMetadata(x,y,z);
        int facing=MathHelper.floor_double(placer.rotationYaw*4.0F/360.0F+0.5D)&3;
        int direction;
        switch(facing){
            case 1: direction=3;break;
            case 2: direction=1;break;
            case 3: direction=2;break;
            default:direction=0;
        }
        world.setBlockMetadataWithNotify(x,y,z,(old&12)|direction,2);
    }
}
