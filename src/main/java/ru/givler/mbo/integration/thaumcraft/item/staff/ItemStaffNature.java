package ru.givler.mbo.integration.thaumcraft.item.staff;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.givler.mbo.entity.magic.EntityForceOrb;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.Thaumcraft;


public class ItemStaffNature extends ItemStaffBasic {
    @SideOnly(Side.CLIENT)
    private IIcon narureIcon;

    private static final float DAMAGE_MULTIPLIER = 1.0F;
    private static final float BLAST_MULTIPLIER = 1.0F;

    public ItemStaffNature() {
        super();
        this.setUnlocalizedName("staffNature");
        this.setCreativeTab(Thaumcraft.tabTC);
        this.setMaxStackSize(1);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StatCollector.translateToLocal("item.staffNature.name");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        this.narureIcon = reg.registerIcon("mbo:staff/staffNature");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass) {
        return this.narureIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconIndex(ItemStack stack) {
        return this.narureIcon;
    }


    @Override
    public int getMaxVis(ItemStack stack) {
        return 50 * 100;
    }

    public AspectList getVisCost(ItemStack stack) {
        return (new AspectList()).add(Aspect.WATER, 80).add(Aspect.EARTH, 80);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player) {
        ItemStack handled = interactWithWandable(itemstack, world, player);
        if (handled != null) return handled;

        if (this.consumeAllVis(itemstack, player, this.getVisCost(itemstack), !world.isRemote, false)) {
            if (!world.isRemote) {
                EntityForceOrb forceOrb = new EntityForceOrb(world, player, DAMAGE_MULTIPLIER, BLAST_MULTIPLIER);
                world.spawnEntityInWorld(forceOrb);
                world.playSoundAtEntity(player, "mbo:shard", 1.0F,
                        0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
            }
            player.swingItem();
        }

        return itemstack;
    }
}
