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
import ru.givler.mbo.main;
import ru.givler.mbo.registry.CreativeTabRegistry;

import java.util.List;
import java.util.UUID;

public class ItemSpeedRing extends ItemRingBase {
    public ItemSpeedRing(String name, String texture) {
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

        if (player.getEntityAttribute(SharedMonsterAttributes.movementSpeed)
                .getModifier(uniqueId) == null) {

            AttributeModifier modifier = new AttributeModifier(uniqueId, "ring_speed", 0.05D, 1); // 5% множитель (тип 1)
            player.getEntityAttribute(SharedMonsterAttributes.movementSpeed).applyModifier(modifier);
        }
    }

    @Override
    public void onUnequipped(ItemStack itemstack, EntityLivingBase player) {
        if (!itemstack.hasTagCompound()) return;
        if (!itemstack.getTagCompound().hasKey("UniqueRingID")) return;

        UUID uniqueId = UUID.fromString(itemstack.getTagCompound().getString("UniqueRingID"));

        if (player.getEntityAttribute(SharedMonsterAttributes.movementSpeed)
                .getModifier(uniqueId) != null) {
            player.getEntityAttribute(SharedMonsterAttributes.movementSpeed)
                    .removeModifier(player.getEntityAttribute(SharedMonsterAttributes.movementSpeed)
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
