package ru.givler.mbo.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import ru.givler.mbo.entity.boat.EntityMBOBoat;
import ru.givler.mbo.entity.boat.EntityMBOChestBoat;

import java.util.List;

public class ItemMBOBoat extends Item {
    public static final String[] NAMES = {
            "oak", "spruce", "birch", "jungle", "acacia", "dark_oak",
            "sacred_oak", "cherry", "dark", "fir", "ethereal", "magic",
            "mangrove", "palm", "redwood", "willow", "bamboo", "pine",
            "hellbark", "jacaranda", "mahogany"
    };
    private final boolean chest;
    private final int firstType;
    private final int typeCount;
    private IIcon[] icons;

    public ItemMBOBoat(String name, boolean chest, int firstType, int typeCount, CreativeTabs tab) {
        this.chest = chest;
        this.firstType = firstType;
        this.typeCount = typeCount;
        setUnlocalizedName(name);
        setHasSubtypes(true);
        setMaxStackSize(1);
        setCreativeTab(tab);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int type = firstType + Math.max(0, Math.min(typeCount - 1, stack.getItemDamage()));
        return "item." + NAMES[type] + (chest ? "_chest_boat" : "_boat");
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        int type = firstType + Math.max(0, Math.min(typeCount - 1, stack.getItemDamage()));
        String boatName = StatCollector.translateToLocal("item." + NAMES[type] + "_boat.name");
        return chest ? boatName + " — " + StatCollector.translateToLocal("container.mbo.chest_boat") : boatName;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        icons = new IIcon[typeCount];
        String suffix = chest ? "_chest_boat" : "_boat";
        for (int i = 0; i < typeCount; i++) {
            int type = firstType + i;
            String texture = type < 6 ? NAMES[type] + suffix : (chest ? "chest_boat" : "boat");
            icons[i] = register.registerIcon("mbo:boat/" + texture);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int damage) {
        return icons[Math.max(0, Math.min(icons.length - 1, damage))];
    }

    @Override
    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < typeCount; i++) list.add(new ItemStack(item, 1, i));
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch);
        float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw);
        double x = player.prevPosX + (player.posX - player.prevPosX);
        double y = player.prevPosY + (player.posY - player.prevPosY) + 1.62D - player.yOffset;
        double z = player.prevPosZ + (player.posZ - player.prevPosZ);
        Vec3 start = Vec3.createVectorHelper(x, y, z);
        float cy = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float sy = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float cp = -MathHelper.cos(-pitch * 0.017453292F);
        float sp = MathHelper.sin(-pitch * 0.017453292F);
        Vec3 end = start.addVector((double)(sy * cp) * 5D, (double)sp * 5D, (double)(cy * cp) * 5D);
        MovingObjectPosition hit = world.rayTraceBlocks(start, end, true);
        if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return stack;
        int bx = hit.blockX, by = hit.blockY, bz = hit.blockZ;
        Block block = world.getBlock(bx, by, bz);
        if (block == Blocks.snow_layer) --by;
        int type = firstType + Math.max(0, Math.min(typeCount - 1, stack.getItemDamage()));
        EntityMBOBoat boat = chest
                ? new EntityMBOChestBoat(world, bx + 0.5D, by + 1D, bz + 0.5D, type)
                : new EntityMBOBoat(world, bx + 0.5D, by + 1D, bz + 0.5D, type);
        boat.rotationYaw = player.rotationYaw;
        if (!world.getCollidingBoundingBoxes(boat, boat.boundingBox.expand(-0.1D, -0.1D, -0.1D)).isEmpty()) return stack;
        if (!world.isRemote) world.spawnEntityInWorld(boat);
        if (!player.capabilities.isCreativeMode) --stack.stackSize;
        return stack;
    }
}
