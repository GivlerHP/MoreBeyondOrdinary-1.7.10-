package ru.givler.mbo.block;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class BlockSlimeMBO extends Block {
    private static final SoundType SLIME_SOUND=new SoundType("slime",1.0F,1.0F) {
        @Override public String getBreakSound() { return "mob.slime.big"; }
        @Override public String getStepResourcePath() { return "mob.slime.big"; }
        @Override public String func_150496_b() { return "mob.slime.big"; }
    };
    private static final Map<Entity,Double> BOUNCES=new WeakHashMap<Entity,Double>();
    private long lastBounceTick;
    private int slimeRenderType;

    public BlockSlimeMBO() {
        this("SlimeBlock", "mbo:slime");
    }

    protected BlockSlimeMBO(String name, String texture) {
        super(Material.clay);
        setBlockName(name);
        setBlockTextureName(texture);
        setHardness(0);
        setResistance(0);
        setStepSound(SLIME_SOUND);
        setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        setLightOpacity(0);
    }

    public void setSlimeRenderType(int id) { slimeRenderType=id; }

    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world,int x,int y,int z) {
        return AxisAlignedBB.getBoundingBox(x,y,z,x+1,y+.875,z+1);
    }

    @Override public void onFallenUpon(World world,int x,int y,int z,Entity entity,float distance) {
        if(!entity.isSneaking()) {
            entity.fallDistance=0;
            if(entity.motionY<.1) {
                BOUNCES.put(entity,-entity.motionY);
                lastBounceTick=world.getTotalWorldTime();
                if(!world.isRemote)
                    world.playSoundEffect(entity.posX,entity.posY,entity.posZ,
                            "mob.slime.big",.8F,.9F+world.rand.nextFloat()*.2F);
            }
        }
        super.onFallenUpon(world,x,y,z,entity,distance);
    }

    @Override public void onEntityCollidedWithBlock(World world,int x,int y,int z,Entity entity) {
        if(lastBounceTick==world.getTotalWorldTime()) {
            Double bounce=BOUNCES.remove(entity);
            if(bounce!=null) {
                entity.motionY=bounce;
            }
        } else BOUNCES.clear();
        double drag=.4+Math.abs(entity.motionY)*.2;
        entity.motionX*=drag;
        entity.motionZ*=drag;
        super.onEntityCollidedWithBlock(world,x,y,z,entity);
    }

    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public int getRenderBlockPass() { return 1; }
    @Override public int getRenderType() { return slimeRenderType; }
}
