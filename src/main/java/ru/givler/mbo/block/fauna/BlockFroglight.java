package ru.givler.mbo.block.fauna;

import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import ru.givler.mbo.block.BlockBase;

public final class BlockFroglight extends BlockBase {
  private final IIcon[] sides = new IIcon[3], tops = new IIcon[3];

  public BlockFroglight() {
    super(Material.rock, "mbo.froglight", "fauna/ochre_froglight_side", false);
    setHarvestLevel(null, -1);
    setResistance(0);
    setHardness(.3F);
    setLightOpacity(255);
    setLightLevel(1F);
  }

  @Override
  public void registerBlockIcons(IIconRegister register) {
    String[] colors = {"ochre", "pearlescent", "verdant"};
    for (int i = 0; i < 3; i++) {
      sides[i] = register.registerIcon("mbo:fauna/" + colors[i] + "_froglight_side");
      tops[i] = register.registerIcon("mbo:fauna/" + colors[i] + "_froglight_top");
    }
  }

  @Override
  public IIcon getIcon(int side, int metadata) {
    int variant = Math.min(2, metadata & 3), axis = metadata & 12;
    boolean end = axis == 0 ? side < 2 : axis == 4 ? side >= 4 : side == 2 || side == 3;
    return end ? tops[variant] : sides[variant];
  }

  @Override
  public int onBlockPlaced(
      World world,
      int x,
      int y,
      int z,
      int side,
      float hitX,
      float hitY,
      float hitZ,
      int metadata) {
    return (metadata & 3) | (side < 2 ? 0 : side >= 4 ? 4 : 8);
  }

  @Override
  public int damageDropped(int metadata) {
    return Math.min(2, metadata & 3);
  }

  @Override
  public void getSubBlocks(Item item, CreativeTabs tab, List stacks) {
    for (int i = 0; i < 3; i++) stacks.add(new ItemStack(item, 1, i));
  }
}
