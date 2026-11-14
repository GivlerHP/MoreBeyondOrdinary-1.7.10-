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
        System.out.println("PASS all 78 migrated entries, exact metadata, corrected aspect names and repeat registration");
    }
}
