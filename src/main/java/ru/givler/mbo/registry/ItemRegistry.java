package ru.givler.mbo.registry;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.item.Item;
import noppes.npcs.constants.EnumNpcToolMaterial;
import ru.givler.mbo.item.*;
import ru.givler.mbo.item.amulets.*;
import ru.givler.mbo.item.belt.ItemFallBelt;
import ru.givler.mbo.item.belt.ItemFertilityBelt;
import ru.givler.mbo.item.belt.ItemMinerBelt;
import ru.givler.mbo.item.ring.*;
import ru.givler.mbo.item.glyph.*;

import ru.givler.mbo.item.glyph.ItemGlyphWeapon;
import ru.givler.mbo.item.weapon.*;


public class ItemRegistry {
    //переменные  предметов тотемов
    public static Item GlyphAmphibian, GlyphDragon, GlyphHawk, GlyphMiner, GlyphOwl, GlyphWeapon, GlyphCleansing, GlyphHealing, BrokenStaffHealing;
    // переменные оружия ближнего боя
    public static ItemWeaponBase BrokenLongsword, BrokenSword, BrokenRapier, BrokenMace, BrokenAxe, BrokenDagger, BrokenCudgel, Uchigatana, DragonSlayer;
    // переменные призрачного оружия
    public static ItemGhostWeapon WeaponRapier;
    // переменные луков
    public static net.minecraft.item.ItemBow BrokenBowHunting ;
    // переменные материалов
    public static Item Metal, SapphireHeart, SapphireEye, Crystall, GlyphVoid;
    // переменные амулетов
    public static Item HealingAmulet, VampirismAmulet, CleansingAmulet, PhoenixAmulet, CowardAmulet, DragonAmulet, StaminaAmulet, VeilAmulet,
        ThornsAmulet, StrengthAmulet, MercenaryAmulet, GoldBasicAmulet, SilverBasicAmulet;
    // переменные колец
    public static Item LifeRing, StaminaRing, DamageRing, SpeedRing, LifeSmallRing, StaminaSmallRing, DamageSmallRing, SpeedSmallRing,
        SmallBasicRing, BasicRing;
    // переменные пояса
    public static Item FertilityBelt, FallBelt, MinerBelt, WaterminerBelt, KnightBelt;
    //магические посохи
    public static Item BrokenStaffFire, BrokenGrimoireWater;
    public static  Item Chalk, SilverCross;

    static {
        Item.ToolMaterial BrokenLongswordMat = ItemWeaponBase.createMaterial("BrokenLongswordMat", 0, 800, 0.0F, 1.5F, 30);
        Item.ToolMaterial BrokenSwordMat = ItemWeaponBase.createMaterial("BrokenSwordMat", 0, 800, 0.0F, 0.0F, 30);
        Item.ToolMaterial BrokenRapierMat = ItemWeaponBase.createMaterial("BrokenRapierMat", 0, 800, 0.0F, -1.0F, 30);
        Item.ToolMaterial BrokenDaggerMat = ItemWeaponBase.createMaterial("BrokenDaggerMat", 0, 800, 0.0F, -2.0F, 30);
        Item.ToolMaterial BrokenMaceMat = ItemWeaponBase.createMaterial("BrokenMaceMat", 0, 800, 0.0F, 1.0F, 30);
        Item.ToolMaterial BrokenAxeMat = ItemWeaponBase.createMaterial("BrokenAxeMat", 0, 800, 0.0F, 0.5F, 30);
        Item.ToolMaterial Divine = ItemWeaponBase.createMaterial("Divine", 3, 10000, 0.0F, 10000.0f, 30);
        Item.ToolMaterial DragonSlayerMat  = ItemWeaponBase.createMaterial("DragonSlayerMat", 3, 800, 0.0F, 12.0f, 30);

        BrokenLongsword = new ItemGreatswordMBO("BrokenLongsword", "broadsword", BrokenLongswordMat, 80, 1, 1.3F);
        BrokenSword = new ItemSwordMBO("BrokenSword", "brokenstraightsword", BrokenSwordMat, 80, 1, 1F);
        BrokenDagger = new ItemDaggerMBO("BrokenDagger", "ruineddagger", BrokenDaggerMat, 60, 1, 0.9F);
        BrokenRapier = new ItemRapierMBO("BrokenRapier", "bluntedrapier", BrokenRapierMat, 50, 1, 1F);
        BrokenMace = new ItemMaceMBO("BrokenMace", "brokenshestoper", BrokenMaceMat, 50, 1, 1F);
        BrokenAxe = new ItemBattleaxeMBO("BrokenAxe", "therustyaxe", BrokenAxeMat, 50, 1, 1F);
        BrokenCudgel = new ItemMaceMBO("BrokenCudgel", "cudgel", BrokenSwordMat, 80, 1, 1F);

        BrokenBowHunting = new ItemBowMBO("BrokenBowHunting", "brokenlittlecrossbow", 30, 0.25F, 0.7F);
        WeaponRapier = new ItemGhostWeapon("WeaponRapier", "mithrilsword", BrokenSwordMat, 40, 1, 1.0F);
        Uchigatana = new ItemSwordMBO("Uchigatana", "uchigatana", Divine, 10000, 1, 1.6F);
        DragonSlayer = new ItemDragonSlayerMBO("DragonSlayer", "dragon_slayer", DragonSlayerMat, 1750, 1, 1.8F);

    }

    @Mod.EventHandler
    public static void preLoad(FMLPreInitializationEvent event) {
        //глифы
        GlyphAmphibian = new ItemGlyphAmphibian("GlyphAmphibian", "glyph_amphibian", 1);
        GlyphDragon = new ItemGlyphDragon("GlyphDragon", "glyph_dragon", 1);
        GlyphHawk = new ItemGlyphHawk("GlyphHawk", "glyph_hawk", 1);
        GlyphMiner = new ItemGlyphMiner("GlyphMiner", "glyph_miner", 1);
        GlyphOwl = new ItemGlyphOwl("GlyphOwl", "glyph_owl", 1);
        GlyphWeapon = new ItemGlyphWeapon("GlyphWeapon", "glyph_weapon", 1);
        GlyphCleansing = new ItemGlyphCleansing("GlyphCleansing", "glyph_cleansing", 1);
        GlyphHealing = new ItemGlyphMHealing("GlyphHealing", "glyph_healing", 1);
        BrokenStaffHealing = new ItemStaffHealing("BrokenStaffHealing", "staff", 1);

        //материлаы
        Metal = new ItemMeta("Metal", "material/metal", 64, 1);
        SapphireHeart = new ItemBase("SapphireHeart", "material/sapphire_heart", 64);
        SapphireEye = new ItemBase("SapphireEye", "material/sapphire_eye", 64);
        Crystall = new ItemMeta("Crystall", "material/crystall", 64, 16);
        GlyphVoid = new ItemBase("GlyphVoid", "glyph/glyph_void", 1);

        //бижютерия
        GoldBasicAmulet = new ItemVoidAmulet("GoldBasicAmulet", "bijouterie/amulet_basic_gold");
        SilverBasicAmulet = new ItemVoidAmulet("SilverBasicAmulet", "bijouterie/amulet_basic_silver");

        HealingAmulet = new ItemHealingAmulet("HealingAmulet", "bijouterie/amulet_healing");
        VampirismAmulet = new ItemVampirismAmulet("VampirismAmulet", "bijouterie/amulet_vampirism");
        CleansingAmulet = new ItemCleansingAmulet("CleansingAmulet", "bijouterie/amulet_cleansing");
        PhoenixAmulet = new ItemPhoenixAmulet("PhoenixAmulet", "bijouterie/amulet_phoenix");
        CowardAmulet = new ItemCowardAmulet("CowardAmulet", "bijouterie/amulet_coward");
        DragonAmulet = new ItemDragonAmulet("DragonAmulet", "bijouterie/amulet_dragon");
        StaminaAmulet = new ItemStaminaAmulet("StaminaAmulet", "bijouterie/amulet_stamina");
        VeilAmulet = new ItemVeilAmulet("VeilAmulet", "bijouterie/amulet_veil");
        ThornsAmulet = new ItemThornsAmulet("ThronsAmulet", "bijouterie/amulet_thorns");
        StrengthAmulet = new ItemStrengthAmulet("StrengthAmulet", "bijouterie/amulet_strength");
        MercenaryAmulet = new ItemMercenaryAmulet("MercenaryAmulet", "bijouterie/amulet_mercenary");

        SmallBasicRing = new ItemVoidRing("SmallBasicRing", "bijouterie/ring_basic_small");
        BasicRing = new ItemVoidRing("BasicRing", "bijouterie/ring_basic");

        LifeSmallRing = new ItemLifeRing("LifeSmallRing", "bijouterie/ring_small_life", 2.0D, "0");
        StaminaSmallRing  = new ItemStaminaRing("StaminaSmallRing", "bijouterie/ring_small_stamina", 15F, "0");
        DamageSmallRing = new ItemDamageRing("DamageSmallRing", "bijouterie/ring_small_damage", 0.05D, "0" );
        SpeedSmallRing = new ItemSpeedRing("SpeedSmallRing", "bijouterie/ring_small_speed", 0.05D, "0");

        LifeRing = new ItemLifeRing("LifeRing", "bijouterie/ring_life", 4.0D, "1");
        StaminaRing  = new ItemStaminaRing("StaminaRing", "bijouterie/ring_stamina", 25F, "1");
        DamageRing = new ItemDamageRing("DamageRing", "bijouterie/ring_damage", 0.075D, "1" );
        SpeedRing = new ItemSpeedRing("SpeedRing", "bijouterie/ring_speed", 0.075D, "1");

        FertilityBelt = new ItemFertilityBelt("FertilityBelt", "bijouterie/belt_fertility");
        FallBelt = new ItemFallBelt("FallBelt", "bijouterie/belt_fall");
        MinerBelt = new ItemMinerBelt("MinerBelt", "bijouterie/belt_miner");
        WaterminerBelt = new ItemMinerBelt("WaterminerBelt", "bijouterie/belt_waterminer");
        KnightBelt = new ItemMinerBelt("KnightBelt", "bijouterie/belt_knight").setMaxDamage(1);

        if (Loader.isModLoaded("customnpcs")) {
            BrokenStaffFire = new ItemStaffBase(26780, EnumNpcToolMaterial.WOOD).setFull3D().setMaxStackSize(1)
                    .setUnlocalizedName("BrokenStaffFire").setMaxDamage(10)
                    .setTextureName("mbo:brokenwizardswand");
            BrokenGrimoireWater = new ItemStaffBase(26780, EnumNpcToolMaterial.WOOD).setMaxStackSize(1)
                    .setUnlocalizedName("BrokenGrimoireWater").setMaxDamage(10)
                    .setTextureName("mbo:distortedholyscripture");
        }

        Chalk = new ItemChalk("Chalk", "chalk", 1);
        SilverCross = new ItemSilverCross("SilverCross", "silver_cross", 1);

        BrokenLongsword.register();
        BrokenSword.register();
        BrokenRapier.register();
        BrokenMace.register();
        BrokenAxe.register();
        BrokenDagger.register();
        WeaponRapier.register();
        Uchigatana.register();
        BrokenCudgel.register();
        DragonSlayer.register();
        ((ItemBowMBO) BrokenBowHunting).register();
    }

}
