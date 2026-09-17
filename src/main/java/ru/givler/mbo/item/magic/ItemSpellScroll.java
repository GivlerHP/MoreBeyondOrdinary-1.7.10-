package ru.givler.mbo.item.magic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import ru.givler.mbo.magic.api.CastType;
import ru.givler.mbo.magic.api.Spell;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellResult;
import ru.givler.mbo.magic.registry.SpellRegistry;
import ru.givler.mbo.registry.CreativeTabRegistry;
import ru.givler.mbo.registry.PotionRegistry;

/** Consumable scroll that stores a stable MBO spell id in NBT. */
public final class ItemSpellScroll extends Item {
  private static final String SPELL_ID_TAG = "SpellId";
  private static final String CHANNEL_REMAINING_TAG = "ChannelRemaining";
  private static final int CHANNEL_DURATION_TICKS = 100;

  public ItemSpellScroll() {
    setHasSubtypes(false);
    setMaxStackSize(16);
    setCreativeTab(CreativeTabRegistry.tabMBOmagic);
    setTextureName("mbo:scroll");
    setUnlocalizedName("spellScroll");
  }

  public ItemStack create(Spell spell) {
    ItemStack stack = new ItemStack(this);
    NBTTagCompound tag = new NBTTagCompound();
    tag.setString(SPELL_ID_TAG, spell.id().toString());
    stack.setTagCompound(tag);
    return stack;
  }

  public Spell getSpell(ItemStack stack) {
    if (stack == null || !stack.hasTagCompound()) return null;
    String value = stack.getTagCompound().getString(SPELL_ID_TAG);
    return value.isEmpty() ? null : SpellRegistry.find(new ResourceLocation(value));
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void getSubItems(Item item, CreativeTabs tab, List items) {
    for (Spell spell : SpellRegistry.values()) {
      items.add(create(spell));
    }
  }

  @Override
  public boolean hasEffect(ItemStack stack, int pass) {
    return getSpell(stack) != null;
  }

  @Override
  public String getItemStackDisplayName(ItemStack stack) {
    return StatCollector.translateToLocal("item.spellScroll.name");
  }

  @Override
  @SideOnly(Side.CLIENT)
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
    Spell spell = getSpell(stack);
    if (spell == null) {
      lines.add(
          EnumChatFormatting.RED + StatCollector.translateToLocal("item.spellScroll.invalid"));
      return;
    }
    lines.add(
        StatCollector.translateToLocalFormatted(
            "item.spellScroll.spell", spell.element().color() + spellName(spell)));
    lines.add(
        StatCollector.translateToLocalFormatted(
            "item.spellScroll.tier",
            spell.tier().color() + StatCollector.translateToLocal(spell.tier().translationKey())));
    if (GuiScreen.isShiftKeyDown()) {
      lines.addAll(
          net.minecraft.client.Minecraft.getMinecraft()
              .fontRenderer
              .listFormattedStringToWidth(
                  EnumChatFormatting.GRAY + StatCollector.translateToLocal(spell.descriptionKey()),
                  240));
    } else {
      lines.add(
          EnumChatFormatting.DARK_GRAY
              + StatCollector.translateToLocal("item.spellScroll.holdShift"));
    }
  }

  @Override
  public EnumAction getItemUseAction(ItemStack stack) {
    Spell spell = getSpell(stack);
    return spell == null ? EnumAction.none : spell.useAction();
  }

  @Override
  public int getMaxItemUseDuration(ItemStack stack) {
    return CHANNEL_DURATION_TICKS;
  }

  @Override
  public boolean showDurabilityBar(ItemStack stack) {
    Spell spell = getSpell(stack);
    return spell != null
        && spell.castType() == CastType.CONTINUOUS
        && getChannelRemaining(stack) < CHANNEL_DURATION_TICKS;
  }

  @Override
  public double getDurabilityForDisplay(ItemStack stack) {
    return 1.0D - getChannelRemaining(stack) / (double) CHANNEL_DURATION_TICKS;
  }

  @Override
  public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
    Spell spell = getSpell(stack);
    if (spell == null) return stack;
    if (player.isPotionActive(PotionRegistry.ArcaneJammer)) return stack;

    if (spell.castType() == CastType.CONTINUOUS) {
      player.setItemInUse(stack, Math.max(1, getChannelRemaining(stack)));
      return stack;
    }

    SpellResult result = spell.cast(new SpellContext(world, player, null, stack, 0, 1, 1, 1, 1));
    if (result.consumesResources() && !world.isRemote) {
      if (!player.capabilities.isCreativeMode) stack.stackSize--;
    }
    return stack;
  }

  @Override
  public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
    Spell spell = getSpell(stack);
    if (spell == null || spell.castType() != CastType.CONTINUOUS) {
      player.clearItemInUse();
      return;
    }
    if (player.isPotionActive(PotionRegistry.ArcaneJammer)) {
      player.clearItemInUse();
      return;
    }

    int remaining = getChannelRemaining(stack);
    if (remaining <= 0) {
      finishChannel(stack, player);
      return;
    }

    int ticksUsed = CHANNEL_DURATION_TICKS - remaining;
    SpellResult result =
        spell.cast(new SpellContext(player.worldObj, player, null, stack, ticksUsed, 1, 1, 1, 1));
    setChannelRemaining(stack, remaining - 1);
    if (remaining == 1) finishChannel(stack, player);
  }

  private static void finishChannel(ItemStack stack, EntityPlayer player) {
    player.clearItemInUse();
    if (player.capabilities.isCreativeMode) {
      setChannelRemaining(stack, CHANNEL_DURATION_TICKS);
      return;
    }
    stack.stackSize--;
    if (stack.stackSize > 0) setChannelRemaining(stack, CHANNEL_DURATION_TICKS);
  }

  private static int getChannelRemaining(ItemStack stack) {
    if (stack == null || !stack.hasTagCompound()) return CHANNEL_DURATION_TICKS;
    NBTTagCompound tag = stack.getTagCompound();
    return tag.hasKey(CHANNEL_REMAINING_TAG)
        ? Math.max(0, Math.min(CHANNEL_DURATION_TICKS, tag.getInteger(CHANNEL_REMAINING_TAG)))
        : CHANNEL_DURATION_TICKS;
  }

  private static void setChannelRemaining(ItemStack stack, int remaining) {
    if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
    stack
        .getTagCompound()
        .setInteger(
            CHANNEL_REMAINING_TAG, Math.max(0, Math.min(CHANNEL_DURATION_TICKS, remaining)));
  }

  private static String spellName(Spell spell) {
    return StatCollector.translateToLocal(spell.nameKey());
  }
}
