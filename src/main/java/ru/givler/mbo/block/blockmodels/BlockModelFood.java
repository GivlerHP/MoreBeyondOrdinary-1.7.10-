package ru.givler.mbo.block.blockmodels;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.tileentity.ModelTileBase;

public class BlockModelFood extends BlockModels {

    private int foodAmount = 4;
    private float saturationModifier = 0.3F;
    private PotionEffect[] foodEffects = null;
    private int eatDelayTicks = 20;
    private String eatSound = "random.eat";
    private int eatSoundInterval = 4;
    private boolean repeatEatSound = true;

    private Block emptyBlock;

    public BlockModelFood(Material material, String name, String texture, String model) {
        super(material, name, texture, model);
    }


    public BlockModelFood setFoodAmount(int amount) {
        this.foodAmount = amount;
        return this;
    }

    public BlockModelFood setSaturationModifier(float saturation) {
        this.saturationModifier = saturation;
        return this;
    }

    public BlockModelFood setEatDelay(int ticks) {
        this.eatDelayTicks = ticks;
        return this;
    }

    public BlockModelFood setEatSound(String sound) {
        this.eatSound = sound;
        return this;
    }

    public BlockModelFood setEatSoundInterval(int ticks) {
        this.eatSoundInterval = ticks;
        return this;
    }

    public BlockModelFood setFoodEffects(PotionEffect... effects) {
        this.foodEffects = effects;
        return this;
    }

    public BlockModelFood setEmptyBlock(net.minecraft.block.Block emptyBlock) {
        this.emptyBlock = emptyBlock;
        return this;
    }

    public int getFoodAmount() { return foodAmount; }

    public BlockModelFood setRepeatEatSound(boolean repeat) {
        this.repeatEatSound = repeat;
        return this;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {

        int meta = world.getBlockMetadata(x, y, z);
        if (isTopPartMeta(meta)) return false;

        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof ModelTileBase)) return false;
        ModelTileBase tile = (ModelTileBase) te;

        if (tile.isEating()) {
            return true;
        }

        if (!world.isRemote) {
            tile.startEating(eatDelayTicks, eatSoundInterval);
            tile.markDirty();

            player.getFoodStats().addStats(foodAmount, saturationModifier);

            if (foodEffects != null) {
                for (PotionEffect effect : foodEffects) {
                    player.addPotionEffect(new PotionEffect(effect));
                }
            }
        }

        world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, eatSound, 0.5F,
                1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F);

        return true;
    }

    public void playEatSound(World world, int x, int y, int z) {
        world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, eatSound, 0.5F,
                1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F);
    }

    public void onEatingFinished(World world, int x, int y, int z) {
        if (emptyBlock == null) return;
        int meta = world.getBlockMetadata(x, y, z);
        world.setBlock(x, y, z, emptyBlock, meta, 2);
    }

    @Override
    public int quantityDropped(int meta, int fortune, Random random) {
        return 0;
    }

    private boolean isTopPartMeta(int meta) {
        return meta >= 4;
    }
}