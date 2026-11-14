package ru.givler.mbo.integration.thaumcraft;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import net.minecraft.item.Item;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.*;

public final class ThaumcraftAspectsSmoke {
    static void check(boolean ok, String text) { if (!ok) throw new AssertionError(text); }
    public static void main(String[] args) throws Exception {
        java.lang.reflect.Field uf = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        uf.setAccessible(true); sun.misc.Unsafe unsafe = (sun.misc.Unsafe) uf.get(null);
        java.lang.reflect.Field instance = cpw.mods.fml.common.Loader.class.getDeclaredField("instance");
        instance.setAccessible(true); instance.set(null, unsafe.allocateInstance(cpw.mods.fml.common.Loader.class));
        java.lang.reflect.Field mods = cpw.mods.fml.common.Loader.class.getDeclaredField("namedMods");
        mods.setAccessible(true); mods.set(instance.get(null), new java.util.HashMap<>());
        net.minecraft.init.Bootstrap.func_151354_b();

        List<String[]> expected = new ArrayList<>();
        BufferedReader input = new BufferedReader(new InputStreamReader(ThaumcraftAspectsSmoke.class.getResourceAsStream("/thaumcraft-aspects-migration.tsv"), "UTF-8"));
        for (String line; (line = input.readLine()) != null;) expected.add(line.split("\t"));
        Map<String, Item> items = new HashMap<>();
        for (String[] row : expected) {
            if (items.containsKey(row[0])) continue;
            int split = row[0].indexOf('.');
            Class<?> registry = Class.forName("ru.givler.mbo.registry." + row[0].substring(0, split));
            Item item = new Item(); registry.getField(row[0].substring(split + 1)).set(null, item);
            items.put(row[0], item);
        }
        java.util.Set<String> creatureKeys = new java.util.HashSet<>();
        net.minecraft.entity.EntityList.classToStringMapping.put(ru.givler.mbo.entity.monster.EntityStoneGolem.class, "test.EntityStoneGolem");
        creatureKeys.add("test.EntityStoneGolem");
        net.minecraft.entity.EntityList.classToStringMapping.put(ru.givler.mbo.entity.fauna.EntityRabbit.class, "test.EntityRabbit");
        creatureKeys.add("test.EntityRabbit");
        net.minecraft.entity.EntityList.classToStringMapping.put(ru.givler.mbo.entity.fauna.EntityMBOCod.class, "test.EntityMBOCod");
        creatureKeys.add("test.EntityMBOCod");
        net.minecraft.entity.EntityList.classToStringMapping.put(ru.givler.mbo.entity.fauna.EntityMBOSalmon.class, "test.EntityMBOSalmon");
        creatureKeys.add("test.EntityMBOSalmon");
        net.minecraft.entity.EntityList.classToStringMapping.put(ru.givler.mbo.entity.fauna.EntityMBOTropicalFish.class, "test.EntityMBOTropicalFish");
        creatureKeys.add("test.EntityMBOTropicalFish");
        net.minecraft.entity.EntityList.classToStringMapping.put(ru.givler.mbo.entity.fauna.EntityMBOPufferfish.class, "test.EntityMBOPufferfish");
        creatureKeys.add("test.EntityMBOPufferfish");
        net.minecraft.entity.EntityList.classToStringMapping.put(ru.givler.mbo.entity.fauna.EntityMBOPolarBear.class, "test.EntityMBOPolarBear");
        creatureKeys.add("test.EntityMBOPolarBear");
        net.minecraft.entity.EntityList.classToStringMapping.put(ru.givler.mbo.entity.fauna.EntityMBOTurtle.class, "test.EntityMBOTurtle");
        creatureKeys.add("test.EntityMBOTurtle");
        net.minecraft.entity.EntityList.classToStringMapping.put(ru.givler.mbo.entity.fauna.EntityMBOFrog.class, "test.EntityMBOFrog");
        creatureKeys.add("test.EntityMBOFrog");
        net.minecraft.entity.EntityList.classToStringMapping.put(ru.givler.mbo.entity.fauna.EntityMBOTadpole.class, "test.EntityMBOTadpole");
        creatureKeys.add("test.EntityMBOTadpole");
        net.minecraft.entity.EntityList.classToStringMapping.put(ru.givler.mbo.entity.fauna.EntityMBOGoat.class, "test.EntityMBOGoat");
        creatureKeys.add("test.EntityMBOGoat");
        net.minecraft.entity.EntityList.classToStringMapping.put(ru.givler.mbo.entity.fauna.EntityMBOPanda.class, "test.EntityMBOPanda");
        creatureKeys.add("test.EntityMBOPanda");
        net.minecraft.entity.EntityList.classToStringMapping.put(ru.givler.mbo.entity.fauna.EntityMBOFox.class, "test.EntityMBOFox");
        creatureKeys.add("test.EntityMBOFox");
        net.minecraft.entity.EntityList.classToStringMapping.put(ru.givler.mbo.entity.fauna.EntityMBOAxolotl.class, "test.EntityMBOAxolotl");
        creatureKeys.add("test.EntityMBOAxolotl");
        net.minecraft.entity.EntityList.classToStringMapping.put(ru.givler.mbo.entity.fauna.EntityMBOGlowSquid.class, "test.EntityMBOGlowSquid");
        creatureKeys.add("test.EntityMBOGlowSquid");
        net.minecraft.entity.EntityList.classToStringMapping.put(ru.givler.mbo.entity.fauna.EntityMBOGuardian.class, "test.EntityMBOGuardian");
        creatureKeys.add("test.EntityMBOGuardian");
        net.minecraft.entity.EntityList.classToStringMapping.put(ru.givler.mbo.entity.fauna.EntityMBOElderGuardian.class, "test.EntityMBOElderGuardian");
        creatureKeys.add("test.EntityMBOElderGuardian");
        net.minecraft.entity.EntityList.classToStringMapping.put(ru.givler.mbo.entity.fauna.EntityMBOCamel.class, "test.EntityMBOCamel");
        creatureKeys.add("test.EntityMBOCamel");
        net.minecraft.entity.EntityList.classToStringMapping.put(ru.givler.mbo.entity.fauna.EntityMBOCamelHusk.class, "test.EntityMBOCamelHusk");
        creatureKeys.add("test.EntityMBOCamelHusk");
        net.minecraft.entity.EntityList.classToStringMapping.put(ru.givler.mbo.entity.fauna.EntityMBOZombieHorse.class, "test.EntityMBOZombieHorse");
        creatureKeys.add("test.EntityMBOZombieHorse");
        net.minecraft.entity.EntityList.classToStringMapping.put(ru.givler.mbo.entity.fauna.EntityMBOSkeletonHorse.class, "test.EntityMBOSkeletonHorse");
        creatureKeys.add("test.EntityMBOSkeletonHorse");
        net.minecraft.entity.EntityList.classToStringMapping.put(ru.givler.mbo.entity.monster.EntityMBOHusk.class, "test.EntityMBOHusk");
        creatureKeys.add("test.EntityMBOHusk");
        net.minecraft.entity.EntityList.classToStringMapping.put(ru.givler.mbo.entity.monster.EntityMBOParched.class, "test.EntityMBOParched");
        creatureKeys.add("test.EntityMBOParched");
        net.minecraft.entity.EntityList.classToStringMapping.put(ru.givler.mbo.entity.monster.EntityMBOStray.class, "test.EntityMBOStray");
        creatureKeys.add("test.EntityMBOStray");
        net.minecraft.entity.EntityList.classToStringMapping.put(ru.givler.mbo.entity.monster.EntityMBOBogged.class, "test.EntityMBOBogged");
        creatureKeys.add("test.EntityMBOBogged");
        int entityTagsBefore = ThaumcraftApi.scanEntities.size();
        ThaumcraftAspects.register();
        for (String[] row : expected) {
            AspectList found = ThaumcraftApi.objectTags.get(Arrays.asList(items.get(row[0]), Integer.parseInt(row[1])));
            check(found != null, "Missing migrated entry: " + row[0]);
            int count = 0;
            for (String pair : row[2].split(",")) {
                String[] parts = pair.trim().split(" "); Aspect aspect = Aspect.getAspect(parts[0]);
                check(aspect != null && found.getAmount(aspect) == Integer.parseInt(parts[1]), "Changed aspect: " + row[0] + " " + pair); count++;
            }
            check(found.size() == count, "Unexpected extra aspect: " + row[0]);
        }
        ThaumcraftAspects.register();
        check(ThaumcraftApi.objectTags.get(Arrays.asList(items.get("ItemRegistry.Crystall"), 0)).getAmount(Aspect.LIFE) == 4, "Repeated registration accumulated amounts");
        check(ThaumcraftApi.objectTags.get(Arrays.asList(items.get("FoodRegistry.FoodBacon"),32767)).getAmount(Aspect.FLESH)==4,"Cyrillic corpus migration");
        check(ThaumcraftApi.objectTags.get(Arrays.asList(items.get("DrinkRegistry.DrinkWine"),32767)).getAmount(Aspect.PLANT)==1,"herba migration");
        java.util.Set<String> tagged = new java.util.HashSet<>();
        for (int i = entityTagsBefore; i < ThaumcraftApi.scanEntities.size(); i++) {
            thaumcraft.api.ThaumcraftApi.EntityTags tag = ThaumcraftApi.scanEntities.get(i);
            check(creatureKeys.contains(tag.entityName), "Technical entity received aspects: " + tag.entityName);
            check(tagged.add(tag.entityName), "Duplicate creature aspects: " + tag.entityName);
            if (tag.entityName.endsWith("EntityMBOGlowSquid")) check(tag.aspects.getAmount(Aspect.LIGHT)==2,"Glowing squid light");
            if (tag.entityName.endsWith("EntityMBOPufferfish")) check(tag.aspects.getAmount(Aspect.POISON)==2,"Pufferfish poison");
            if (tag.entityName.endsWith("EntityMBOStray")) check(tag.aspects.getAmount(Aspect.COLD)==2,"Stray cold");
        }
        check(tagged.equals(creatureKeys), "Missing creature aspects");
        System.out.println("PASS 78 migrated item tags; 25 creatures, real registry keys, no technical entities or duplicate registrations");
    }
}
