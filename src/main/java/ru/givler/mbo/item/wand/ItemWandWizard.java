package ru.givler.mbo.item.wand;


import electroblob.wizardry.entity.projectile.EntityMagicMissile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.item.ItemWandBase;
import ru.givler.mbo.registry.CreativeTabRegistry;

import java.util.List;

public class ItemWandWizard extends ItemWandBase {

    private static final float RANGE_MULTIPLIER = 1.0F;
    private static final float DAMAGE_MULTIPLIER = 1.0F;

    public ItemWandWizard(int maxDurability) {
        super(maxDurability);
        this.setUnlocalizedName("wandWizard");
        this.setTextureName(MoreBeyondOrdinary.MODID + ":wand/wandWizard");
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            EntityMagicMissile magicMissile = new EntityMagicMissile(world, player, 2 * RANGE_MULTIPLIER, DAMAGE_MULTIPLIER);
            world.spawnEntityInWorld(magicMissile);

            if (!player.capabilities.isCreativeMode) {
                itemstack.damageItem(1, player);
            }
        }

        player.swingItem();
        world.playSoundAtEntity(player, "wizardry:magic", 1.0F, world.rand.nextFloat() * 0.4F + 1.2F);

        return itemstack;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        super.addInformation(stack, player, list, advanced);
        list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("item.wandWizard.desc"));
    }

    @Override
    public EnumRarity getRarity(ItemStack itemStack) {
        return EnumRarity.uncommon;
    }
}
