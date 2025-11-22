package ru.givler.mbo.item.ring;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import ru.givler.mbo.item.ItemRingBase;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

import java.util.List;
import java.util.UUID;

public class ItemDamageRing extends ItemRingBase {
    private final double percentValue;
    private final String level;

    public ItemDamageRing(String name, String texture, double percentValue, String level) {
        this.setUnlocalizedName(name);
        this.setTextureName(MoreBeyondOrdinary.MODID + ":" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxStackSize(1);
        GameRegistry.registerItem(this, name);

        this.percentValue = percentValue;
        this.level = level;
    }

    @Override
    public void onWornTick(ItemStack itemstack, EntityLivingBase player) {
        if (!itemstack.hasTagCompound()) {
            itemstack.setTagCompound(new net.minecraft.nbt.NBTTagCompound());
        }

        if (!itemstack.getTagCompound().hasKey("UniqueRingID")) {
            itemstack.getTagCompound().setString("UniqueRingID", UUID.randomUUID().toString());
        }

        UUID uniqueId = UUID.fromString(itemstack.getTagCompound().getString("UniqueRingID"));

        if (player.getEntityAttribute(SharedMonsterAttributes.attackDamage)
                .getModifier(uniqueId) == null) {

            // operation = 1 → multiplicative BASE damage bonus
            AttributeModifier modifier = new AttributeModifier(uniqueId, "ring_damage_percent", this.percentValue, 1);
            player.getEntityAttribute(SharedMonsterAttributes.attackDamage).applyModifier(modifier);
        }
    }

    @Override
    public void onUnequipped(ItemStack itemstack, EntityLivingBase player) {
        if (!itemstack.hasTagCompound() || !itemstack.getTagCompound().hasKey("UniqueRingID")) return;

        UUID uniqueId = UUID.fromString(itemstack.getTagCompound().getString("UniqueRingID"));

        AttributeModifier mod = player.getEntityAttribute(SharedMonsterAttributes.attackDamage).getModifier(uniqueId);

        if (mod != null) {
            player.getEntityAttribute(SharedMonsterAttributes.attackDamage).removeModifier(mod);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        String key = "description.ring." + this.level + "." + this.getUnlocalizedName().substring(5);
        if (StatCollector.canTranslate(key)) {
            list.add(StatCollector.translateToLocal(key));
        }
    }
}
