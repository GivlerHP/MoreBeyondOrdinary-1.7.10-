package ru.givler.mbo.magic.spell;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

/** Gives the caster one temporary spectral item configured by the spell duration. */
public final class ConjureItemExecutor implements SpellExecutor {
  private final Item item;

  public ConjureItemExecutor(Item item) {
    this.item = item;
  }

  @Override
  public SpellResult cast(SpellContext context) {
    if (!(context.caster() instanceof EntityPlayer)) return SpellResult.INVALID;
    EntityPlayer player = (EntityPlayer) context.caster();
    if (player.inventory.hasItem(item)) return SpellResult.BLOCKED;
    if (context.world().isRemote) return SpellResult.SUCCESS;

    ItemStack summoned = new ItemStack(item);
    summoned.setTagCompound(new NBTTagCompound());
    summoned.getTagCompound().setFloat("durationMultiplier", Math.max(0.05F, context.duration()));
    if (!player.inventory.addItemStackToInventory(summoned)) return SpellResult.BLOCKED;
    player.inventory.markDirty();
    SpellEffects.sparkleBurst(player, 10, 0.7F, 0.9F, 1.0F);
    context.world().playSoundAtEntity(player, "mbo:aura", 1.0F, 1.0F);
    return SpellResult.SUCCESS;
  }
}
