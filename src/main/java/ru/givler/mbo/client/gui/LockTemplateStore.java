package ru.givler.mbo.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.io.File;
import java.io.IOException;

@SideOnly(Side.CLIENT)
final class LockTemplateStore {
    private LockTemplateStore() {}

    private static File file() {
        return new File(new File(Minecraft.getMinecraft().mcDataDir, "config/MoreBeyondOrdinary"), "lock_templates.dat");
    }

    static NBTTagList load() {
        File file = file();
        if (!file.isFile()) return new NBTTagList();
        try {
            NBTTagCompound root = CompressedStreamTools.read(file);
            return root == null ? new NBTTagList() : root.getTagList("Templates", 10);
        } catch (IOException ignored) { return new NBTTagList(); }
    }

    static void save(NBTTagList templates) {
        File file = file(); File parent = file.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) return;
        NBTTagCompound root = new NBTTagCompound(); root.setTag("Templates", templates);
        try { CompressedStreamTools.safeWrite(root, file); } catch (IOException ignored) {}
    }
}
