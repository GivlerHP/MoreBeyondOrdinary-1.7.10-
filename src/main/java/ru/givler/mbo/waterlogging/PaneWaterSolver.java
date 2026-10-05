package ru.givler.mbo.waterlogging;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import ru.givler.mbo.core.WaterloggingRenderHooks;

/** Flood the four horizontal compartments once, without enumerating paths. */
final class PaneWaterSolver {
  private static final int[] DX = {0,0,-1,1}, DZ = {-1,1,0,0};
  private static final ForgeDirection[] SIDES = {ForgeDirection.NORTH,ForgeDirection.SOUTH,ForgeDirection.WEST,ForgeDirection.EAST};
  private static final int[][] TOUCH = {{0,2},{1,3},{0,1},{2,3}};
  private static final int[][] MIRROR = {{1,3},{0,2},{2,3},{0,1}};
  private static final class Node {
    final int x,y,z,id;
    final boolean[] free,wet = new boolean[4],arms = new boolean[4];
    final Node[] neighbours = new Node[4];
    Node(World world,int x,int y,int z,int id) {
      this.x=x; this.y=y; this.z=z; this.id=id;
      free = WaterloggedGeometry.freeCells(world,x,y,z);
      BlockPane pane = (BlockPane)world.getBlock(x,y,z);
      for (int side=0;side<4;++side)
        arms[side]=pane.canPaneConnectTo(world,x+DX[side],y,z+DZ[side],SIDES[side]);
    }
  }
  static boolean[] solve(World world,int x,int y,int z) {
    List<Node> nodes = new ArrayList<Node>();
    Map<String,Node> indexed = new HashMap<String,Node>();
    Node root = new Node(world,x,y,z,0); nodes.add(root); indexed.put(key(x,y,z),root);
    ArrayDeque<Integer> pending = new ArrayDeque<Integer>();
    for (int cursor=0;cursor<nodes.size();++cursor) {
      Node node=nodes.get(cursor);
      for (int side=0;side<4;++side) {
        int nx=node.x+DX[side], nz=node.z+DZ[side];
        Block adjacent=world.getBlock(nx,y,nz);
        if (adjacent.getMaterial()==Material.water && world.getBlockMetadata(nx,y,nz)==0) {
          seed(node,TOUCH[side][0],pending); seed(node,TOUCH[side][1],pending);
        } else if (adjacent instanceof BlockPane
            && WaterloggingRenderHooks.isWaterlogged(world,nx,y,nz)) {
          if (adjacent.getMaterial()==Material.iron) {
            seed(node,TOUCH[side][0],pending); seed(node,TOUCH[side][1],pending);
          } else {
            String key=key(nx,y,nz); Node neighbour=indexed.get(key);
            if (neighbour==null) {
              neighbour=new Node(world,nx,y,nz,nodes.size()); nodes.add(neighbour); indexed.put(key,neighbour);
            }
            node.neighbours[side]=neighbour;
            neighbour.neighbours[side^1]=node;
          }
        }
      }
      boolean isolated=!node.arms[0]&&!node.arms[1]&&!node.arms[2]&&!node.arms[3];
      boolean source=!world.isRemote && world.perWorldStorage!=null
          && (WaterloggedWorldData.get(world).ingressMask(node.x,y,node.z)&WaterloggedWorldData.INTERNAL_SOURCE)!=0;
      Block above=world.getBlock(node.x,y+1,node.z);
      source |= above!=null && above.getMaterial()==Material.water && world.getBlockMetadata(node.x,y+1,node.z)==0;
      if (world.isRemote && world.provider!=null
          && ClientWaterloggedBlocks.contains(world.provider.dimensionId,node.x,y,node.z)) {
        boolean connected=false;
        for (int side=0;side<4;++side)
          connected |= ClientWaterloggedBlocks.contains(world.provider.dimensionId,node.x+DX[side],y,node.z+DZ[side]);
        source |= !connected && !node.wet[0]&&!node.wet[1]&&!node.wet[2]&&!node.wet[3];
      }
      if (isolated || source) for (int q=0;q<4;++q) seed(node,q,pending);
    }
    while (!pending.isEmpty()) {
      int item=pending.removeFirst(); Node node=nodes.get(item/4); int q=item%4;
      int qx=q/2,qz=q%2;
      if (!node.arms[qz]) seed(node,q^2,pending);
      if (!node.arms[2+qx]) seed(node,q^1,pending);
      for (int side=0;side<4;++side) {
        Node neighbour=node.neighbours[side]; if (neighbour==null) continue;
        for (int edge=0;edge<2;++edge)
          if (TOUCH[side][edge]==q) seed(neighbour,MIRROR[side][edge],pending);
      }
    }
    boolean[] result=null;
    for (Node node:nodes) {
      boolean[] cells=node.free.clone();
      for (int cx=0;cx<2;++cx) for (int cy=0;cy<2;++cy) for (int cz=0;cz<2;++cz)
        cells[WaterloggedGeometry.index(cx,cy,cz)] &= node.wet[cx*2+cz];
      WaterloggedGeometry.cacheCells(node.x,y,node.z,cells);
      if (node==root) result=cells;
    }
    return result;
  }
  private static void seed(Node node,int q,ArrayDeque<Integer> queue) {
    if (node.wet[q] || !(node.free[WaterloggedGeometry.index(q/2,0,q%2)]
        || node.free[WaterloggedGeometry.index(q/2,1,q%2)])) return;
    node.wet[q]=true; queue.addLast(node.id*4+q);
  }
  private static String key(int x,int y,int z) { return x+":"+y+":"+z; }
}
