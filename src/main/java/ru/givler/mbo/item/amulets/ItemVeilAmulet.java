package ru.givler.mbo.item.amulets;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import ru.givler.mbo.item.ItemAmuletBase;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.BlockRegistry;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class ItemVeilAmulet extends ItemAmuletBase {
    public ItemVeilAmulet(String name, String texture) {
        this.setUnlocalizedName(name);
        this.setTextureName(MoreBeyondOrdinary.MODID + ":" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxStackSize(1);
        this.setMaxDamage(8);
        GameRegistry.registerItem(this, name);
    }

    @Override
    public void activate(EntityPlayer player, ItemStack stack) {
        int radius = 5;
        int delayTicks = 20 * 10;
        World world = player.worldObj;

        if (world.isRemote) return;

        int px = (int) Math.floor(player.posX);
        int py = (int) Math.floor(player.posY);
        int pz = (int) Math.floor(player.posZ);

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double dist = Math.sqrt(x * x + y * y + z * z);
                    if (dist >= radius - 0.5 && dist <= radius + 0.5) {
                        int bx = px + x;
                        int by = py + y;
                        int bz = pz + z;
                        Block block = world.getBlock(bx, by, bz);
                        if (block.isAir(world, bx, by, bz)) {
                            world.setBlock(bx, by, bz, BlockRegistry.BlockGlass);
                            scheduleGlassRemoval(world, bx, by, bz, delayTicks);
                        }
                    }
                }
            }
        }
    }

    private void scheduleGlassRemoval(final World world, final int x, final int y, final int z, int delay) {
        world.scheduleBlockUpdate(x, y, z, BlockRegistry.BlockGlass, delay);
    }

    @Override
    public void onWornTick(ItemStack itemstack, EntityLivingBase player) {
    }

    @Override
    public int getCooldownTicks() {
        return 20 * 60;
    }

    @Override
    public int getExperienceCost() {
        return 4;
    }
}
