package ru.givler.mbo.item.wand;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.item.ItemWandBase;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.registry.MagicSpells;
import ru.givler.mbo.registry.CreativeTabRegistry;

import java.util.List;

public class ItemWandWizard extends ItemWandBase {

    private static final float RANGE_MULTIPLIER = 1.0F;
    private static final float DAMAGE_MULTIPLIER = 1.0F;

    public ItemWandWizard(int maxDurability) {
        super("wandWizard", "wandWizard", maxDurability);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player) {
        boolean cast = MagicSpells.MAGIC_MISSILE.cast(new SpellContext(
                world, player, null, itemstack, 0,
                DAMAGE_MULTIPLIER, RANGE_MULTIPLIER, 1.0F, 1.0F)).consumesResources();
        if (cast && !world.isRemote && !player.capabilities.isCreativeMode) {
            itemstack.damageItem(1, player);
        }

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
