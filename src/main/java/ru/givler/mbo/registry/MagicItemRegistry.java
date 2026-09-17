package ru.givler.mbo.registry;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import ru.givler.mbo.item.magic.ItemSpectralAxe;
import ru.givler.mbo.item.magic.ItemSpectralBow;
import ru.givler.mbo.item.magic.ItemSpectralPickaxe;
import ru.givler.mbo.item.magic.ItemSpectralWeapon;
import ru.givler.mbo.item.magic.ItemSpellScroll;

/** Registers items owned by MBO's magic subsystem. */
public final class MagicItemRegistry {
  public static ItemSpellScroll spellScroll;
  public static ItemSpectralBow spectralBow;
  public static ItemSpectralWeapon spectralSword;
  public static ItemSpectralPickaxe spectralPickaxe;
  public static ItemSpectralAxe flamingAxe;
  public static ItemSpectralAxe frostAxe;

  private MagicItemRegistry() {}

  public static void preLoad(FMLPreInitializationEvent event) {
    spellScroll = new ItemSpellScroll();
    GameRegistry.registerItem(spellScroll, "spell_scroll");
    spectralSword = new ItemSpectralWeapon(Item.ToolMaterial.IRON, 600);
    spectralSword.setUnlocalizedName("spectral_sword");
    spectralSword.setTextureName("mbo:magic/spectral_sword");
    spectralSword.setCreativeTab(CreativeTabRegistry.tabMBOmagic);
    GameRegistry.registerItem(spectralSword, "spectral_sword");
    spectralPickaxe = new ItemSpectralPickaxe(Item.ToolMaterial.IRON, 600);
    spectralPickaxe.setUnlocalizedName("spectral_pickaxe");
    spectralPickaxe.setTextureName("mbo:magic/spectral_pickaxe");
    spectralPickaxe.setCreativeTab(CreativeTabRegistry.tabMBOmagic);
    GameRegistry.registerItem(spectralPickaxe, "spectral_pickaxe");
    flamingAxe =
        new ItemSpectralAxe(
            Item.ToolMaterial.IRON, 600, ru.givler.mbo.magic.item.SpectralEffects.AXE_IGNITE);
    flamingAxe.setUnlocalizedName("flaming_axe");
    flamingAxe.setTextureName("mbo:magic/flaming_axe");
    flamingAxe.setCreativeTab(CreativeTabRegistry.tabMBOmagic);
    GameRegistry.registerItem(flamingAxe, "flaming_axe");
    frostAxe =
        new ItemSpectralAxe(
            Item.ToolMaterial.IRON, 600, ru.givler.mbo.magic.item.SpectralEffects.FREEZE);
    frostAxe.setUnlocalizedName("frost_axe");
    frostAxe.setTextureName("mbo:magic/frost_axe");
    frostAxe.setCreativeTab(CreativeTabRegistry.tabMBOmagic);
    GameRegistry.registerItem(frostAxe, "frost_axe");
    spectralBow =
        new ItemSpectralBow(600, "mbo:magic/spectral_bow")
            .setImpactEffect(ru.givler.mbo.magic.item.SpectralEffects.NONE);
    spectralBow.setUnlocalizedName("spectral_bow");
    spectralBow.setCreativeTab(CreativeTabRegistry.tabMBOmagic);
    GameRegistry.registerItem(spectralBow, "spectral_bow");
  }
}
