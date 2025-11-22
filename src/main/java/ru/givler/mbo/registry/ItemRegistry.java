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



public class ItemRegistry {
    //Ниже переменные для предметов тотемов
    public static Item GlyphAmphibian, GlyphDragon, GlyphHawk, GlyphMiner, GlyphOwl, GlyphWeapon, GlyphCleansing, GlyphHealing, BrokenStaffHealing;
    //ниже переменные для оружия ближнего боя
    public static ItemWeapon BrokenLongsword, BrokenSword, BrokenRapier, BrokenMace, BrokenAxe, BrokenDagger, BrokenCudgel, Uchigatana;
    //ниже переменные для призрачного оружия
    public static ItemGhostWeapon WeaponRapier;
    //ниже переменные для луков
    public static net.minecraft.item.ItemBow BrokenBowHunting ;
    //ниже переменные для материалов
    public static Item Metal, SapphireHeart, SapphireEye, Crystall, GlyphVoid;
    //ниже переменные для амулетов
    public static Item HealingAmulet, VampirismAmulet, CleansingAmulet, PhoenixAmulet, CowardAmulet, DragonAmulet, StaminaAmulet, VeilAmulet,
        ThornsAmulet, StrengthAmulet, MercenaryAmulet, GoldBasicAmulet, SilverBasicAmulet;
    //ниже переменные для колец
    public static Item LifeRing, StaminaRing, DamageRing, SpeedRing, LifeSmallRing, StaminaSmallRing, DamageSmallRing, SpeedSmallRing,
        SmallBasicRing, BasicRing;
    //ниже переменные для пояса
    public static Item FertilityBelt, FallBelt, MinerBelt, WaterminerBelt, KnightBelt;
    //магические посохи
    public static Item BrokenStaffFire, BrokenGrimoireWater;

    static {
        Item.ToolMaterial BrokenLongswordMat = ItemWeapon.createMaterial("BrokenLongswordMat", 0, 800, 0.0F, 1.5F, 30);
        Item.ToolMaterial BrokenSwordMat = ItemWeapon.createMaterial("BrokenSwordMat", 0, 800, 0.0F, 0.0F, 30);
        Item.ToolMaterial BrokenRapierMat = ItemWeapon.createMaterial("BrokenRapierMat", 0, 800, 0.0F, -1.0F, 30);
        Item.ToolMaterial BrokenDaggerMat = ItemWeapon.createMaterial("BrokenDaggerMat", 0, 800, 0.0F, -2.0F, 30);
        Item.ToolMaterial BrokenMaceMat = ItemWeapon.createMaterial("BrokenMaceMat", 0, 800, 0.0F, 1.0F, 30);
        Item.ToolMaterial BrokenAxeMat = ItemWeapon.createMaterial("BrokenAxeMat", 0, 800, 0.0F, 0.5F, 30);
        Item.ToolMaterial Divine = ItemWeapon.createMaterial("Divine", 3, 10000, 0.0F, 10000.0f, 30);

        BrokenLongsword = new ItemWeapon("BrokenLongsword", "broadsword", BrokenLongswordMat, 80, 1, 1.3F);
        BrokenSword = new ItemWeapon("BrokenSword", "brokenstraightsword", BrokenSwordMat, 80, 1, 1F);
        BrokenDagger = new ItemWeapon("BrokenDagger", "ruineddagger", BrokenDaggerMat, 60, 1, 0.9F);
        BrokenRapier = new ItemWeapon("BrokenRapier", "bluntedrapier", BrokenRapierMat, 50, 1, 1F);
        BrokenMace = new ItemWeapon("BrokenMace", "brokenshestoper", BrokenMaceMat, 50, 1, 1F);
        BrokenAxe = new ItemWeapon("BrokenAxe", "therustyaxe", BrokenAxeMat, 50, 1, 1F);
        BrokenCudgel = new ItemWeapon("BrokenCudgel", "cudgel", BrokenSwordMat, 80, 1, 1F);

        BrokenBowHunting = new ItemBow("BrokenBowHunting", "brokenlittlecrossbow", 30, 0.25F, 0.7F);
        WeaponRapier = new ItemGhostWeapon("WeaponRapier", "mithrilsword", BrokenSwordMat, 40, 1, 1.0F);
        Uchigatana = new ItemWeapon("Uchigatana", "uchigatana", Divine, 10000, 1, 1.6F);

    }

    @Mod.EventHandler
    public static void preLoad(FMLPreInitializationEvent event) {
        //ниже тотемы
        GlyphAmphibian = new ItemGlyphAmphibian("GlyphAmphibian", "glyph_amphibian", 1);
        GlyphDragon = new ItemGlyphDragon("GlyphDragon", "glyph_dragon", 1);
        GlyphHawk = new ItemGlyphHawk("GlyphHawk", "glyph_hawk", 1);
        GlyphMiner = new ItemGlyphMiner("GlyphMiner", "glyph_miner", 1);
        GlyphOwl = new ItemGlyphOwl("GlyphOwl", "glyph_owl", 1);
        GlyphWeapon = new ItemGlyphWeapon("GlyphWeapon", "glyph_weapon", 1);
        GlyphCleansing = new ItemGlyphCleansing("GlyphCleansing", "glyph_cleansing", 1);
        GlyphHealing = new ItemGlyphMHealing("GlyphHealing", "glyph_healing", 1);
        BrokenStaffHealing = new ItemStaffHealing("BrokenStaffHealing", "staff", 1);

        //ниже материлаы
        Metal = new ItemMeta("Metal", "material/metal", 64, 1);
        SapphireHeart = new ItemBasic("SapphireHeart", "material/sapphire_heart", 64);
        SapphireEye = new ItemBasic("SapphireEye", "material/sapphire_eye", 64);
        Crystall = new ItemMeta("Crystall", "material/crystall", 64, 15);
        GlyphVoid = new ItemBasic("GlyphVoid", "glyph/glyph_void", 1);

        //ниже бижютерия
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
            BrokenStaffFire = new ItemStaffBasic(26780, EnumNpcToolMaterial.WOOD).setFull3D().setMaxStackSize(1)
                    .setUnlocalizedName("BrokenStaffFire").setMaxDamage(10)
                    .setTextureName("mbo:brokenwizardswand");
            BrokenGrimoireWater = new ItemStaffBasic(26780, EnumNpcToolMaterial.WOOD).setMaxStackSize(1)
                    .setUnlocalizedName("BrokenGrimoireWater").setMaxDamage(10)
                    .setTextureName("mbo:distortedholyscripture");
        }

        BrokenLongsword.register();
        BrokenSword.register();
        BrokenRapier.register();
        BrokenMace.register();
        BrokenAxe.register();
        BrokenDagger.register();
        Uchigatana.register();
        BrokenCudgel.register();
        ((ItemBow) BrokenBowHunting).register();
    }

}
