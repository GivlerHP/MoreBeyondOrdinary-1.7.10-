package ru.givler.mbo.decor;

import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import ru.givler.mbo.core.ArmorSkinTransformer;
import ru.givler.mbo.item.skin.EquipmentSkinData;
import ru.givler.mbo.item.skin.EquipmentSkinDefinition;
import ru.givler.mbo.registry.EquipmentSkinRegistry;
import software.bernie.geckolib3.file.GeoModelLoader;
import software.bernie.geckolib3.geo.render.built.GeoModel;

public final class EquipmentSkinSmoke {
    public static void main(String[] args) throws Exception {
        StaticDecorPixelsSmoke.fakeMinecraft();
        checkModel(EquipmentSkinRegistry.MACE_BASE, "mace", 7, 30, 64);
        checkModel(EquipmentSkinRegistry.HAMMER_BASE, "weapon", 6, 36, 32);
        checkModel(EquipmentSkinRegistry.AXE_BASE, "weapon", 5, 30, 32);
        checkModel(EquipmentSkinRegistry.HAMMER, "hammer", 4, 24, 32);
        checkModel(EquipmentSkinRegistry.DRAGONSLAYER, "weapon", 9, 49, 32);
        checkNbt(EquipmentSkinRegistry.MACE_BASE);
        checkArmorModel();
        checkArmorHooks();
        System.out.println("Weapon and armor skin geometry, slots, Forge hooks and NBT verified.");
    }

    private static void checkArmorHooks() throws Exception {
        String target = "net.minecraftforge.client.ForgeHooksClient";
        byte[] original;
        try (InputStream stream = EquipmentSkinSmoke.class.getResourceAsStream("/" + target.replace('.', '/') + ".class")) {
            original = org.apache.commons.io.IOUtils.toByteArray(stream);
        }
        byte[] patched = new ArmorSkinTransformer().transform(target, target, original);
        check(patched != original, "Armor renderer hooks were not installed");
        ClassNode node = new ClassNode();
        new ClassReader(patched).accept(node, 0);
        for (MethodNode method : node.methods) {
            if (method.name.equals("getArmorModel") || method.name.equals("getArmorTexture")) {
                new Analyzer<>(new BasicVerifier()).analyze(node.name, method);
            }
        }
    }

    private static void checkArmorModel() throws Exception {
        EquipmentSkinDefinition skin = EquipmentSkinRegistry.MAGICIAN_BASE;
        GeoModel model = GeoModelLoader.getInstance().loadModel(
                net.minecraft.client.Minecraft.getMinecraft().getResourceManager(), skin.model);
        for (String bone : new String[]{"head", "body", "deco", "right_arm", "left_arm", "right_leg", "left_leg"}) {
            check(model.getBone(bone).isPresent() && !model.getBone(bone).get().childCubes.isEmpty(),
                    "Missing robe geometry: " + bone);
        }
        for (String panel : new String[]{"robe_front", "robe_back"}) {
            check(model.getBone(panel).isPresent() && model.getBone(panel).get().childCubes.size() == 4,
                    "Robe cloth must be rendered as a shared panel: " + panel);
        }
        check(skin.supportsArmorSlot(0) && skin.supportsArmorSlot(1) && skin.supportsArmorSlot(2)
                && !skin.supportsArmorSlot(3), "Robe supports the wrong armor slots");
        java.awt.image.BufferedImage image = ImageIO.read(
                new File("src/main/resources/assets/mbo", skin.texture.getResourcePath()));
        check(image.getWidth() == 128 && image.getHeight() == 128, "Wrong robe texture size");
    }

    private static void checkModel(EquipmentSkinDefinition skin, String root, int cubes, int expectedFaces, int size)
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

    private static void checkNbt(EquipmentSkinDefinition skin) throws Exception {
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
        check(EquipmentSkinData.resolve(mace) == skin, "Legacy mace skin no longer resolves");
        check(!EquipmentSkinData.set(new ItemStack(new Item()), skin.id), "Skin applied to incompatible item");
        check(!EquipmentSkinData.set(mace, "unknown"), "Unknown skin accepted");
        check(EquipmentSkinData.set(mace, skin.id) && EquipmentSkinData.resolve(mace) == skin, "Skin not applied");
        check(EquipmentSkinData.set(mace, "") && EquipmentSkinData.resolve(mace) == null, "Skin not removed");
        check("preserve".equals(mace.getTagCompound().getString("OtherData")), "Other NBT changed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
