package ru.givler.mbo.item.weapon;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.item.ItemSpectralSword;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

import java.util.List;
import java.util.Random;

public class ItemTorchWeaponMBO extends ItemSpectralSword {

    private static final float FIRE_CHANCE = 0.75f;
    private static final int FIRE_DURATION = 4;
    private static final String LIFE_TAG = "MBO_TorchLife";

    private final Random random = new Random();
    private final float scale;

    private String descriptionKey;
    private EnumChatFormatting descriptionColor = EnumChatFormatting.GRAY;
    private EnumRarity rarity = EnumRarity.rare;

    public ItemTorchWeaponMBO(String name, String texture, ToolMaterial material, int maxDamage, int maxStackSize, float scale) {
        super(material);
        this.canRepair = false;
        this.setUnlocalizedName(name);
        this.setTextureName(MoreBeyondOrdinary.MODID + ":weapon/" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxDamage(maxDamage);
        this.maxStackSize = maxStackSize;
        this.scale = scale;
    }

    public float getScale() {
        return scale;
    }

    public void register() {
        GameRegistry.registerItem(this, this.getUnlocalizedName().substring(5));
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        boolean result = super.hitEntity(stack, target, attacker);

        if (random.nextFloat() < FIRE_CHANCE) {
            target.setFire(FIRE_DURATION);
        }

        return result;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held) {
        if (!(entity instanceof EntityPlayer)) {
            return;
        }

        // Vanilla only renders the durability bar for a damaged stack. The real
        // displayed value is supplied by getDisplayDamage() below.
        if (stack.getItemDamage() == 0) {
            stack.setItemDamage(1);
        }

        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }

        int lifetime = stack.getTagCompound().getInteger(LIFE_TAG) + 1;
        stack.getTagCompound().setInteger(LIFE_TAG, lifetime);

        if (!world.isRemote && lifetime > getMaxDamage()) {
            ((EntityPlayer) entity).inventory.setInventorySlotContents(slot, null);
        }
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return getMaxDamage();
    }

    @Override
    public int getDisplayDamage(ItemStack stack) {
        return stack.hasTagCompound() ? stack.getTagCompound().getInteger(LIFE_TAG) : 0;
    }

    @Override
    public boolean hasEffect(ItemStack stack, int pass)
    {
        return false;
    }

    public ItemTorchWeaponMBO setDescription(String langKey) {
        this.descriptionKey = langKey;
        return this;
    }

    public ItemTorchWeaponMBO setDescription(String langKey, EnumChatFormatting color) {
        this.descriptionKey = langKey;
        this.descriptionColor = color;
        return this;
    }

    public ItemTorchWeaponMBO setRarity(EnumRarity rarity) {
        this.rarity = rarity;
        return this;
    }

    @Override
    public EnumRarity getRarity(ItemStack itemStack) {
        return rarity;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        super.addInformation(stack, player, list, advanced);
        if (descriptionKey != null) {
            String translated = StatCollector.translateToLocal(descriptionKey);
            for (String line : translated.replace("\\n", "\n").split("\n")) {
                list.add(descriptionColor + line);
            }
        }
    }
}
