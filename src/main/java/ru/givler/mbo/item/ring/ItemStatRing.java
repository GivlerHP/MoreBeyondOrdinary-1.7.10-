package ru.givler.mbo.item.ring;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import minefantasy.mf2.api.stamina.StaminaBar;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.item.ItemRingBase;
import ru.givler.mbo.registry.CreativeTabRegistry;

import java.util.UUID;

/** Shared implementation for the original health, stamina, damage and speed rings. */
public class ItemStatRing extends ItemRingBase {
    private static final String UUID_MOST = "StatRingUUIDMost";
    private static final String UUID_LEAST = "StatRingUUIDLeast";

    public enum Stat {
        HEALTH(SharedMonsterAttributes.maxHealth, 0, "ring_life"),
        DAMAGE(SharedMonsterAttributes.attackDamage, 1, "ring_damage_percent"),
        SPEED(SharedMonsterAttributes.movementSpeed, 1, "ring_speed"),
        STAMINA(null, 0, "ring_stamina");

        final IAttribute attribute;
        final int operation;
        final String modifierName;

        Stat(IAttribute attribute, int operation, String modifierName) {
            this.attribute = attribute;
            this.operation = operation;
            this.modifierName = modifierName;
        }
    }

    private final Stat stat;
    private final double value;

    public ItemStatRing(String name, String texture, Stat stat, double value, String descriptionLevel) {
        this.stat = stat;
        this.value = value;
        setUnlocalizedName(name);
        setTextureName(MoreBeyondOrdinary.MODID + ":" + texture);
        setCreativeTab(CreativeTabRegistry.tabMBOitems);
        setDescription("description.ring." + descriptionLevel + "." + name);
        GameRegistry.registerItem(this, name);
    }

    @Override
    public void onWornTick(ItemStack stack, EntityLivingBase entity) {
        if (entity.worldObj.isRemote) return;
        if (stat == Stat.STAMINA) {
            applyStamina(stack, entity);
        } else {
            applyVanillaAttribute(stack, entity);
        }
    }

    private void applyVanillaAttribute(ItemStack stack, EntityLivingBase entity) {
        IAttributeInstance instance = entity.getEntityAttribute(stat.attribute);
        UUID id = getOrCreateId(stack);
        AttributeModifier current = instance.getModifier(id);
        if (current != null && current.getOperation() == stat.operation
                && Double.compare(current.getAmount(), value) == 0) return;
        if (current != null) instance.removeModifier(current);
        instance.applyModifier(new AttributeModifier(id, stat.modifierName, value, stat.operation).setSaved(false));
    }

    private void applyStamina(ItemStack stack, EntityLivingBase entity) {
        if (!Loader.isModLoaded("minefantasy2") && !Loader.isModLoaded("MineFantasy2")) return;
        migrateLegacyStamina(stack, entity);
        IAttributeInstance instance = StaminaBar.getMaxStaminaBonusAttribute(entity);
        UUID id = getOrCreateId(stack);
        AttributeModifier current = instance.getModifier(id);
        if (current != null && Double.compare(current.getAmount(), value) == 0) return;
        if (current != null) instance.removeModifier(current);
        instance.applyModifier(new AttributeModifier(id, stat.modifierName, value, 0).setSaved(false));
    }

    @Override
    public void onUnequipped(ItemStack stack, EntityLivingBase entity) {
        if (entity.worldObj.isRemote) return;
        UUID id = getId(stack);
        if (id == null) return;

        if (stat == Stat.STAMINA) {
            if (!Loader.isModLoaded("minefantasy2") && !Loader.isModLoaded("MineFantasy2")) return;
            IAttributeInstance instance = StaminaBar.getMaxStaminaBonusAttribute(entity);
            remove(instance, id);
            float maximum = StaminaBar.getTotalMaxStamina(entity);
            if (StaminaBar.getStaminaValue(entity) > maximum) StaminaBar.setStaminaValue(entity, maximum);
        } else {
            remove(entity.getEntityAttribute(stat.attribute), id);
            if (stat == Stat.HEALTH && entity.getHealth() > entity.getMaxHealth()) {
                entity.setHealth(entity.getMaxHealth());
            }
        }
    }

    private static void remove(IAttributeInstance instance, UUID id) {
        AttributeModifier modifier = instance.getModifier(id);
        if (modifier != null) instance.removeModifier(modifier);
    }

    private void migrateLegacyStamina(ItemStack stack, EntityLivingBase entity) {
        if (!stack.hasTagCompound() || !stack.getTagCompound().getBoolean("StaminaAdded")) return;
        StaminaBar.removeBuffStaminaInfinite(entity, (float) value);
        stack.getTagCompound().removeTag("StaminaAdded");
    }

    private static UUID getOrCreateId(ItemStack stack) {
        UUID id = getId(stack);
        if (id != null) return id;
        id = UUID.randomUUID();
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setLong(UUID_MOST, id.getMostSignificantBits());
        tag.setLong(UUID_LEAST, id.getLeastSignificantBits());
        return id;
    }

    private static UUID getId(ItemStack stack) {
        if (!stack.hasTagCompound()) return null;
        NBTTagCompound tag = stack.getTagCompound();
        if (tag.hasKey(UUID_MOST) && tag.hasKey(UUID_LEAST)) {
            return new UUID(tag.getLong(UUID_MOST), tag.getLong(UUID_LEAST));
        }
        // Preserve UUIDs created by the old health/damage/speed implementations.
        if (tag.hasKey("UniqueRingID")) {
            try { return UUID.fromString(tag.getString("UniqueRingID")); }
            catch (IllegalArgumentException ignored) { }
        }
        return null;
    }
}
