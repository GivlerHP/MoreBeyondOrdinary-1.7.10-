package ru.givler.mbo.item.ring;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import minefantasy.mf2.api.stamina.StaminaBar;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.StatCollector;
import ru.givler.mbo.item.ItemRingBase;
import ru.givler.mbo.main;
import ru.givler.mbo.registry.CreativeTabRegistry;

import java.util.List;
import java.util.UUID;

public class ItemLifeRing extends ItemRingBase {
    public ItemLifeRing(String name, String texture) {
        this.setUnlocalizedName(name);
        this.setTextureName(main.MODID + ":" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxStackSize(1);
        this.setMaxDamage(10);
        GameRegistry.registerItem(this, name);
    }

    @Override
    public void onWornTick(ItemStack itemstack, EntityLivingBase player) {
        if (!itemstack.hasTagCompound()) {
            itemstack.setTagCompound(new net.minecraft.nbt.NBTTagCompound());
        }

        if (!itemstack.getTagCompound().hasKey("UniqueRingID")) {
            String uuid = UUID.randomUUID().toString();
            itemstack.getTagCompound().setString("UniqueRingID", uuid);
        }

        UUID uniqueId = UUID.fromString(itemstack.getTagCompound().getString("UniqueRingID"));

        if (player.getEntityAttribute(SharedMonsterAttributes.maxHealth)
                .getModifier(uniqueId) == null) {

            AttributeModifier modifier = new AttributeModifier(uniqueId, "ring_life", 2.0D, 0);
            player.getEntityAttribute(SharedMonsterAttributes.maxHealth).applyModifier(modifier);
        }
    }

    @Override
    public void onUnequipped(ItemStack itemstack, EntityLivingBase player) {
        if (!itemstack.hasTagCompound()) return;
        if (!itemstack.getTagCompound().hasKey("UniqueRingID")) return;

        UUID uniqueId = UUID.fromString(itemstack.getTagCompound().getString("UniqueRingID"));

        if (player.getEntityAttribute(SharedMonsterAttributes.maxHealth)
                .getModifier(uniqueId) != null) {
            player.getEntityAttribute(SharedMonsterAttributes.maxHealth)
                    .removeModifier(player.getEntityAttribute(SharedMonsterAttributes.maxHealth)
                            .getModifier(uniqueId));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        String key = "description.ring." + this.getUnlocalizedName().substring(5);
        if (StatCollector.canTranslate(key)) {
            list.add(StatCollector.translateToLocal(key));
        }
    }

}
