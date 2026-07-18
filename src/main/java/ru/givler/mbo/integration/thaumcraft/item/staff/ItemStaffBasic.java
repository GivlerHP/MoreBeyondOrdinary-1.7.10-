package ru.givler.mbo.integration.thaumcraft.item.staff;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.IWandable;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.api.wands.WandCap;
import thaumcraft.api.wands.WandRod;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.wands.ItemWandCasting;

public class ItemStaffBasic extends ItemWandCasting {

    @SideOnly(Side.CLIENT)
    private IIcon customIcon;
    private final java.text.DecimalFormat myFormatter = new java.text.DecimalFormat("#######.##");

    public ItemStaffBasic() {
        super();
        this.setUnlocalizedName("staffBasic");
        this.setCreativeTab(Thaumcraft.tabTC);
        this.setMaxStackSize(1);
    }

    @Override
    public void setCap(ItemStack stack, WandCap cap) { }

    @Override
    public WandCap getCap(ItemStack stack) {
        return ConfigItems.WAND_CAP_IRON;
    }

    @Override
    public void setRod(ItemStack stack, WandRod rod) { }

    @Override
    public WandRod getRod(ItemStack stack) {
        return ConfigItems.WAND_ROD_WOOD;
    }

    @Override
    public ItemStack getFocusItem(ItemStack stack) {
        return null;
    }

    @Override
    public void setFocus(ItemStack stack, ItemStack focus) {

    }

    @Override
    public boolean isStaff(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isSceptre(ItemStack stack) {
        return true;
    }

    @Override
    public int getMaxVis(ItemStack stack) {
        return 50 * 100;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        ItemStack stack = new ItemStack(this, 1, 0);
        for (Aspect aspect : this.getAcceptedAspects(stack).getAspects()) {
            this.storeVis(stack, aspect, this.getMaxVis(stack));
        }
        list.add(stack);
    }

    @Override
    public void onCreated(ItemStack stack, World world, EntityPlayer player) {
        for (Aspect aspect : this.getAcceptedAspects(stack).getAspects()) {
            if (!stack.hasTagCompound() || !stack.stackTagCompound.hasKey(aspect.getTag())) {
                this.storeVis(stack, aspect, 0);
            }
        }
    }

    private AspectList getAcceptedAspects(ItemStack stack) {
        AspectList cost = this.getVisCost(stack);
        return cost == null ? new AspectList() : cost;
    }

    private boolean acceptsAspect(ItemStack stack, Aspect aspect) {
        return aspect != null && this.getAcceptedAspects(stack).getAmount(aspect) > 0;
    }

    @Override
    public AspectList getAspectsWithRoom(ItemStack stack) {
        AspectList result = new AspectList();
        for (Aspect aspect : this.getAcceptedAspects(stack).getAspects()) {
            if (this.getVis(stack, aspect) < this.getMaxVis(stack)) {
                result.add(aspect, 1);
            }
        }
        return result;
    }

    @Override
    public int addVis(ItemStack stack, Aspect aspect, int amount, boolean doit) {
        return this.acceptsAspect(stack, aspect) ? super.addVis(stack, aspect, amount, doit) : amount;
    }

    @Override
    public int addRealVis(ItemStack stack, Aspect aspect, int amount, boolean doit) {
        return this.acceptsAspect(stack, aspect) ? super.addRealVis(stack, aspect, amount, doit) : amount;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.rare;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        this.customIcon = reg.registerIcon("mbo:staff/staffBasic");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconIndex(ItemStack stack) {
        return this.customIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass) {
        return this.customIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isFull3D() {
        return true;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StatCollector.translateToLocal("item.staffBasic.name");
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        int pos = list.size();
        String tt2 = "";

        String tt = "";
        int tot = 0;
        int num = 0;

        for (Aspect aspect : this.getAcceptedAspects(stack).getAspects()) {
            int raw = this.getVis(stack, aspect);
            String amount = this.myFormatter.format(raw / 100.0F);
            float mod = this.getConsumptionModifier(stack, player, aspect, false);
            String consumption = this.myFormatter.format(mod * 100.0F);
            ++num;
            tot = (int) (tot + mod * 100.0F);

            if (Thaumcraft.proxy.isShiftKeyDown()) {
                list.add(" §" + aspect.getChatcolor() + aspect.getName() + "§r x " + amount
                        + ", §o(" + consumption + "% " + StatCollector.translateToLocal("tc.vis.cost") + ")");
            } else {
                if (tt.length() > 0) {
                    tt = tt + " | ";
                }
                tt = tt + "§" + aspect.getChatcolor() + amount + "§r";
            }
        }

        if (!Thaumcraft.proxy.isShiftKeyDown() && num > 0) {
            list.add(tt);
            tot /= num;
            tt2 = " (" + tot + "% " + StatCollector.translateToLocal("tc.vis.costavg") + ")";
        }

        list.add(pos, EnumChatFormatting.GOLD
                + StatCollector.translateToLocal("item.capacity.text") + " "
                + this.getMaxVis(stack) / 100 + "§r" + tt2);

        AspectList cost = this.getVisCost(stack);
        if (cost != null && cost.size() > 0) {
            list.add(StatCollector.translateToLocal(
                    this.isVisCostPerTick(stack) ? "item.Focus.cost2" : "item.Focus.cost1"));
            for (Aspect aspect : cost.getAspectsSorted()) {
                String amount = this.myFormatter.format(cost.getAmount(aspect) / 100F);
                list.add(" §" + aspect.getChatcolor() + aspect.getName() + "§r x " + amount);
            }
        }
    }

    public boolean isVisCostPerTick(ItemStack stack) {
        return false;
    }

    public AspectList getVisCost(ItemStack stack) {
        return (new AspectList()).add(Aspect.FIRE, 500);
    }

    @Override
    public ItemFocusBasic getFocus(ItemStack stack) {
        return null;
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world,
                                  int x, int y, int z, int side, float hx, float hy, float hz) {
        return false;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player) {
        MovingObjectPosition mop = this.getMovingObjectPositionFromPlayer(world, player, true);

        if (mop != null && mop.typeOfHit == MovingObjectType.BLOCK) {
            int i = mop.blockX;
            int j = mop.blockY;
            int k = mop.blockZ;
            Block bi = world.getBlock(i, j, k);

            if (bi instanceof IWandable) {
                ItemStack is = ((IWandable) bi).onWandRightClick(world, itemstack, player);
                if (is != null) {
                    return is;
                }
            }

            TileEntity tile = world.getTileEntity(i, j, k);
            if (tile != null && tile instanceof IWandable) {
                ItemStack is = ((IWandable) tile).onWandRightClick(world, itemstack, player);
                if (is != null) {
                    return is;
                }
            }
        }
        return itemstack;
    }
}
