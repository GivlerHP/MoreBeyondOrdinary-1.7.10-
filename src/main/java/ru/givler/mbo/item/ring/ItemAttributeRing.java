package ru.givler.mbo.item.ring;

import cpw.mods.fml.common.registry.GameRegistry;
import minefantasy.mf2.api.rpg.RPGAttributes;
import minefantasy.mf2.network.packet.AttributePacket;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.item.ItemRingBase;
import ru.givler.mbo.registry.CreativeTabRegistry;

import java.util.List;

/** A Baubles ring which contributes to one MineFantasy RPG attribute. */
public class ItemAttributeRing extends ItemRingBase {
    public static final String LEVEL_TAG = "AttributeLevel";
    public static final int BASE_BONUS = 2;
    public static final int MAX_LEVEL = 7;

    private static final String APPLIED_TAG = "MBOAttributeBonusApplied";
    private static final String OWNER_TAG = "MBOAttributeBonusOwner";

    public enum Attribute {
        STRENGTH {
            int get(EntityPlayer player) { return RPGAttributes.getBonusStrength(player); }
            void set(EntityPlayer player, int value) { RPGAttributes.setBonusStrength(player, value); }
        },
        DEXTERITY {
            int get(EntityPlayer player) { return RPGAttributes.getBonusDexterity(player); }
            void set(EntityPlayer player, int value) { RPGAttributes.setBonusDexterity(player, value); }
        },
        ENDURANCE {
            int get(EntityPlayer player) { return RPGAttributes.getBonusEndurance(player); }
            void set(EntityPlayer player, int value) { RPGAttributes.setBonusEndurance(player, value); }
        },
        SPIRIT {
            int get(EntityPlayer player) { return RPGAttributes.getBonusSpirit(player); }
            void set(EntityPlayer player, int value) { RPGAttributes.setBonusSpirit(player, value); }
        };

        abstract int get(EntityPlayer player);
        abstract void set(EntityPlayer player, int value);

        void add(EntityPlayer player, int amount) {
            set(player, Math.max(0, get(player) + amount));
        }
    }

    private final Attribute attribute;

    public ItemAttributeRing(String name, String texture, Attribute attribute) {
        this.attribute = attribute;
        setUnlocalizedName(name);
        setTextureName(MoreBeyondOrdinary.MODID + ":" + texture);
        setCreativeTab(CreativeTabRegistry.tabMBOitems);
        GameRegistry.registerItem(this, name);
    }

    public static int getLevel(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) return 0;
        return Math.max(0, Math.min(MAX_LEVEL, stack.getTagCompound().getInteger(LEVEL_TAG)));
    }

    public static void setLevel(ItemStack stack, int level) {
        if (stack == null) return;
        NBTTagCompound tag = getOrCreateTag(stack);
        tag.setInteger(LEVEL_TAG, Math.max(0, Math.min(MAX_LEVEL, level)));
    }

    public int getAttributeBonus(ItemStack stack) {
        return BASE_BONUS + getLevel(stack);
    }

    @Override
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public boolean hasEffect(ItemStack stack, int renderPass) {
        return getLevel(stack) >= 4;
    }

    @Override
    public void onWornTick(ItemStack stack, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer) || entity.worldObj.isRemote) return;
        updateBonus(stack, (EntityPlayer) entity);
    }

    @Override
    public void onEquipped(ItemStack stack, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer) || entity.worldObj.isRemote) return;
        updateBonus(stack, (EntityPlayer) entity);
    }

    private void updateBonus(ItemStack stack, EntityPlayer player) {
        NBTTagCompound tag = getOrCreateTag(stack);
        String owner = player.getUniqueID().toString();
        int applied = owner.equals(tag.getString(OWNER_TAG)) ? tag.getInteger(APPLIED_TAG) : 0;
        int wanted = getAttributeBonus(stack);

        if (applied != wanted) {
            attribute.add(player, wanted - applied);
            tag.setInteger(APPLIED_TAG, wanted);
            tag.setString(OWNER_TAG, owner);
            AttributePacket.sendSync(player);
        }
    }

    @Override
    public void onUnequipped(ItemStack stack, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer) || entity.worldObj.isRemote || !stack.hasTagCompound()) return;
        EntityPlayer player = (EntityPlayer) entity;
        NBTTagCompound tag = stack.getTagCompound();
        if (!player.getUniqueID().toString().equals(tag.getString(OWNER_TAG))) return;

        int applied = Math.max(0, tag.getInteger(APPLIED_TAG));
        if (applied > 0) {
            attribute.add(player, -applied);
            AttributePacket.sendSync(player);
        }
        tag.removeTag(APPLIED_TAG);
        tag.removeTag(OWNER_TAG);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        int level = getLevel(stack);
        return super.getItemStackDisplayName(stack) + (level > 0 ? " +" + level : "");
    }

    @Override
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        super.addInformation(stack, player, list, advanced);
        String key = "description.attribute_ring." + attribute.name().toLowerCase();
        if (StatCollector.canTranslate(key)) {
            list.add(EnumChatFormatting.GRAY + "+" + getAttributeBonus(stack) + " "
                    + StatCollector.translateToLocal(key));
        }
    }

    private static NBTTagCompound getOrCreateTag(ItemStack stack) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        return stack.getTagCompound();
    }
}
