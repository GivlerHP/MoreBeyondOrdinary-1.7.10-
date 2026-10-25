package ru.givler.mbo.waterlogging;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockWall;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.BlockPistonMoving;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.block.DoorBase;
import ru.givler.mbo.block.BlockCampfire;
import ru.givler.mbo.block.model.BlockModelCollision;

public final class WaterloggedBlockSupport {
  private WaterloggedBlockSupport() {}
  private static final int MF2 = 1, CARPENTER = 2, NPC_DECOR = 4, MODEL = 8, NPC = 16;
  private static final java.util.Map<Class<?>,Integer> OPTIONAL_TYPES =
      new java.util.concurrent.ConcurrentHashMap<Class<?>,Integer>();

  /** Optional integrations use superclass names so these mods remain optional. */
  private static int optionalType(Block block) {
    return optionalType(block.getClass());
  }

  private static int optionalType(Class<?> type) {
    Integer cached=OPTIONAL_TYPES.get(type);
    if(cached!=null) return cached;
    int flags=0;
    for(Class<?> parent=type;parent!=null;parent=parent.getSuperclass()) {
      String name=parent.getName();
      if(name.startsWith("minefantasy.mf2.block.")) flags|=MF2;
      if(name.equals("minefantasy.mf2.block.decor.BlockGeoModel")
          || name.equals("minefantasy.mf2.block.decor.BlockModelCollision")) flags|=MODEL;
      if(name.equals("thaumcraft.common.blocks.BlockLoot")
          || name.equals("biomesoplenty.common.blocks.BlockBones")
          || name.equals("biomesoplenty.common.blocks.BlockGrave")
          || name.equals("com.jaquadro.minecraft.gardenstuff.block.BlockHeavyChain")
          || name.equals("com.jaquadro.minecraft.gardenstuff.block.BlockLightChain")
          || name.equals("com.jaquadro.minecraft.gardenstuff.block.BlockHorizontalChain")) flags|=MODEL;
      for(Class<?> contract:parent.getInterfaces())
        if(contract.getName().equals("com.jaquadro.minecraft.gardencore.api.block.IChain")) flags|=MODEL;
      if(name.equals("com.carpentersblocks.block.BlockCoverable")) flags|=CARPENTER;
      if(name.startsWith("noppes.npcs.blocks.") && (flags & NPC)==0) {
        flags|=NPC;
        if(isNpcDecor(name.substring(name.lastIndexOf('.')+1))) flags|=NPC_DECOR;
      }
    }
    OPTIONAL_TYPES.put(type,flags);
    return flags;
  }

  private static boolean isNpcDecor(String name) {
    return !name.equals("BlockBorder") && !name.equals("BlockTrigger")
        && !name.equals("BlockWaypoint") && !name.equals("BlockPlaceholder")
        && !name.equals("BlockScripted") && !name.equals("BlockNpcRedstone");
  }

  /** Model collision envelopes are not sealed fluid volumes. */
  public static boolean isWaterPermeableModel(Block block) {
    if(block instanceof BlockModels || block instanceof BlockModelCollision
        || block instanceof BlockCactus || block instanceof BlockAnvil
        || block instanceof BlockEndPortalFrame || isPartialTile(block)) return true;
    int type=optionalType(block);
    if((type & (MODEL|NPC_DECOR))!=0) return true;
    // Keep the existing slab, stair, pane and door compartment rules.
    return (type & MF2)!=0 && isPartial(block)
        && !(block instanceof BlockSlab) && !(block instanceof BlockStairs)
        && !(block instanceof BlockPane) && !(block instanceof BlockDoor)
        && !(block instanceof BlockTrapDoor);
  }

  private static boolean isPartial(Block block) {
    return !block.isOpaqueCube() || !block.renderAsNormalBlock();
  }

  private static boolean isPartialTile(Block block) {
    return block instanceof BlockContainer && !(block instanceof BlockPistonMoving)
        && (optionalType(block) & (CARPENTER|MF2|NPC))==0 && isPartial(block);
  }

  public static boolean canWaterlog(World world, int x, int y, int z) {
    if (world == null || y < -64 || y > 255 || !world.blockExists(x, y, z)) return false;
    Block block = world.getBlock(x, y, z);
    // CaveAbyss crystal blocks contain source water, including when placed by hand.
    if (block.getMaterial() == Material.water) {
      Object name = Block.blockRegistry.getNameForObject(block);
      if (name != null && name.toString().startsWith("caveabyss:")
          && (name.toString().endsWith("_aquamarine_bud")
              || name.toString().endsWith(":aquamarine_cluster"))) return true;
    }
    return supportsWaterlogging(block);
  }

  /** Shared block eligibility for server flow, bucket use and client rendering. */
  public static boolean supportsWaterlogging(Block block) {
    if(block==null || block.getMaterial()==Material.lava) return false;
    if (block instanceof BlockStairs) return true;
    if (block instanceof BlockChest || block instanceof BlockCampfire) return true;
    if (block instanceof BlockSlab) return !block.isOpaqueCube();
    if(block instanceof BlockAnvil || block instanceof BlockCactus || isPartialTile(block)
        || block instanceof BlockPistonBase || block instanceof BlockPistonExtension
        || block instanceof BlockPistonMoving || block instanceof BlockEndPortalFrame) return true;
    int type=optionalType(block);
    if((type & (CARPENTER|NPC_DECOR|MODEL))!=0 || ((type & MF2)!=0 && isPartial(block))) return true;
    return block instanceof BlockModels
        || block instanceof BlockModelCollision
        || block instanceof BlockFence
        || block instanceof BlockWall
        || block instanceof BlockTrapDoor
        || block instanceof BlockFenceGate
        || block instanceof DoorBase
        || block instanceof BlockDoor
        || block instanceof BlockPane
        || block instanceof BlockLever
        || block instanceof BlockButton
        || block instanceof BlockBasePressurePlate;
  }
}
