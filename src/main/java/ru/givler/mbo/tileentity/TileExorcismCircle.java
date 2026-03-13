package ru.givler.mbo.tileentity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import ru.givler.mbo.block.specialblocks.BlockExorcismCircle;

import java.util.List;

public class TileExorcismCircle extends TileEntity {
    public int centerX, centerY, centerZ;
    public boolean hasZombie = false;
    public EntityZombie trappedZombie;
    private EntityAIWatchClosest watchTask;

    public static void markCenter(World world, int x, int y, int z, int cx, int cy, int cz) {
        TileEntity te = world.getTileEntity(x, y, z);
        if(te instanceof TileExorcismCircle) {
            TileExorcismCircle c = (TileExorcismCircle) te;
            c.centerX = cx;
            c.centerY = cy;
            c.centerZ = cz;
        }
    }

    @Override
    public void updateEntity() {
        if (worldObj.isRemote) return;
        if (hasZombie || centerX == 0 && centerY == 0 && centerZ == 0) return;

        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
                centerX - 0.5, centerY, centerZ - 0.5,
                centerX + 0.5, centerY + 2, centerZ + 0.5
        );

        List<Entity> list = worldObj.getEntitiesWithinAABB(Entity.class, box);
        for (Entity e : list) {
            if (e instanceof EntityZombie) {
                EntityZombie z = (EntityZombie) e;
                if (z.isVillager()) {
                    trappedZombie = z;
                    hasZombie = true;
                    startTrap();
                    break;
                }
            }
        }
    }

    public void startTrap() {
        if (trappedZombie == null) return;

        trappedZombie.addPotionEffect(new PotionEffect(Potion.weakness.id, 99999, 1, false));

        watchTask = new EntityAIWatchClosest(trappedZombie, EntityPlayer.class, 6.0F);
        trappedZombie.tasks.addTask(0, watchTask);

        trappedZombie.motionX = 0;
        trappedZombie.motionY = 0;
        trappedZombie.motionZ = 0;
        trappedZombie.velocityChanged = true;
    }

    public void failRitual() {
        if (trappedZombie != null) {
            trappedZombie.removePotionEffect(Potion.moveSlowdown.id);

            trappedZombie.addPotionEffect(new PotionEffect(Potion.damageBoost.id, 600, 3));
            trappedZombie.addPotionEffect(new PotionEffect(Potion.resistance.id, 600, 2));
            trappedZombie.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, 600, 2));

            // кастомный усиленный NBT тэг
            trappedZombie.getEntityData().setBoolean("ExorcismFailedBuff", true);
        }

        destroyCircle();
        hasZombie = false;
        trappedZombie = null;
    }


    public void successRitual() {
        if (trappedZombie == null) return;
        if (!trappedZombie.isVillager()) return;

        // Создаём фейковый ItemStack золотого яблока в руке игрока
        ItemStack apple = new ItemStack(net.minecraft.init.Items.golden_apple);

        EntityPlayer fakePlayer = worldObj.getClosestPlayer(centerX, centerY, centerZ, 5.0F);
        if (fakePlayer != null) {
            fakePlayer.inventory.mainInventory[fakePlayer.inventory.currentItem] = apple;
            trappedZombie.interact(fakePlayer); // ← запускает конверсию ванильным способом
        }

        destroyCircle();
        hasZombie = false;
        trappedZombie = null;
    }

    public void customConvert() {
        if (trappedZombie == null) return;

        EntityVillager v = new EntityVillager(worldObj);
        v.setPosition(trappedZombie.posX, trappedZombie.posY, trappedZombie.posZ);
        worldObj.spawnEntityInWorld(v);

        trappedZombie.setDead();
        destroyCircle();

        hasZombie = false;
        trappedZombie = null;
    }

    private void destroyCircle() {
        for(int ix = -1; ix <= 1; ix++) {
            for(int iz = -1; iz <= 1; iz++) {
                Block b = worldObj.getBlock(centerX + ix, centerY, centerZ + iz);
                if (b instanceof BlockExorcismCircle) {
                    worldObj.setBlockToAir(centerX + ix, centerY, centerZ + iz);
                }
            }
        }
    }
}