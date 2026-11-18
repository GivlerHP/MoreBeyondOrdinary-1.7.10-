package ru.givler.mbo.decor;

import java.io.File;
import javax.imageio.ImageIO;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import ru.givler.mbo.item.skin.WeaponSkinData;
import ru.givler.mbo.item.skin.WeaponSkinDefinition;
import ru.givler.mbo.registry.WeaponSkinRegistry;
import software.bernie.geckolib3.file.GeoModelLoader;
import software.bernie.geckolib3.geo.render.built.GeoModel;

public final class WeaponSkinSmoke {
    public static void main(String[] args) throws Exception {
        StaticDecorPixelsSmoke.fakeMinecraft();
        checkModel(WeaponSkinRegistry.MACE_BASE, "mace", 7, 30, 64);
        checkModel(WeaponSkinRegistry.HAMMER_BASE, "weapon", 6, 36, 32);
        checkModel(WeaponSkinRegistry.AXE_BASE, "weapon", 5, 30, 32);
        checkModel(WeaponSkinRegistry.HAMMER, "hammer", 4, 24, 32);
        checkModel(WeaponSkinRegistry.DRAGONSLAYER, "weapon", 9, 49, 32);
        checkNbt(WeaponSkinRegistry.MACE_BASE);
        System.out.println("Weapon skin models, UV faces, grips, compatibility and NBT verified.");
    }

    private static void checkModel(WeaponSkinDefinition skin, String root, int cubes, int expectedFaces, int size)
            throws Exception {
        File assets = new File("src/main/resources/assets/mbo");
        GeoModel model = GeoModelLoader.getInstance().loadModel(
                net.minecraft.client.Minecraft.getMinecraft().getResourceManager(), skin.model);
        check(model.getBone("grip").isPresent(), "Missing grip");
        check(model.getBone(root).get().childCubes.size() == cubes, "Missing geometry: " + skin.id);
        int faces = 0;
        for (software.bernie.geckolib3.geo.render.built.GeoCube cube : model.getBone(root).get().childCubes) {
            for (software.bernie.geckolib3.geo.render.built.GeoQuad quad : cube.quads) {
                if (quad == null) continue;
                check(quad.normal != null && quad.vertices != null, "Undrawable face");
                faces++;
            }
        }
        check(faces == expectedFaces, "Missing textured faces: " + skin.id);
        java.awt.image.BufferedImage image = ImageIO.read(new File(assets, skin.texture.getResourcePath()));
        check(image.getWidth() == size && image.getHeight() == size, "Wrong texture resolution: " + skin.id);
    }

    private static void checkNbt(WeaponSkinDefinition skin) throws Exception {
        sun.misc.Unsafe unsafe;
        java.lang.reflect.Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        unsafe = (sun.misc.Unsafe) field.get(null);
        Item maceItem = (Item) unsafe.allocateInstance(skin.weaponType);
        java.lang.reflect.Field delegate = Item.class.getDeclaredField("delegate");
        unsafe.putObject(maceItem, unsafe.objectFieldOffset(delegate),
                new cpw.mods.fml.common.registry.RegistryDelegate.Delegate<Item>(maceItem, Item.class));
        ItemStack mace = new ItemStack(maceItem);
        mace.setTagCompound(new NBTTagCompound());
        mace.getTagCompound().setString("OtherData", "preserve");
        mace.getTagCompound().setString("MBO_WeaponSkin", "mace_test");
        check(WeaponSkinData.resolve(mace) == skin, "Legacy mace skin no longer resolves");
        check(!WeaponSkinData.set(new ItemStack(new Item()), skin.id), "Skin applied to incompatible item");
        check(!WeaponSkinData.set(mace, "unknown"), "Unknown skin accepted");
        check(WeaponSkinData.set(mace, skin.id) && WeaponSkinData.resolve(mace) == skin, "Skin not applied");
        check(WeaponSkinData.set(mace, "") && WeaponSkinData.resolve(mace) == null, "Skin not removed");
        check("preserve".equals(mace.getTagCompound().getString("OtherData")), "Other NBT changed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
