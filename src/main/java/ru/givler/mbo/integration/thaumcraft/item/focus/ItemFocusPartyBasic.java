package ru.givler.mbo.integration.thaumcraft.item.focus;

import cpw.mods.fml.common.Loader;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import thaumcraft.api.wands.ItemFocusBasic;

import java.util.ArrayList;
import java.util.List;

public abstract class ItemFocusPartyBasic extends ItemFocusBasic {

    @Override
    public boolean requiresSpecificArmor(ItemStack focusstack) {
        return true;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void addFocusInformation(ItemStack focusstack, EntityPlayer player, List tooltip, boolean advanced) {
        super.addFocusInformation(focusstack, player, tooltip, advanced);

        String label = StatCollector.translateToLocal("item.Focus.armorEfficiency");
        for (Object line : tooltip) {
            String plain = EnumChatFormatting.getTextWithoutFormattingCodes(String.valueOf(line));
            if (plain != null && plain.contains(label)) return;
        }

        int efficiency = Math.round(getArmorEfficiency(player, focusstack) * 100.0F);
        tooltip.add(EnumChatFormatting.DARK_PURPLE + label + " " + efficiency + "%");
    }

    protected double getPartyRadius(ItemStack focusstack) {
        return 8.0D;
    }

    protected List<EntityPlayer> getPartyTargets(World world, EntityPlayer player, ItemStack focusstack) {
        List<EntityPlayer> targets = new ArrayList<>();
        double radius = getPartyRadius(focusstack);

        List<EntityPlayer> nearby = world.getEntitiesWithinAABB(
                EntityPlayer.class,
                player.boundingBox.expand(radius, radius, radius)
        );

        for (EntityPlayer candidate : nearby) {
            if (candidate == player) {
                targets.add(candidate);
                continue;
            }
            if (Loader.isModLoaded("customnpcs") && isPartyMember(player, candidate)) {
                targets.add(candidate);
            }
        }
        return targets;
    }

    private static boolean isPartyMember(EntityPlayer owner, EntityPlayer candidate) {
        try {
            Class<?> bridge = Class.forName(
                    "ru.givler.mbo.integration.customnpcs.CustomNpcPartyAccess");
            return ((Boolean) bridge.getMethod("isPartyMember", EntityPlayer.class, EntityPlayer.class)
                    .invoke(null, owner, candidate)).booleanValue();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to access optional CustomNPC party API", e);
        }
    }

    protected final boolean canActivateFocus(ItemStack focusstack, World world, EntityPlayer player,
                                             boolean requirePartyArmor) {
        if (!hasRequiredResearch(player, focusstack)) {
            onKnowledgeCheckFailed(focusstack, world, player);
            return false;
        }
        if (requirePartyArmor && getThaumicArmorCount(player) < 2) {
            onArmorCheckFailed(focusstack, world, player);
            return false;
        }
        return true;
    }

    @Override
    public void applyFocusBacklash(ItemStack focusstack, World world, EntityPlayer player) {
        if (world.isRemote) return;

        player.attackEntityFrom(DamageSource.magic, 4.0F);
        player.addPotionEffect(new PotionEffect(Potion.confusion.id, 100, 0));
    }
}
