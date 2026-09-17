package ru.givler.mbo.magic.spell;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

/** Smelts at most five inventory items, scanning the hotbar first. */
public final class PocketFurnaceExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext context) {
    if (!(context.caster() instanceof EntityPlayer)) return SpellResult.BLOCKED;
    EntityPlayer player = (EntityPlayer) context.caster();
    int remaining = 5;
    if (!context.world().isRemote) {
      for (int slot = 0; slot < player.inventory.getSizeInventory() && remaining > 0; slot++) {
        ItemStack input = player.inventory.getStackInSlot(slot);
        if (input == null) continue;
        ItemStack recipe = FurnaceRecipes.smelting().getSmeltingResult(input);
        if (recipe == null) continue;
        int count = Math.min(input.stackSize, remaining);
        ItemStack output = recipe.copy();
        output.stackSize = count;
        player.inventory.decrStackSize(slot, count);
        if (!player.inventory.addItemStackToInventory(output)) {
          player.dropPlayerItemWithRandomChoice(output, false);
        }
        remaining -= count;
      }
      player.inventory.markDirty();
    } else {
      for (int i = 0; i < 10; i++)
        context
            .world()
            .spawnParticle(
                "flame",
                context.caster().posX + context.world().rand.nextFloat() * 2.0F - 1.0F,
                context.caster().posY
                    + context.caster().getEyeHeight()
                    - 0.5F
                    + context.world().rand.nextFloat(),
                context.caster().posZ + context.world().rand.nextFloat() * 2.0F - 1.0F,
                0,
                0.01D,
                0);
    }
    if (!context.world().isRemote && remaining == 5) return SpellResult.PASS;
    context.caster().playSound("fire.fire", 1.0F, 0.75F);
    context.caster().swingItem();
    return SpellResult.SUCCESS;
  }
}
