package ru.givler.mbo.registry;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.banner.BlockBanner;
import ru.givler.mbo.banner.BlockLoom;
import ru.givler.mbo.banner.ItemBlockBanner;

public final class BannerRegistry {
    public static BlockBanner banner;
    public static BlockLoom loom;
    private BannerRegistry() { }

    public static void init() {
        banner = new BlockBanner();
        loom = new BlockLoom();
        GameRegistry.registerBlock(banner, ItemBlockBanner.class, "Banner");
        GameRegistry.registerBlock(loom, "Loom");
        for (int color=0; color<16; color++) {
            GameRegistry.addRecipe(new ItemStack(banner, 1, color), "WWW", "WWW", " S ",
                    'W', new ItemStack(Blocks.wool, 1, color), 'S', net.minecraft.init.Items.stick);
        }
        GameRegistry.addRecipe(new ItemStack(loom), "SS", "PP", 'S', net.minecraft.init.Items.string, 'P', Blocks.planks);
    }
}
