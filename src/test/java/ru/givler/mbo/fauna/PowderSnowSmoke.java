package ru.givler.mbo.fauna;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import ru.givler.mbo.block.fauna.BlockPowderSnow;
import ru.givler.mbo.entity.fauna.EntityMBOCod;
import ru.givler.mbo.entity.fauna.EntityMBOPolarBear;
import ru.givler.mbo.handler.FaunaEvents;

public final class PowderSnowSmoke {
  public static void main(String[] args) {
    BlockPowderSnow block = new BlockPowderSnow();
    EntityMBOCod entity = new EntityMBOCod(null);
    entity.setPosition(.5, .5, .5);
    List<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>();
    AxisAlignedBB query = AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1);
    block.addCollisionBoxesToList(null, 0, 0, 0, query, boxes, entity);
    if (!boxes.isEmpty()) throw new AssertionError("Ordinary entity must sink");
    entity.fallDistance = 3;
    block.addCollisionBoxesToList(null, 0, 0, 0, query, boxes, entity);
    if (boxes.size() != 1 || Math.abs(boxes.get(0).maxY - .9) > 1E-6)
      throw new AssertionError("Falling entity needs temporary 0.9-height support");
    if (FaunaEvents.freezeImmune(entity))
      throw new AssertionError("Unprotected fish incorrectly immune");
    entity.setCurrentItemOrArmor(
        1, new ItemStack(new ItemArmor(ItemArmor.ArmorMaterial.CLOTH, 0, 3)));
    if (!FaunaEvents.freezeImmune(entity))
      throw new AssertionError("Leather must prevent freezing");
    if (!FaunaEvents.freezeImmune(new EntityMBOPolarBear(null)))
      throw new AssertionError("Polar bear must be immune");
    System.out.println(
        "Powder snow sinking, falling support, leather and polar bear immunity passed.");
  }
}
