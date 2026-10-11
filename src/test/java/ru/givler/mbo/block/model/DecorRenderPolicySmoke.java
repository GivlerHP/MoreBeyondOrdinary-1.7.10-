package ru.givler.mbo.block.model;

import java.lang.reflect.Field;
import java.util.Map;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.tileentity.ModelTileBase;
import ru.givler.mbo.tileentity.TileEntityLootContainer;
import ru.givler.mbo.tileentity.TileEntityModelCollision;
import sun.misc.Unsafe;

/** Run with the actual Forge/Minecraft compile classpath, without launching a world. */
public final class DecorRenderPolicySmoke {
    private static void check(boolean value,String message) { if(!value)throw new AssertionError(message); }
    private static void field(Object target,String name,Object value)throws Exception {
        Field field=BlockModels.class.getDeclaredField(name);field.setAccessible(true);field.set(target,value);
    }
    public static void main(String[] args)throws Exception {
        Field access=Unsafe.class.getDeclaredField("theUnsafe");access.setAccessible(true);Unsafe unsafe=(Unsafe)access.get(null);
        BlockModels block=(BlockModels)unsafe.allocateInstance(BlockModels.class);
        field(block,"textureFrameCount",1);field(block,"textureFrameSpeed",100);
        field(block,"modelName","decor_test");field(block,"textureName","decor_texture");
        Field mapping=TileEntity.class.getDeclaredField("classToNameMap");mapping.setAccessible(true);
        ((Map)mapping.get(null)).put(ModelTileBase.class,"ModelTileBase");
        ModelTileBase old=new ModelTileBase("old_texture","old_model");old.blockType=block;
        old.xCoord=-17;old.yCoord=85;old.zCoord=412;old.blockMetadata=2;
        old.ropeFloor=old.ropeAnchor=old.ropeAnchorExtra=true;
        NBTTagCompound saved=new NBTTagCompound();old.writeToNBT(saved);
        ModelTileBase loaded=new ModelTileBase();loaded.readFromNBT(saved);loaded.blockType=block;loaded.blockMetadata=2;
        check(!loaded.canUpdate() && !loaded.usesDynamicRender(),"static decor must neither tick nor animate");
        Field factory=ModelTileBase.class.getDeclaredField("factory");factory.setAccessible(true);
        check(factory.get(loaded)==null,"static model must not allocate an AnimationFactory");
        check(loaded.xCoord==-17&&loaded.yCoord==85&&loaded.zCoord==412&&loaded.blockMetadata==2,"world position and facing changed");
        check(loaded.ropeFloor&&loaded.ropeAnchor&&loaded.ropeAnchorExtra,"individual rope state lost");
        block.withAnimation("idle",true);
        check(loaded.isAnimated()&&loaded.usesDynamicRender()&&!loaded.canUpdate(),"new animation must override old NBT without adding gameplay ticks");
        check("idle".equals(loaded.getAnimationName()),"old NBT suppresses registered animation");
        check(loaded.getFactory()!=null,"animated object requires a lazy factory");
        block.withAnimation(null,true);
        check(loaded.usesDynamicRender(),"a potentially animated block remains dynamic");
        BlockModels textured=(BlockModels)unsafe.allocateInstance(BlockModels.class);
        textured.withAnimatedTexture(6,80);loaded.blockType=textured;
        check(loaded.getFrameCount()==6&&loaded.getFrameSpeed()==80&&loaded.usesDynamicRender(),"new texture animation ignored by old tile");
        textured.withAnimatedTexture(1,100);check(loaded.usesDynamicRender(),"texture capability must remain dynamic");
        BlockModelFood food=(BlockModelFood)unsafe.allocateInstance(BlockModelFood.class);
        field(food,"textureFrameCount",1);loaded.blockType=food;
        check(loaded.canUpdate()&&!loaded.usesDynamicRender(),"food logic must tick independently of static rendering");
        loaded.startEating(2,0);loaded.updateEntity();check(loaded.isEating(),"food timer did not run");
        check(new TileEntityLootContainer().canUpdate(),"loot recovery logic disabled");
        check(!new TileEntityModelCollision().canUpdate(),"collision helpers must not tick continuously");
        block.withModelLogicTicks();loaded.blockType=block;check(loaded.canUpdate(),"optional gameplay ticks ignored");
        System.out.println("Old NBT, unchanged coordinates/facing, lazy factories, static-to-dynamic registration and independent gameplay ticks passed.");
    }
}
