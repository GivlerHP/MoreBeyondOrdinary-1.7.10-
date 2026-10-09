package ru.givler.mbo.registry;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.oredict.OreDictionary;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.block.DoorBase;
import ru.givler.mbo.item.*;
import ru.givler.mbo.item.weapon.ItemEffectArrow;
import ru.givler.mbo.recipes.RecipeEffectArrows;
import ru.givler.mbo.integration.minefantasy2.UndeadMineFantasy;
import ru.givler.mbo.item.amulets.*;
import ru.givler.mbo.item.belt.ItemFallBelt;
import ru.givler.mbo.item.belt.ItemFertilityBelt;
import ru.givler.mbo.item.belt.ItemMinerBelt;
import ru.givler.mbo.item.fauna.ItemCreatureBucket;
import ru.givler.mbo.entity.fauna.EntityMBOAxolotl;
import ru.givler.mbo.entity.fauna.EntityMBOFish;
import ru.givler.mbo.item.fauna.ItemGoatHorn;
import ru.givler.mbo.item.fauna.ItemPowderSnowBucket;
import ru.givler.mbo.item.glyph.*;
import ru.givler.mbo.item.glyph.ItemGlyphWeapon;
import ru.givler.mbo.item.magic.ItemSpectralWeapon;
import ru.givler.mbo.item.ring.*;
import ru.givler.mbo.item.wand.ItemWandWizard;
import ru.givler.mbo.item.weapon.*;
import ru.givler.mbo.magic.item.SpectralEffects;


public class ItemRegistry {
    public static Item effectArrow;
    public static Item seagrass, turtleScute, powderSnowBucket, goatHorn;
    public static Item axolotlBucket,prismarineShard,prismarineCrystal;
    public static final Item[] fishBuckets = new Item[5];
    private static final String[] FISH = {"cod", "salmon", "tropical_fish", "pufferfish", "tadpole"};

    private static void registerEffectArrows() {
        effectArrow = Loader.isModLoaded("minefantasy2") ? UndeadMineFantasy.createArrow() : new ItemEffectArrow();
        GameRegistry.registerItem(effectArrow, "effect_arrow");
        ItemEffectArrow.registerDispenser(effectArrow);
    }

    private static void registerFaunaItems() {
        axolotlBucket = new ItemCreatureBucket<EntityMBOAxolotl>("mbo.axolotl_bucket", "fauna/axolotl_bucket",
                EntityMBOAxolotl::new, .1F, "mbo:entity.axolotl.splash");
        GameRegistry.registerItem(axolotlBucket, "axolotl_bucket");
        prismarineShard=new ItemBase("mbo.prismarine_shard","fauna/prismarine_shard",64,false);GameRegistry.registerItem(prismarineShard,"prismarine_shard");
        prismarineCrystal=new ItemBase("mbo.prismarine_crystals","fauna/prismarine_crystals",64,false);GameRegistry.registerItem(prismarineCrystal,"prismarine_crystals");
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.BlockPrismarine,1,0),"SS","SS",'S',prismarineShard);
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.BlockPrismarine,1,1),"SSS","SSS","SSS",'S',prismarineShard);
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.BlockPrismarine,1,2),"SSS","SIS","SSS",'S',prismarineShard,'I',new ItemStack(Items.dye,1,0));
        GameRegistry.addRecipe(new ItemStack(BlockRegistry.BlockSeaLantern),"SCS","CCC","SCS",'S',prismarineShard,'C',prismarineCrystal);
        goatHorn = new ItemGoatHorn();
        GameRegistry.registerItem(goatHorn, "goat_horn");
        seagrass = Item.getItemFromBlock(BlockRegistry.seagrassBlock);
        turtleScute = new ItemBase("mbo.turtle_scute", "fauna/turtle_scute", 64, false);
        GameRegistry.registerItem(turtleScute, "turtle_scute");
        powderSnowBucket = new ItemPowderSnowBucket();
        GameRegistry.registerItem(powderSnowBucket, "powder_snow_bucket");
        for (int i = 0; i < FISH.length; i++) {
            final int type = i;
            fishBuckets[i] = new ItemCreatureBucket<EntityMBOFish>("mbo." + FISH[i] + "_bucket",
                    "fauna/" + FISH[i] + "_bucket", world -> EntityMobRegistry.createFish(world, type),
                    .25F, "game.neutral.swim");
            GameRegistry.registerItem(fishBuckets[i], FISH[i] + "_bucket");
        }
    }

    //РїРµСЂРµРјРµРЅРЅС‹Рµ  РїСЂРµРґРјРµС‚РѕРІ С‚РѕС‚РµРјРѕРІ
    public static Item GlyphAmphibian, GlyphDragon, GlyphHawk, GlyphMiner, GlyphOwl, GlyphWeapon, GlyphCleansing, GlyphHealing, BrokenStaffHealing;
    // РїРµСЂРµРјРµРЅРЅС‹Рµ РѕСЂСѓР¶РёСЏ Р±Р»РёР¶РЅРµРіРѕ Р±РѕСЏ
    public static ItemWeaponBase BrokenLongsword, BrokenSword, BrokenRapier, BrokenMace, BrokenAxe, BrokenDagger, BrokenCudgel, Uchigatana,
            DragonSlayer, TorchMat;
    // РїРµСЂРµРјРµРЅРЅС‹Рµ РїСЂРёР·СЂР°С‡РЅРѕРіРѕ РѕСЂСѓР¶РёСЏ
    public static ItemSpectralWeapon WeaponRapier;
    // РїРµСЂРµРјРµРЅРЅС‹Рµ Р»СѓРєРѕРІ
    public static ItemBow BrokenBowHunting ;
    // РїРµСЂРµРјРµРЅРЅС‹Рµ РјР°С‚РµСЂРёР°Р»РѕРІ
    public static Item Metal, SapphireHeart, SapphireEye, Crystall, GlyphVoid, Drop, TooltipDemo;
    public static Item CopperIngot, Honeycomb;
    public static Item RabbitHide, RabbitFoot;
    public static DoorItemBase[] CopperDoorItems;
    // РїРµСЂРµРјРµРЅРЅС‹Рµ Р°РјСѓР»РµС‚РѕРІ
    public static Item HealingAmulet, VampirismAmulet, CleansingAmulet, PhoenixAmulet, CowardAmulet, DragonAmulet, StaminaAmulet, VeilAmulet,
        ThornsAmulet, StrengthAmulet, MercenaryAmulet, GoblinAmulet, GoldBasicAmulet, SilverBasicAmulet;
    // РїРµСЂРµРјРµРЅРЅС‹Рµ РєРѕР»РµС†
    public static Item LifeRing, StaminaRing, DamageRing, SpeedRing, LifeSmallRing, StaminaSmallRing, DamageSmallRing, SpeedSmallRing,
        SmallBasicRing, BasicRing, MushroomRing, StrengthAttributeRing, DexterityAttributeRing,
        EnduranceAttributeRing, SpiritAttributeRing;
    // РїРµСЂРµРјРµРЅРЅС‹Рµ РїРѕСЏСЃР°
    public static Item FertilityBelt, FallBelt, MinerBelt, WaterminerBelt, KnightBelt;
    //РјР°РіРёС‡РµСЃРєРёРµ РїРѕСЃРѕС…Рё
    public static ItemWandBase BrokenWandWizard, BrokenWandPyromancer;
    // РџСЂРёР·СЂР°С‡РЅРѕРіРѕ РѕСЂСѓР¶РёСЏ
    public static ItemSpectralWeapon TorchWeapon;
    public static Item Lockpick, AdminKey, AdminLighter, PlatformEditor, DungeonEditor, LockableDoorItem;

    @Mod.EventHandler
    public static void preLoad(FMLPreInitializationEvent event) {
        registerEffectArrows();
        registerFaunaItems();
        CopperIngot = new ItemBase("CopperIngot", "copper/copper_ingot", 64);
        OreDictionary.registerOre("ingotCopper", new ItemStack(CopperIngot));
        Honeycomb = new ItemBase("Honeycomb", "copper/honeycomb", 64);
        for (String waxName : new String[] {"materialWax", "materialWaxcomb",
                "materialHoneycomb", "itemBeeswax"}) {
            OreDictionary.registerOre(waxName, new ItemStack(Honeycomb));
        }
        RabbitHide = new ItemBase("RabbitHide", "rabbit/rabbit_hide", 64);
        RabbitFoot = new ItemBase("RabbitFoot", "rabbit/rabbit_foot", 64);
        RabbitFoot.setPotionEffect("+0+1-2+3&4-4+13");
        CopperDoorItems = new DoorItemBase[12];
        String[] copperDoorTextures = {"copper_door", "exposed_copper_door",
                "weathered_copper_door", "oxidized_copper_door"};
        for (int stage = 0; stage < 4; stage++) for (int wax = 0; wax <= 8; wax += 8) {
            int state = stage + wax;
            DoorBase block = BlockRegistry.CopperDoors[state];
            CopperDoorItems[state] = new DoorItemBase(block, "CopperDoorItem" + state,
                    "mbo:copper/" + copperDoorTextures[stage] + "_item");
            CopperDoorItems[state].setMaxStackSize(64);
            block.setDropItem(CopperDoorItems[state]);
        }
        Lockpick = new ItemLockpick();
        AdminKey = new ItemAdminKey();
        AdminLighter = new ItemAdminLighter();
        PlatformEditor = new ItemPlatformEditor();
        DungeonEditor = new ItemDungeonEditor();
        GameRegistry.registerItem(Lockpick, "Lockpick");
        GameRegistry.registerItem(AdminKey, "AdminKey");
        GameRegistry.registerItem(AdminLighter, "AdminLighter");
        GameRegistry.registerItem(PlatformEditor, "PlatformEditor");
        GameRegistry.registerItem(DungeonEditor, "DungeonEditor");
        LockableDoorItem = new DoorItemBase(BlockRegistry.LockableDoor, "LockableDoorItem", "minecraft:door_wood");
        BlockRegistry.LockableDoor.setDropItem(LockableDoorItem);

        // РњР°С‚РµСЂРёР°Р»С‹ Рё РѕСЂСѓР¶РёРµ
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
        WeaponRapier = createSpectralWeapon("WeaponRapier", "mithrilsword", BrokenSwordMat, 800);
        Uchigatana = new ItemSwordMBO("Uchigatana", "uchigatana", Divine, 10000, 1);
        DragonSlayer = new ItemDragonSlayerMBO("DragonSlayer", "dragon_slayer", DragonSlayerMat, 1750, 1);

        TorchWeapon = createSpectralWeapon("TorchWeapon", "torch", TorchMat, 800);
        TorchWeapon.setImpactEffect(SpectralEffects.IGNITE, 0.75F)
                .setEnchantedAppearance(false)
                .setDescription("item.TorchWeapon.desc", EnumChatFormatting.RED);

        //РіР»РёС„С‹
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

        //РјР°С‚РµСЂРёР»Р°С‹
        Metal = new ItemMeta("Metal", "material/metal", 64, 1);
        Drop = new ItemMeta("Drop", "material/drop", 64, 3);
        SapphireHeart = new ItemBase("SapphireHeart", "material/sapphire_heart", 64);
        SapphireEye = new ItemBase("SapphireEye", "material/sapphire_eye", 64);
        Crystall = new ItemMeta("Crystall", "material/crystall", 64, 16);
        GlyphVoid = new ItemBase("GlyphVoid", "glyph/glyph_void", 1);
        TooltipDemo = new ItemTooltipDemo();

        //Р±РёР¶СЋС‚РµСЂРёСЏ
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

    private static ItemSpectralWeapon createSpectralWeapon(String name, String texture,
                                                            Item.ToolMaterial material, int duration) {
        ItemSpectralWeapon item = new ItemSpectralWeapon(material, duration);
        item.setUnlocalizedName(name);
        item.setTextureName(MoreBeyondOrdinary.MODID + ":weapon/" + texture);
        item.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        item.setMaxDamage(duration);
        item.setMaxStackSize(1);
        GameRegistry.registerItem(item, name);
        return item;
    }

    // These classes refer to optional mods. Reflection prevents the JVM from loading them
    // when the corresponding mod is absent.
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
