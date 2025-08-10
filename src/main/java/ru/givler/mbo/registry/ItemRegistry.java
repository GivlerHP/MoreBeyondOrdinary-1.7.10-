package ru.givler.mbo.registry;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.item.Item;
import ru.givler.mbo.item.*;
import ru.givler.mbo.item.amulets.*;
import ru.givler.mbo.item.belt.ItemFallBelt;
import ru.givler.mbo.item.belt.ItemFertilityBelt;
import ru.givler.mbo.item.belt.ItemMinerBelt;
import ru.givler.mbo.item.ring.*;
import ru.givler.mbo.item.totems.*;

import ru.givler.mbo.item.totems.ItemTotemWeapon;



public class ItemRegistry {
    //Ниже переменные для предметов тотемов
    public static Item TotemAmphibian, TotemDragon, TotemHawk, TotemMiner, TotemOwl, TotemWeapon, TotemCleansing, TotemHealing;
    //ниже переменные для оружия ближнего боя
    public static ItemWeapon RustyLongsword, Uchigatana;
    //ниже переменные для призрачного оружия
    public static ItemGhostWeapon WeaponRapier;
    //ниже переменные для луков
    public static net.minecraft.item.ItemBow OldBowHunting ;
    //ниже переменные для материалов
    public static Item Metal, SapphireHeart, SapphireEye, Crystall;
    //ниже переменные для амулетов
    public static Item HealingAmulet, VampirismAmulet, CleansingAmulet, PhoenixAmulet, CowardAmulet, DragonAmulet, StaminaAmulet, VeilAmulet,
        ThornsAmulet, StrengthAmulet, GoldBasicAmulet, SilverBasicAmulet;
    //ниже переменные для колец
    public static Item LifeRing, StaminaRing, DamageRing, SpeedRing, LifeSmallRing, StaminaSmallRing, DamageSmallRing, SpeedSmallRing,
        SmallBasicRing, BasicRing;
    //ниже переменные для пояса
    public static Item FertilityBelt, FallBelt, MinerBelt, WaterminerBelt;

    static {
        Item.ToolMaterial RustyMaterial = ItemWeapon.createMaterial("RustyMaterial", 0, 800, 0.0F, 0.0F, 30);
        Item.ToolMaterial Divine = ItemWeapon.createMaterial("Divine", 3, 10000, 0.0F, 10000.0f, 30);

        RustyLongsword = new ItemWeapon("RustyLongsword", "weapon/broadsword", RustyMaterial, 80, 1, 1.3F);
        OldBowHunting = new ItemBow("OldBowHunting", "weapon/brokenlittlecrossbow", 40, 25, -1);
        WeaponRapier = new ItemGhostWeapon("WeaponRapier", "weapon/mithrilsword", RustyMaterial, 40, 1, 1.0F);
        Uchigatana = new ItemWeapon("Uchigatana", "weapon/uchigatana", Divine, 10000, 1, 1.6F);

    }

    @Mod.EventHandler
    public static void preLoad(FMLPreInitializationEvent event) {
        //ниже тотемы
        TotemAmphibian = new ItemTotemAmphibian("TotemAmphibian", "staff", 1);
        TotemDragon = new ItemTotemDragon("TotemDragon", "staff", 1);
        TotemHawk = new ItemTotemHawk("TotemHawk", "staff", 1);
        TotemMiner = new ItemTotemMiner("TotemMiner", "staff", 1);
        TotemOwl = new ItemTotemOwl("TotemOwl", "staff", 1);
        TotemWeapon = new ItemTotemWeapon("TotemWeapon", "staff", 1);
        TotemCleansing = new ItemTotemCleansing("TotemCleansing", "staff", 1);
        TotemHealing = new ItemTotemHealing("TotemHealing", "staff", 1);

        //ниже материлаы
        Metal = new ItemMeta("Metal", "material/metal", 64, 1);
        SapphireHeart = new ItemBasic("SapphireHeart", "material/sapphire_heart", 64);
        SapphireEye = new ItemBasic("SapphireEye", "material/sapphire_eye", 64);
        Crystall = new ItemMeta("Crystall", "material/crystall", 64, 15);

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

        SmallBasicRing = new ItemVoidRing("SmallBasicRing", "bijouterie/ring_basic_small");
        BasicRing = new ItemVoidRing("BasicRing", "bijouterie/ring_basic");

        LifeSmallRing = new ItemLifeRing("LifeSmallRing", "bijouterie/ring_small_life", 2.0D, "0");
        StaminaSmallRing  = new ItemStaminaRing("StaminaSmallRing", "bijouterie/ring_small_stamina", 15F, "0");
        DamageSmallRing = new ItemDamageRing("DamageSmallRing", "bijouterie/ring_small_damage", 1.0D, "0" );
        SpeedSmallRing = new ItemSpeedRing("SpeedSmallRing", "bijouterie/ring_small_speed", 0.05D, "0");

        LifeRing = new ItemLifeRing("LifeRing", "bijouterie/ring_life", 4.0D, "1");
        StaminaRing  = new ItemStaminaRing("StaminaRing", "bijouterie/ring_stamina", 25F, "1");
        DamageRing = new ItemDamageRing("DamageRing", "bijouterie/ring_damage", 1.5D, "1" );
        SpeedRing = new ItemSpeedRing("SpeedRing", "bijouterie/ring_speed", 0.075D, "1");

        FertilityBelt = new ItemFertilityBelt("FertilityBelt", "bijouterie/belt_fertility");
        FallBelt = new ItemFallBelt("FallBelt", "bijouterie/belt_fall");
        MinerBelt = new ItemMinerBelt("MinerBelt", "bijouterie/belt_miner");
        WaterminerBelt = new ItemMinerBelt("WaterminerBelt", "bijouterie/belt_waterminer");

        RustyLongsword.register();
        Uchigatana.register();
        ((ItemBow) OldBowHunting).register();
    }

}
