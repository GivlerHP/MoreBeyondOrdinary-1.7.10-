package ru.givler.mbo.block;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.Loader;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.gui.MboGuiType;
import ru.givler.mbo.registry.CreativeTabRegistry;
import ru.givler.mbo.tileentity.TileDoll;

public final class BlockDoll extends BlockContainer {
    public static final String APPEARANCE = "DollAppearance";

    public BlockDoll() {
        super(Material.wood);
        setBlockName("mbo.doll");
        setBlockTextureName("minecraft:planks_oak");
        setHardness(.7F);
        setStepSound(soundTypeWood);
        setLightOpacity(0);
        setCreativeTab(CreativeTabRegistry.tabMBOdecors);
        setBlockBounds(.22F, 0, .22F, .78F, .95F, .78F);
    }

    @Override public TileEntity createNewTileEntity(World world, int meta) { return new TileDoll(); }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public int getRenderType() { return -1; }
    @Override public int quantityDropped(int meta, int fortune, Random random) { return 0; }

    public static boolean canEdit(EntityPlayer player, World world, int x, int y, int z, int side) {
        if (!world.canMineBlock(player, x, y, z) || !player.canPlayerEdit(x, y, z, side, player.getHeldItem())) return false;
        if (Loader.isModLoaded("minefantasy2")) return minefantasy.mf2.mechanics.ProtectionHelper.canInteract(player, world, x, y, z);
        net.minecraftforge.event.entity.player.PlayerInteractEvent event = net.minecraftforge.event.ForgeEventFactory.onPlayerInteract(
            player, net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, x, y, z, side);
        return !event.isCanceled() && event.useBlock != cpw.mods.fml.common.eventhandler.Event.Result.DENY;
    }

    @Override public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        if (world.isRemote) return;
        int direction = MathHelper.floor_double(placer.rotationYaw * 16F / 360F + .5F) & 15;
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileDoll)) return;
        TileDoll doll = (TileDoll) tile;
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey(APPEARANCE, 10)) {
            net.minecraft.nbt.NBTTagCompound appearance = stack.getTagCompound().getCompoundTag(APPEARANCE);
            doll.readAppearance(appearance);
            if (appearance.hasKey("Rotation", 99)) direction = appearance.getInteger("Rotation") & 15;
            doll.markDirty();
        } else if (placer instanceof EntityPlayer) doll.setProfile(((EntityPlayer) placer).getGameProfile());
        world.setBlockMetadataWithNotify(x, y, z, direction, 3);
    }

    @Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                               int side, float a, float b, float c) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileDoll)) return false;
        if (world.isRemote) return true;
        if (!canEdit(player, world, x, y, z, side)) return true;
        ItemStack held = player.getHeldItem();
        if (held != null && held.getItem() == Items.name_tag && held.hasDisplayName()) {
            String name = held.getDisplayName();
            if (!name.matches("[A-Za-z0-9_]{1,16}")) return true;
            GameProfile profile = net.minecraft.server.MinecraftServer.getServer().func_152358_ax().func_152655_a(name);
            if (profile == null) return true;
            ((TileDoll) tile).setProfile(profile);
            if (!player.capabilities.isCreativeMode && --held.stackSize == 0) {
                player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            }
        } else player.openGui(MoreBeyondOrdinary.instance, MboGuiType.DOLL.id, world, x, y, z);
        return true;
    }

    @Override public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis) {
        return !world.isRemote && world.setBlockMetadataWithNotify(x, y, z, (world.getBlockMetadata(x, y, z) + 1) & 15, 3);
    }

    @Override public void breakBlock(World world, int x, int y, int z, Block block, int metadata) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!world.isRemote && tile instanceof TileDoll && !world.restoringBlockSnapshots) {
            TileDoll doll = (TileDoll) tile;
            ItemStack drop = new ItemStack(this);
            net.minecraft.nbt.NBTTagCompound appearance = doll.appearance();
            appearance.setInteger("Rotation", metadata & 15);
            drop.setTagInfo(APPEARANCE, appearance);
            dropBlockAsItem(world, x, y, z, drop);
            for (int slot = 0; slot < TileDoll.SIZE; slot++) {
                ItemStack equipment = doll.getStackInSlotOnClosing(slot);
                if (equipment != null) dropBlockAsItem(world, x, y, z, equipment);
            }
        }
        super.breakBlock(world, x, y, z, block, metadata);
    }
}
