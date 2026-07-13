package electroblob.wizardry;

import java.util.Random;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.block.BlockLiquid;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.IPlantable;

public class WizardryWorldGenerator implements IWorldGenerator {
	
	/** The string identifier for wizard tower chests, used in ChestGenHooks. */
	public static final String WIZARD_TOWER = Wizardry.MODID + "wizardTower";

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world,
			IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {

		for(int id : Wizardry.flowerDimensions){
			if(id == world.provider.dimensionId) this.generatePlant(Wizardry.crystalFlower, world, random, chunkX * 16, chunkZ * 16, 2, 20);
		}
	}

	/**
	 * Generates the specified plant randomly throughout the world.
	 * @param block The plant block
	 * @param world The world
	 * @param random A random instance
	 * @param x The x coord of the first block in the chunk
	 * @param z The y coord of the first block in the chunk
	 * @param chancesToSpawn Number of chances to spawn a flower patch
	 * @param groupSize The number of times to try generating a flower per flower patch spawn
	 */
	public void generatePlant(Block block, World world, Random random, int x, int z, int chancesToSpawn, int groupSize){
		for(int i = 0; i < chancesToSpawn; i++){
			int randPosX = x + random.nextInt(16);
			int randPosY = random.nextInt(256);
			int randPosZ = z + random.nextInt(16);
			for (int l = 0; l < groupSize; ++l)
			{
				int i1 = randPosX + random.nextInt(8) - random.nextInt(8);
				int j1 = randPosY + random.nextInt(4) - random.nextInt(4);
				int k1 = randPosZ + random.nextInt(8) - random.nextInt(8);

				if(world.blockExists(i1, j1, k1) && world.isAirBlock(i1, j1, k1) && (!world.provider.hasNoSky || j1 < 127) && block.canBlockStay(world, i1, j1, k1)){
					
					world.setBlock(i1, j1, k1, block, 0, 2);
				}
			}
		}
	}

	/**
	 * Checks whether a tower generated at the given coordinates will intersect any solid or liquid blocks. Only tests for
	 * liquids (not solid blocks) for the first four layers to account for the floor and for sloping terrain.
	 * @return True if none of the blocks which the tower would replace are solid or liquid. Dirt, stone etc., water and
	 * lava count, as do logs, but leaves and plants don't.
	 */
	private static boolean checkSpaceForTower(World world, int posX, int posY, int posZ, int[][][] towerBlueprint, int orientation, boolean flip){
		int x1 = 0, z1 = 0;

		int width = towerBlueprint[0].length-1;
		
		for(int y=0; y<towerBlueprint.length; y++){
			for(int z=0; z<towerBlueprint[y].length; z++){
				for(int x=0; x<towerBlueprint[y][z].length; x++){

					switch(orientation){
					case 0:
						x1 = flip ? width-x : x;
						z1 = z;
						break;
					case 1:
						x1 = z;
						z1 = flip ? x : width-x;
						break;
					case 2:
						x1 = flip ? x : width-x;
						z1 = width-z;
						break;
					case 3:
						x1 = width-z;
						z1 = flip ? width-x : x;
						break;
					}

					if(towerBlueprint[y][z1][x1] != 0 && !WizardryUtilities.canBlockBeReplacedB(world, posX + x - width/2, posY + y, posZ + z - width/2)
							&& !(world.getBlock(posX + x - width/2, posY + y, posZ + z - width/2) instanceof IPlantable)
							&& !(world.getBlock(posX + x - width/2, posY + y, posZ + z - width/2) instanceof BlockLeavesBase)
							&& (y > 3 || world.getBlock(posX + x - width/2, posY + y, posZ + z - width/2) instanceof BlockLiquid)){
						return false;
					}
				}
			}
		}
		
		return true;
	}
}
