package ru.givler.mbo.block.specialblocks;

import net.minecraft.entity.Entity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class BlockFog extends Block {

    public BlockFog(Material material, String name, String texture) {
        super(material);

        this.setBlockName(name);   // Устанавливаем внутреннее (регистрационное) имя блока
        this.setLightLevel(0.6F);   // Устанавливаем уровень освещения блока (0.0F — не светится, 1.0F — максимальная яркость)
        this.setLightOpacity(0);   // Устанавливаем прозрачность блока (0 — полностью прозрачный, 255 — полностью непрозрачный)
        this.setHardness(1.0F);      // Устанавливаем твёрдость блока (1.0F — обычная твёрдость камня, 0.0F — моментально разрушается)
        this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);   // Добавляем блок в пользовательскую креативную вкладку
        this.setResistance(10.0F);         // Устанавливаем сопротивление взрывам
        this.setHarvestLevel("pick_axe", 1);  // Устанавливаем инструмент, необходимый для добычи блока
        this.setStepSound(soundTypeStone);              // Устанавливаем звук при размещении/разрушении блока
        this.setBlockTextureName(MoreBeyondOrdinary.MODID + ":" + texture); // Задаём текстуру блока
        this.setBlockUnbreakable();
        GameRegistry.registerBlock(this, name);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return null;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("message.fog.warning"))
                    .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
        }
        return true;
    }

    public boolean isOpaqueCube() {
        return false; // Этот блок будет прозрачным, как стекло
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false; // Блок не рендерится как обычный куб
    }

    @Override
    public int getRenderBlockPass() {
        return 1; // Второй проход для прозрачных блоков
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        Block block = world.getBlock(x, y, z);
        // Все блоки этого типа (например, Fog) не рендерятся со стороны самого себя
        if (block instanceof BlockFog) {
            return false; // Не рендерить соседние блоки Fog
        }
        return super.shouldSideBeRendered(world, x, y, z, side);
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            player.setInWeb();
            boolean hasBlindness = player.getActivePotionEffect(Potion.blindness) != null;

            if (!hasBlindness) {
                player.addPotionEffect(new PotionEffect(Potion.blindness.id, 100, 0));
                player.addPotionEffect(new PotionEffect(Potion.confusion.id, 100, 0));
            }
        }
    }
}
