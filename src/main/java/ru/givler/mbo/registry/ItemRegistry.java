package ru.givler.mbo.registry;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import electroblob.wizardry.item.ItemSpectralSword;
import net.minecraft.item.Item;
import net.minecraft.util.EnumChatFormatting;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.item.*;
import ru.givler.mbo.item.amulets.*;
import ru.givler.mbo.item.belt.ItemFallBelt;
import ru.givler.mbo.item.belt.ItemFertilityBelt;
import ru.givler.mbo.item.belt.ItemMinerBelt;
import ru.givler.mbo.item.ring.*;
import ru.givler.mbo.item.glyph.*;

import ru.givler.mbo.item.glyph.ItemGlyphWeapon;
import ru.givler.mbo.item.wand.ItemWandWizard;
import ru.givler.mbo.item.weapon.*;


public class ItemRegistry {
    //переменные  предметов тотемов
    public static Item GlyphAmphibian, GlyphDragon, GlyphHawk, GlyphMiner, GlyphOwl, GlyphWeapon, GlyphCleansing, GlyphHealing, BrokenStaffHealing;
    // переменные оружия ближнего боя
    public static ItemWeaponBase BrokenLongsword, BrokenSword, BrokenRapier, BrokenMace, BrokenAxe, BrokenDagger, BrokenCudgel, Uchigatana,
            DragonSlayer, TorchMat;
    // переменные призрачного оружия
    public static ItemSpectralSword WeaponRapier;
    // переменные луков
    public static net.minecraft.item.ItemBow BrokenBowHunting ;
    // переменные материалов
    public static Item Metal, SapphireHeart, SapphireEye, Crystall, GlyphVoid, Drop, TooltipDemo;
    // переменные амулетов
    public static Item HealingAmulet, VampirismAmulet, CleansingAmulet, PhoenixAmulet, CowardAmulet, DragonAmulet, StaminaAmulet, VeilAmulet,
        ThornsAmulet, StrengthAmulet, MercenaryAmulet, GoblinAmulet, GoldBasicAmulet, SilverBasicAmulet;
    // переменные колец
    public static Item LifeRing, StaminaRing, DamageRing, SpeedRing, LifeSmallRing, StaminaSmallRing, DamageSmallRing, SpeedSmallRing,
        SmallBasicRing, BasicRing, MushroomRing, StrengthAttributeRing, DexterityAttributeRing,
        EnduranceAttributeRing, SpiritAttributeRing;
    // переменные пояса
    public static Item FertilityBelt, FallBelt, MinerBelt, WaterminerBelt, KnightBelt;
    //магические посохи
    public static ItemWandBase BrokenWandWizard, BrokenWandPyromancer;
    // Призрачного оружия
    public static ItemTorchWeaponMBO TorchWeapon;
    public static Item Lockpick, AdminKey, LockableDoorItem;

    @Mod.EventHandler
    public static void preLoad(FMLPreInitializationEvent event) {
        Lockpick = new ItemLockpick();
        AdminKey = new ItemAdminKey();
        GameRegistry.registerItem(Lockpick, "Lockpick");
        GameRegistry.registerItem(AdminKey, "AdminKey");
        LockableDoorItem = new DoorItemBase(BlockRegistry.LockableDoor, "LockableDoorItem", "minecraft:door_wood");
        BlockRegistry.LockableDoor.setDropItem(LockableDoorItem);

        // Материалы и оружие
        Item.ToolMaterial BrokenLongswordMat = ItemWeaponBase.createMaterial("BrokenLongswordMat", 0, 800, 0.0F, 1.5F, 30);
        Item.ToolMaterial BrokenSwordMat = ItemWeaponBase.createMaterial("BrokenSwordMat", 0, 800, 0.0F, 0.0F, 30);
        Item.ToolMaterial BrokenRapierMat = ItemWeaponBase.createMaterial("BrokenRapierMat", 0, 800, 0.0F, -1.0F, 30);
        Item.ToolMaterial BrokenDaggerMat = ItemWeaponBase.createMaterial("BrokenDaggerMat", 0, 800, 0.0F, -2.0F, 30);
        Item.ToolMaterial BrokenMaceMat = ItemWeaponBase.createMaterial("BrokenMaceMat", 0, 800, 0.0F, 1.0F, 30);
        Item.ToolMaterial BrokenAxeMat = ItemWeaponBase.createMaterial("BrokenAxeMat", 0, 800, 0.0F, 0.5F, 30);
        Item.ToolMaterial Divine = ItemWeaponBase.createMaterial("Divine", 3, 10000, 0.0F, 10000.0f, 30);
        Item.ToolMaterial DragonSlayerMat  = ItemWeaponBase.createMaterial("DragonSlayerMat", 3, 800, 0.0F, 12.0f, 30);
        Item.ToolMaterial TorchMat = ItemWeaponBase.createMaterial("TorchMat", 0, 800, 0.0F, -2.0F, 30);

        BrokenLongsword = new ItemGreatswordMBO("BrokenLongsword", "broadsword", BrokenLongswordMat, 80, 1);
        BrokenSword = new ItemSwordMBO("BrokenSword", "brokenstraightsword", BrokenSwordMat, 110, 1);
        BrokenDagger = new ItemDaggerMBO("BrokenDagger", "ruineddagger", BrokenDaggerMat, 160, 1);
        BrokenRapier = new ItemRapierMBO("BrokenRapier", "bluntedrapier", BrokenRapierMat, 120, 1);
        BrokenMace = new ItemMaceMBO("BrokenMace", "brokenshestoper", BrokenMaceMat, 100, 1);
        BrokenAxe = new ItemBattleaxeMBO("BrokenAxe", "therustyaxe", BrokenAxeMat, 100, 1);
        BrokenCudgel = new ItemMaceMBO("BrokenCudgel", "cudgel", BrokenSwordMat, 200, 1);

        BrokenBowHunting = new ItemBowMBO("BrokenBowHunting", "brokenlittlecrossbow", 30, 0.25F, 0.7F);
        WeaponRapier = createSpectralSword("WeaponRapier", "mithrilsword", BrokenSwordMat, 800);
        Uchigatana = new ItemSwordMBO("Uchigatana", "uchigatana", Divine, 10000, 1);
        DragonSlayer = new ItemDragonSlayerMBO("DragonSlayer", "dragon_slayer", DragonSlayerMat, 1750, 1);

        TorchWeapon = new ItemTorchWeaponMBO("TorchWeapon", "torch", TorchMat, 800, 1)
                .setDescription("item.TorchWeapon.desc", EnumChatFormatting.RED);

        //глифы
        GlyphAmphibian = new ItemGlyphAmphibian("GlyphAmphibian", "glyph_amphibian", 1);
        GlyphDragon = new ItemGlyphDragon("GlyphDragon", "glyph_dragon", 1);
        GlyphHawk = new ItemGlyphHawk("GlyphHawk", "glyph_hawk", 1);
        GlyphMiner = new ItemGlyphMiner("GlyphMiner", "glyph_miner", 1);
        GlyphOwl = new ItemGlyphOwl("GlyphOwl", "glyph_owl", 1);
        GlyphWeapon = new ItemGlyphWeapon("GlyphWeapon", "glyph_weapon", 1);
        GlyphCleansing = new ItemGlyphCleansing("GlyphCleansing", "glyph_cleansing", 1);
        GlyphHealing = new ItemGlyphMHealing("GlyphHealing", "glyph_healing", 1);
        BrokenStaffHealing = new ItemStaffHealing("BrokenStaffHealing", "staff", 1)
                .setDescription("item.BrokenStaffHealing.desc", EnumChatFormatting.RED);

        //материлаы
        Metal = new ItemMeta("Metal", "material/metal", 64, 1);
        Drop = new ItemMeta("Drop", "material/drop", 64, 3);
        SapphireHeart = new ItemBase("SapphireHeart", "material/sapphire_heart", 64);
        SapphireEye = new ItemBase("SapphireEye", "material/sapphire_eye", 64);
        Crystall = new ItemMeta("Crystall", "material/crystall", 64, 16);
        GlyphVoid = new ItemBase("GlyphVoid", "glyph/glyph_void", 1);
        TooltipDemo = new ItemTooltipDemo();

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
        MercenaryAmulet = new ItemMercenaryAmulet("MercenaryAmulet", "bijouterie/amulet_mercenary")
                .setDescription("item.MercenaryAmulet.desc", EnumChatFormatting.BLUE);;
        GoblinAmulet = new ItemGoblinAmulet("GoblinAmulet", "bijouterie/amulet_goblin_ear");

        SmallBasicRing = new ItemVoidRing("SmallBasicRing", "bijouterie/ring_basic_small");
        BasicRing = new ItemVoidRing("BasicRing", "bijouterie/ring_basic");

        LifeSmallRing = new ItemStatRing("LifeSmallRing", "bijouterie/ring_small_life", ItemStatRing.Stat.HEALTH, 4.0D, "0");
        StaminaSmallRing = new ItemStatRing("StaminaSmallRing", "bijouterie/ring_small_stamina", ItemStatRing.Stat.STAMINA, 15.0D, "0");
        DamageSmallRing = new ItemStatRing("DamageSmallRing", "bijouterie/ring_small_damage", ItemStatRing.Stat.DAMAGE, 0.05D, "0");
        SpeedSmallRing = new ItemStatRing("SpeedSmallRing", "bijouterie/ring_small_speed", ItemStatRing.Stat.SPEED, 0.05D, "0");

        LifeRing = new ItemStatRing("LifeRing", "bijouterie/ring_life", ItemStatRing.Stat.HEALTH, 6.0D, "1");
        StaminaRing = new ItemStatRing("StaminaRing", "bijouterie/ring_stamina", ItemStatRing.Stat.STAMINA, 25.0D, "1");
        DamageRing = new ItemStatRing("DamageRing", "bijouterie/ring_damage", ItemStatRing.Stat.DAMAGE, 0.075D, "1");
        SpeedRing = new ItemStatRing("SpeedRing", "bijouterie/ring_speed", ItemStatRing.Stat.SPEED, 0.075D, "1");
        MushroomRing = new ItemMushroomRing("MushroomRing", "bijouterie/ring_mushroom", 4.0D, "1");

        if (Loader.isModLoaded("minefantasy2")) {
            StrengthAttributeRing = createOptionalAttributeRing("StrengthAttributeRing", "bijouterie/ring_strength", "STRENGTH");
            DexterityAttributeRing = createOptionalAttributeRing("DexterityAttributeRing", "bijouterie/ring_dexterity", "DEXTERITY");
            EnduranceAttributeRing = createOptionalAttributeRing("EnduranceAttributeRing", "bijouterie/ring_endurance", "ENDURANCE");
            SpiritAttributeRing = createOptionalAttributeRing("SpiritAttributeRing", "bijouterie/ring_spirit", "SPIRIT");
        }

        FertilityBelt = new ItemFertilityBelt("FertilityBelt", "bijouterie/belt_fertility");
        FallBelt = new ItemFallBelt("FallBelt", "bijouterie/belt_fall");
        MinerBelt = new ItemMinerBelt("MinerBelt", "bijouterie/belt_miner");
        WaterminerBelt = new ItemMinerBelt("WaterminerBelt", "bijouterie/belt_waterminer");
        KnightBelt = new ItemMinerBelt("KnightBelt", "bijouterie/belt_knight").setMaxDamage(1).setDescription("item.KnightBelt.desc", EnumChatFormatting.YELLOW);

        BrokenWandWizard = new ItemWandWizard(15);
        if (Loader.isModLoaded("Thaumcraft")) {
            BrokenWandPyromancer = createOptionalWand(
                    "ru.givler.mbo.item.wand.ItemWandPyromancer", 15);
        }

    }

    private static ItemSpectralSword createSpectralSword(String name, String texture,
                                                          Item.ToolMaterial material, int duration) {
        ItemSpectralSword item = new ItemSpectralSword(material);
        item.setUnlocalizedName(name);
        item.setTextureName(MoreBeyondOrdinary.MODID + ":weapon/" + texture);
        item.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        item.setMaxDamage(duration);
        item.setMaxStackSize(1);
        GameRegistry.registerItem(item, name);
        return item;
    }

    private static ItemWandBase createOptionalWand(String className, int durability) {
        try {
            return (ItemWandBase) Class.forName(className)
                    .getConstructor(int.class).newInstance(durability);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create optional item " + className, e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Item createOptionalAttributeRing(String name, String texture, String attributeName) {
        try {
            Class<?> itemClass = Class.forName("ru.givler.mbo.item.ring.ItemAttributeRing");
            Class<? extends Enum> attributeClass = (Class<? extends Enum>) Class.forName(
                    "ru.givler.mbo.item.ring.ItemAttributeRing$Attribute");
            Object attribute = Enum.valueOf(attributeClass, attributeName);
            return (Item) itemClass.getConstructor(String.class, String.class, attributeClass)
                    .newInstance(name, texture, attribute);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create optional MineFantasy ring " + name, e);
        }
    }

}
