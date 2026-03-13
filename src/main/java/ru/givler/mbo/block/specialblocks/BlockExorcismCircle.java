package ru.givler.mbo.block.specialblocks;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;
import ru.givler.mbo.tileentity.TileExorcismCircle;

public class BlockExorcismCircle extends BlockContainer {
    public BlockExorcismCircle(Material material, String name, String texture) {
        super(material);

        this.setBlockName(name);
        this.setLightLevel(0.0F);
        this.setLightOpacity(0);
        this.setHardness(1.0F);
        this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        this.setResistance(10.0F);
        this.setStepSound(soundTypeStone);
        this.setBlockTextureName(MoreBeyondOrdinary.MODID + ":" + texture);
        this.setBlockUnbreakable();

        GameRegistry.registerBlock(this, name);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileExorcismCircle();
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return -1; // не рендерить блок (рендер через TESR)
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return null;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.01F, 1.0F); // очень тонкий (как нажимная плита)
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (world.isRemote) return;

        if (entity instanceof EntityZombie) {
            EntityZombie zombie = (EntityZombie) entity;

            if (!zombie.isVillager()) return;

            TileEntity te = world.getTileEntity(x, y, z);
            if (!(te instanceof TileExorcismCircle)) return;
            TileExorcismCircle circle = (TileExorcismCircle) te;

            if (circle.hasZombie) return;

            /**
             * Проверка на попадание ровно в центр круга.
             * Центр блока — x+0.5, z+0.5
             */
            double dx = Math.abs(entity.posX - (x + 0.5));
            double dz = Math.abs(entity.posZ - (z + 0.5));

            if (dx < 0.25 && dz < 0.25) {

                zombie.motionX = 0;
                zombie.motionY = 0;
                zombie.motionZ = 0;
                zombie.velocityChanged = true;

                for (int i = 0; i < 40; i++) {
                    double px = x + 0.5 + (world.rand.nextDouble() - 0.5) * 1.2;
                    double py = y + 0.05;
                    double pz = z + 0.5 + (world.rand.nextDouble() - 0.5) * 1.2;
                    world.spawnParticle("spell", px, py, pz, 0, 0.01, 0);
                }

                zombie.addPotionEffect(new PotionEffect(Potion.weakness.id, 3000, 2, false));

                zombie.tasks.taskEntries.clear();
                zombie.targetTasks.taskEntries.clear();

                circle.trappedZombie = zombie;
                circle.hasZombie = true;

            }
        }
    }

}