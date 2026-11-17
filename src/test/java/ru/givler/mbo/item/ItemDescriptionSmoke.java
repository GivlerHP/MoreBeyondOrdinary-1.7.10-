package ru.givler.mbo.item;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import io.netty.buffer.Unpooled;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import ru.givler.mbo.client.font.TooltipElements;
import ru.givler.mbo.client.gui.ItemDescriptionTooltips;
import ru.givler.mbo.network.packet.PacketItemDescription;
import ru.givler.mbo.tileentity.TileEntityDescriptionWorkbench;

public final class ItemDescriptionSmoke {
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        int modelCount = ru.givler.mbo.block.BlockDecorModel.getAllModels().size();
        ru.givler.mbo.block.craft.BlockDescriptionWorkbench block = new ru.givler.mbo.block.craft.BlockDescriptionWorkbench();
        check(ru.givler.mbo.block.BlockDecorModel.getAllModels().size() == modelCount + 1
                && ru.givler.mbo.block.BlockDecorModel.getAllModels().contains(block),
                "Workbench must join the shared model registration list once");
        check(net.minecraft.block.Block.blockRegistry.getNameForObject(block) == null,
                "Constructor must not register a second ItemBlock before DecorBlockRegistry.init");
        int[][] offsets = {{0, 0, 1}, {-1, 0, 0}, {0, 0, -1}, {1, 0, 0}};
        for (int facing = 0; facing < 4; facing++) {
            List<int[]> parts = block.getCollisionPartPositions(0, 0, 0, facing);
            check(parts.size() == 1 && Arrays.equals(parts.get(0), offsets[facing]),
                    "Workbench must occupy owner plus one horizontal helper for facing " + facing);
            for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++)
                check(block.getMultipartBoxesForCell(x, 1, z, facing).length == 0,
                        "Upper decorative objects must not occupy collision blocks");
        }
        java.lang.reflect.Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        net.minecraft.entity.EntityLivingBase placer = (net.minecraft.entity.EntityLivingBase)
                ((sun.misc.Unsafe)unsafeField.get(null)).allocateInstance(net.minecraft.entity.player.EntityPlayerMP.class);
        Method facingMethod = ru.givler.mbo.block.BlockDecorModel.class.getDeclaredMethod("getPlacementFacing", net.minecraft.entity.EntityLivingBase.class);
        facingMethod.setAccessible(true);
        int[][] rightOffsets = {{-1,0,0},{0,0,-1},{1,0,0},{0,0,1}};
        for(int direction=0;direction<4;direction++) {
            placer.rotationYaw=direction*90;
            int facing=(Integer)facingMethod.invoke(block,placer);
            check(Arrays.equals(block.getCollisionPartPositions(0,0,0,facing).get(0),rightOffsets[direction]),
                    "Second workbench cell is not to the player's right at yaw " + placer.rotationYaw);
        }
        Item item = new Item();
        Method register = Item.itemRegistry.getClass().getDeclaredMethod("addObjectRaw", int.class, String.class, Object.class);
        register.setAccessible(true); register.invoke(Item.itemRegistry, 30012, "mbo:test_description_item", item);
        ItemStack stack = new ItemStack(item);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("MF_CustomTag0", "Old weapon story"); tag.setInteger("OtherModValue", 17);
        NBTTagCompound display = new NBTTagCompound();
        NBTTagList lore = new NBTTagList(); lore.appendTag(new NBTTagString("Vanilla lore")); display.setTag("Lore", lore);
        tag.setTag("display", display); stack.setTagCompound(tag);
        ItemDescription.write(stack, Arrays.asList("First line", "", "Second line", ""), 0x123abc);
        check(ItemDescription.read(stack).equals(Arrays.asList("First line", "", "Second line")), "Multiline round trip");
        check(ItemDescription.color(stack) == 0x123abc && stack.getTagCompound().getInteger("OtherModValue") == 17,
                "Color or unrelated NBT lost");
        int[] wordColors = {0xff0000, 0xff0000, 0x00ff00, 0x00ff00};
        ItemDescription.write(stack, Arrays.asList("word"), Arrays.asList(wordColors), 0x123abc);
        check(Arrays.equals(ItemDescription.colors(stack).get(0), wordColors), "Selected-word colours lost in NBT");
        List<String> coloredPreview = ItemDescriptionTooltips.preview(stack, null, 240);
        check(coloredPreview.get(0).equals("<mbo:color:ff0000>wo<mbo:color:00ff00>rd"), "Colour run boundaries lost");
        check(Arrays.equals(ItemDescription.editColors("word", "woXrd", wordColors, 0x0000ff),
                new int[]{0xff0000, 0xff0000, 0x0000ff, 0x00ff00, 0x00ff00}), "Typing destroyed neighbouring word colours");
        check(Arrays.equals(ItemDescription.editColors("word", "wrd", wordColors, 0),
                new int[]{0xff0000, 0x00ff00, 0x00ff00}), "Deletion shifted colours incorrectly");
        ItemDescription.write(stack, Arrays.asList("First line", "", "Second line"), 0x123abc);
        List<String> rows = new ArrayList<String>(Arrays.asList("Weapon", "Old weapon story", "Vanilla lore",
                "<mbo:statline><mbo:weapon>+2 Damage", "90% Piercing"));
        ItemDescriptionTooltips.decorate(stack, rows, null);
        int scrollRow = -1, damageRow = -1;
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).contains("<mbo:scroll>")) scrollRow = i;
            if (rows.get(i).contains("90% Piercing")) damageRow = i;
        }
        check(scrollRow > damageRow, "Description must be below every equipment statistic");
        TooltipElements.expandBlocks(rows);
        check(rows.toString().contains("<mbo:scroll>Old weapon story"), "Existing MF2 description not decorated");
        check(rows.toString().contains("<mbo:color:123abc>First line"), "Custom description color missing");
        check(rows.toString().contains("Vanilla lore") && rows.toString().contains("+2 Damage"), "Existing lore/stats lost");
        List<String> once = new ArrayList<String>(rows);
        ItemDescriptionTooltips.decorate(stack, rows, null); TooltipElements.expandBlocks(rows);
        check(once.equals(rows), "Description formatting is not idempotent");
        check(TooltipElements.tokenAt("<mbo:color:123abc>", 0).textColor == 0x123abc, "RGB text tag");
        Method mapping = TileEntity.class.getDeclaredMethod("addMapping", Class.class, String.class);
        mapping.setAccessible(true); mapping.invoke(null, TileEntityDescriptionWorkbench.class, "mbo:test_description_table");
        TileEntityDescriptionWorkbench tile = new TileEntityDescriptionWorkbench();
        tile.setInventorySlotContents(0, stack);
        NBTTagCompound saved = new NBTTagCompound(); tile.writeToNBT(saved);
        TileEntityDescriptionWorkbench loaded = new TileEntityDescriptionWorkbench(); loaded.readFromNBT(saved);
        check(ItemStack.areItemStacksEqual(stack, loaded.getStackInSlot(0)), "Table inventory/NBT persistence");
        check(!loaded.canUpdate(), "Stationary workbench must not tick");
        check(loaded.getStackInSlotOnClosing(0) != null && loaded.getStackInSlot(0) == null, "Drop must take item exactly once");
        PacketItemDescription packet = new PacketItemDescription(4, stack.copy(), ItemDescription.read(stack), 0x123abc);
        io.netty.buffer.ByteBuf bytes = Unpooled.buffer(); packet.toBytes(bytes);
        new PacketItemDescription().fromBytes(bytes); check(!bytes.isReadable(), "Packet round trip");
        java.lang.reflect.Field valid = PacketItemDescription.class.getDeclaredField("valid");
        valid.setAccessible(true);
        PacketItemDescription oversized = new PacketItemDescription(4, stack.copy(),
                Arrays.asList("1", "2", "3", "4", "5", "6", "7"), 0);
        bytes.clear(); oversized.toBytes(bytes);
        PacketItemDescription received = new PacketItemDescription(); received.fromBytes(bytes);
        check(!valid.getBoolean(received), "Oversized network description accepted");
        ItemDescription.write(stack, Arrays.asList("\u00a7c<mbo:weapon>bad\ntext"), 0xffffff);
        check(!ItemDescription.read(stack).get(0).contains("<") && !ItemDescription.read(stack).get(0).contains("\u00a7"),
                "Formatting injection not removed");
        ItemDescription.write(stack, new ArrayList<String>(), 0);
        check(!stack.getTagCompound().hasKey(ItemDescription.KEY) && stack.getTagCompound().getInteger("OtherModValue") == 17,
                "Clear damaged unrelated item data");
        System.out.println("Description workbench passed: arbitrary items, NBT preservation, color, multiline lore, sections, persistence, packet and single-drop semantics");
    }
}
