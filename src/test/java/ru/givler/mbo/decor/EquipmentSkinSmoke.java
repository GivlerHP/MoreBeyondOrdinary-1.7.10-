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
import ru.givler.mbo.item.skin.ItemSkinTemplate;
import ru.givler.mbo.item.skin.WardrobeData;
import ru.givler.mbo.item.skin.WardrobeSlot;
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
        checkWardrobe();
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
        target = "net.minecraft.client.renderer.ItemRenderer";
        try (InputStream stream = EquipmentSkinSmoke.class.getResourceAsStream("/" + target.replace('.', '/') + ".class")) {
            original = org.apache.commons.io.IOUtils.toByteArray(stream);
        }
        patched = new ArmorSkinTransformer().transform(target, target, original);
        check(patched != original, "Held-item wardrobe hook was not installed");
        node = new ClassNode();
        new ClassReader(patched).accept(node, 0);
        for (MethodNode method : node.methods) {
            if (method.name.equals("renderItem")) new Analyzer<>(new BasicVerifier()).analyze(node.name, method);
        }
    }

    private static void checkWardrobe() throws Exception {
        java.lang.reflect.Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) field.get(null);
        net.minecraft.entity.player.EntityPlayer wearer = (net.minecraft.entity.player.EntityPlayer)
            unsafe.allocateInstance(net.minecraft.client.entity.EntityOtherPlayerMP.class);
        net.minecraft.entity.player.EntityPlayer observer = (net.minecraft.entity.player.EntityPlayer)
            unsafe.allocateInstance(net.minecraft.client.entity.EntityOtherPlayerMP.class);
        ItemStack template = new ItemStack(ItemSkinTemplate.INSTANCE);
        check(ItemSkinTemplate.set(template, EquipmentSkinRegistry.MACE_BASE.id, -1)
                && ItemSkinTemplate.slot(template) == WardrobeSlot.MACE, "Workbench did not write a weapon template");
        ItemStack hammerTemplate = new ItemStack(ItemSkinTemplate.INSTANCE);
        check(ItemSkinTemplate.set(hammerTemplate, EquipmentSkinRegistry.HAMMER.id, -1), "Could not make the hammer template");
        NBTTagCompound settings = WardrobeData.inventory(wearer);
        check(!WardrobeData.isDisabled(settings, WardrobeSlot.WARHAMMER, hammerTemplate), "Skin must be active by default");
        WardrobeData.setDisabled(settings, WardrobeSlot.WARHAMMER, hammerTemplate, true);
        NBTTagCompound reloaded = (NBTTagCompound) settings.copy();
        check(WardrobeData.isDisabled(reloaded, WardrobeSlot.WARHAMMER, hammerTemplate)
                && !WardrobeData.isDisabled(reloaded, WardrobeSlot.MACE, template),
            "Disabled skin must survive reload without changing other slots");
        check(ItemSkinTemplate.set(hammerTemplate, EquipmentSkinRegistry.HAMMER_BASE.id, -1)
                && !WardrobeData.isDisabled(reloaded, WardrobeSlot.WARHAMMER, hammerTemplate),
            "A different skin in the same slot must start enabled");
        check(ItemSkinTemplate.set(hammerTemplate, EquipmentSkinRegistry.HAMMER.id, -1), "Could not restore the hammer template");
        WardrobeData.setDisabled(reloaded, WardrobeSlot.WARHAMMER, hammerTemplate, false);
        check(!WardrobeData.isDisabled(reloaded, WardrobeSlot.WARHAMMER, hammerTemplate), "Could not re-enable the skin");
        check(ItemSkinTemplate.set(template, EquipmentSkinRegistry.MAGICIAN_BASE.id, 2)
                && ItemSkinTemplate.slot(template) == WardrobeSlot.MAGIC_LEGS, "Workbench lost the selected armor part");
        check(!ItemSkinTemplate.set(template, EquipmentSkinRegistry.MAGICIAN_BASE.id, 3)
                && ItemSkinTemplate.slot(template) == WardrobeSlot.MAGIC_LEGS, "Unsupported armor part changed the template");
        check(ItemSkinTemplate.set(template, "", -1) && ItemSkinTemplate.skin(template) == null,
            "Workbench could not clear a template");
        template.setTagCompound(new NBTTagCompound());
        template.getTagCompound().setString("Skin", EquipmentSkinRegistry.MAGICIAN_BASE.id);
        template.getTagCompound().setString("Slot", "magic_head");
        check(ItemSkinTemplate.slot(template) == WardrobeSlot.MAGIC_HEAD
                && !WardrobeSlot.MAGIC_FEET.accepts(EquipmentSkinRegistry.MAGICIAN_BASE),
            "Armor template must target a supported part");
        template.getTagCompound().setString("Slot", "light_head");
        check(ItemSkinTemplate.slot(template) == null, "Magic template was accepted in a light armor slot");
        template.getTagCompound().removeTag("Slot");
        template.getTagCompound().setString("Skin", "hammer");
        check(ItemSkinTemplate.slot(template) == WardrobeSlot.WARHAMMER, "Warhammer template target was not inferred");
        template.getTagCompound().setString("Slot", "mace");
        check(ItemSkinTemplate.slot(template) == null, "Warhammer template was accepted as a mace skin");

        EquipmentSkinDefinition skin = EquipmentSkinRegistry.HAMMER, original = EquipmentSkinRegistry.HAMMER_BASE;
        Item hammer = (Item) unsafe.allocateInstance(skin.weaponType);
        java.lang.reflect.Field delegate = Item.class.getDeclaredField("delegate");
        unsafe.putObject(hammer, unsafe.objectFieldOffset(delegate),
            new cpw.mods.fml.common.registry.RegistryDelegate.Delegate<Item>(hammer, Item.class));
        ItemStack weapon = new ItemStack(hammer);
        NBTTagCompound payload = new NBTTagCompound();
        payload.setString("warhammer", skin.id);
        WardrobeData.acceptVisual(wearer, payload);
        check(WardrobeData.resolve(weapon, wearer) == skin && WardrobeData.resolve(weapon, observer) == null,
            "The wardrobe skin must belong to the wearer only");
        ItemStack rendered = (ItemStack) WardrobeData.heldStack(wearer, weapon);
        check(rendered != weapon && EquipmentSkinData.resolve(rendered) == skin && !weapon.hasTagCompound(),
            "Applying a wardrobe skin must leave the original stack unchanged");
        EquipmentSkinData.set(weapon, original.id);
        check(WardrobeData.resolve(weapon, wearer) == original && WardrobeData.heldStack(wearer, weapon) == weapon,
            "A table skin must take priority over the wardrobe");
        WardrobeData.inventory(wearer).setString("probe", "persistent");
        check("persistent".equals(wearer.getEntityData().getCompoundTag(net.minecraft.entity.player.EntityPlayer.PERSISTED_NBT_TAG)
            .getCompoundTag("MBO_Wardrobe").getString("probe")), "Wardrobe is missing from death-persistent player data");
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
