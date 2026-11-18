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
        WeaponSkinDefinition skin = WeaponSkinRegistry.MACE_TEST;
        StaticDecorPixelsSmoke.fakeMinecraft();
        File assets = new File("src/main/resources/assets/mbo");
        GeoModel model = GeoModelLoader.getInstance().loadModel(
                net.minecraft.client.Minecraft.getMinecraft().getResourceManager(), skin.model);
        check(model.getBone("grip").isPresent(), "Missing grip");
        check(model.getBone("mace").get().childCubes.size() == 7, "Missing geometry");
        int faces = 0;
        for (software.bernie.geckolib3.geo.render.built.GeoCube cube : model.getBone("mace").get().childCubes) {
            for (software.bernie.geckolib3.geo.render.built.GeoQuad quad : cube.quads) {
                if (quad == null) continue;
                check(quad.normal != null && quad.vertices != null, "Undrawable face");
                faces++;
            }
        }
        check(faces == 30, "Expected 24 solid faces and six faces on the three vanes");
        java.awt.image.BufferedImage image = ImageIO.read(new File(assets, skin.texture.getResourcePath()));
        check(image.getWidth() == 64 && image.getHeight() == 64, "Wrong texture resolution");

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
        check(!WeaponSkinData.set(new ItemStack(new Item()), skin.id), "Skin applied to incompatible item");
        check(!WeaponSkinData.set(mace, "unknown"), "Unknown skin accepted");
        check(WeaponSkinData.set(mace, skin.id) && WeaponSkinData.resolve(mace) == skin, "Skin not applied");
        check(WeaponSkinData.set(mace, "") && WeaponSkinData.resolve(mace) == null, "Skin not removed");
        check("preserve".equals(mace.getTagCompound().getString("OtherData")), "Other NBT changed");
        System.out.println("Weapon skin model, UV faces, grip, compatibility and NBT verified.");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
