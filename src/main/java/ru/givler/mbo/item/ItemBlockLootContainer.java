package ru.givler.mbo.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.lootcontainer.LootContainerData;
import ru.givler.mbo.lootcontainer.action.ApplyEffectAction;
import ru.givler.mbo.lootcontainer.action.ApplyExplosionEffectAction;
import ru.givler.mbo.lootcontainer.action.BurningAction;
import ru.givler.mbo.lootcontainer.action.ExplosionBurningAction;
import ru.givler.mbo.lootcontainer.action.ExplosionInstantDamageAction;
import ru.givler.mbo.lootcontainer.action.InstantDamageAction;
import ru.givler.mbo.lootcontainer.action.ItemDropAction;
import ru.givler.mbo.lootcontainer.action.LootContainerAction;
import ru.givler.mbo.lootcontainer.action.SpawnEntityAction;

import java.util.List;

public class ItemBlockLootContainer extends ItemBlock {
    public ItemBlockLootContainer(Block block) {
        super(block);
        setMaxStackSize(1);
    }

    public static boolean isEditor(EntityPlayer player) {
        return player != null &&
                (player.capabilities.isCreativeMode || player.canCommandSenderUseCommand(2, "mbo.lootcontainer"));
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        MovingObjectPosition hit = this.getMovingObjectPositionFromPlayer(world, player, true);
        if (hit == null && world.isRemote && isEditor(player)) {
            player.openGui(MoreBeyondOrdinary.instance, MoreBeyondOrdinary.GUI_LOOT_CONTAINER_CONFIG, world,
                    (int) player.posX, (int) player.posY, (int) player.posZ);
        }
        return stack;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        LootContainerData data = LootContainerData.fromItemStackNbt(stack == null ? null : stack.getTagCompound());
        if (data.customName != null && !data.customName.trim().isEmpty()) {
            return data.customName;
        }
        return super.getItemStackDisplayName(stack);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        LootContainerData data = LootContainerData.fromItemStackNbt(stack == null ? null : stack.getTagCompound());
        list.add(EnumChatFormatting.GRAY + "Variations:");
        if (data.modelVariations == null || data.modelVariations.isEmpty()) {
            list.add(EnumChatFormatting.DARK_GRAY + "  <none>");
        } else {
            for (int i = 0; i < data.modelVariations.size(); i++) {
                LootContainerData.ModelVariation variation = data.modelVariations.get(i);
                String model = variation == null || variation.normalModel == null || variation.normalModel.trim().isEmpty()
                        ? "<empty>" : variation.normalModel;
                list.add(EnumChatFormatting.GRAY + "  model " + (i + 1) + ": " + model);
            }
        }

        List<LootContainerAction> actions = data.getActions();
        list.add(EnumChatFormatting.DARK_GRAY + "Actions:");
        if (actions == null || actions.isEmpty()) {
            list.add(EnumChatFormatting.DARK_GRAY + "  <none>");
            return;
        }

        for (LootContainerAction action : actions) {
            String line = actionToTooltip(action);
            if (line != null && !line.isEmpty()) {
                list.add(EnumChatFormatting.DARK_GRAY + "  " + line);
            }
        }
    }

    private static String actionToTooltip(LootContainerAction action) {
        if (action == null) return "";
        String chance = formatChance(action.getChance());
        if (action instanceof ItemDropAction) {
            ItemDropAction itemDrop = (ItemDropAction) action;
            String itemId = itemDrop.itemId == null || itemDrop.itemId.trim().isEmpty() ? "<empty>" : itemDrop.itemId;
            String count = itemDrop.countExpr == null || itemDrop.countExpr.trim().isEmpty() ? "1" : itemDrop.countExpr;
            return "spawn item: " + itemId + ", " + count + ", " + chance;
        }
        if (action instanceof SpawnEntityAction) {
            SpawnEntityAction spawn = (SpawnEntityAction) action;
            String entityId = spawn.entityId == null || spawn.entityId.trim().isEmpty() ? "<empty>" : spawn.entityId;
            String spawnCount = spawn.countExpr == null || spawn.countExpr.trim().isEmpty()
                    ? String.valueOf(Math.max(1, spawn.spawnCount))
                    : spawn.countExpr;
            return "spawn entity: " + entityId + " x" + spawnCount + ", " + chance;
        }
        if (action instanceof ApplyEffectAction) {
            ApplyEffectAction effect = (ApplyEffectAction) action;
            String potionId = effect.potionId == null || effect.potionId.trim().isEmpty() ? "<empty>" : effect.potionId;
            return "apply effect: " + potionId + ", " + effect.durationSec + "s, amp " + effect.amplifier + ", " + chance;
        }
        if (action instanceof ApplyExplosionEffectAction) {
            ApplyExplosionEffectAction effect = (ApplyExplosionEffectAction) action;
            String potionId = effect.potionId == null || effect.potionId.trim().isEmpty() ? "<empty>" : effect.potionId;
            return "explosion effect: " + potionId + ", " + effect.durationSec + "s, amp " + effect.amplifier
                    + ", rad " + effect.radius + ", particles " + effect.particles + ", knockback " + effect.knockback + ", " + chance;
        }
        if (action instanceof InstantDamageAction) {
            InstantDamageAction damage = (InstantDamageAction) action;
            return "instant damage: " + damage.damage + ", " + chance;
        }
        if (action instanceof ExplosionInstantDamageAction) {
            ExplosionInstantDamageAction damage = (ExplosionInstantDamageAction) action;
            return "explosion instant damage: " + damage.damage + ", radius " + damage.radius
                    + ", particles " + damage.particles + ", knockback " + damage.knockback + ", " + chance;
        }
        if (action instanceof BurningAction) {
            BurningAction burning = (BurningAction) action;
            return "burning: " + burning.durationSec + "s, " + chance;
        }
        if (action instanceof ExplosionBurningAction) {
            ExplosionBurningAction burning = (ExplosionBurningAction) action;
            return "explosion burning: " + burning.durationSec + "s, radius " + burning.radius
                    + ", particles " + burning.particles + ", knockback " + burning.knockback + ", " + chance;
        }
        return action.getType() + ": " + chance;
    }

    private static String formatChance(float chance) {
        float rounded = Math.round(chance * 10.0F) / 10.0F;
        if (Math.abs(rounded - Math.round(rounded)) < 0.0001F) {
            return ((int) Math.round(rounded)) + "%";
        }
        return rounded + "%";
    }

    public static LootContainerData readData(ItemStack stack) {
        if (stack == null) return new LootContainerData();
        return LootContainerData.fromItemStackNbt(stack.getTagCompound());
    }

    public static void writeData(ItemStack stack, LootContainerData data) {
        if (stack == null || data == null) return;
        NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
        data.writeToItemStackNbt(tag);
        stack.setTagCompound(tag);
    }
}
