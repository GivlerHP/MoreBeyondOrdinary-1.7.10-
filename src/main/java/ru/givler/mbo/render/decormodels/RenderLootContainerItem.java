package ru.givler.mbo.render.decormodels;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.item.ItemBlockLootContainer;
import ru.givler.mbo.lootcontainer.LootContainerData;
import ru.givler.mbo.tileentity.TileEntityLootContainer;
import software.bernie.geckolib3.renderers.geo.RenderBlockItem;

public class RenderLootContainerItem extends RenderBlockItem {
    public RenderLootContainerItem(TileEntitySpecialRenderer renderer, TileEntityLootContainer tile) {
        super(renderer, tile);
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        if (dummytile instanceof TileEntityLootContainer) {
            LootContainerData config = ItemBlockLootContainer.readData(item);
            ((TileEntityLootContainer) dummytile).applyConfig(config, false);
        }
        super.renderItem(type, item, data);
    }
}
