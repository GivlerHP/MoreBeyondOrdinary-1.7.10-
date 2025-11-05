package ru.givler.mbo.item.glyph;

        import cpw.mods.fml.common.registry.GameRegistry;
        import net.minecraft.entity.player.EntityPlayer;
        import net.minecraft.item.ItemStack;
        import net.minecraft.potion.Potion;
        import net.minecraft.potion.PotionEffect;

        import net.minecraft.world.World;
        import ru.givler.mbo.MoreBeyondOrdinary;
        import ru.givler.mbo.registry.CreativeTabRegistry;

//класс для создания предметов
public class ItemGlyphMiner extends ItemGlyphBasic {

    public ItemGlyphMiner(String name, String texture, int maxStackSize) {
        super(name, texture, maxStackSize);
        this.canRepair = false;
        this.setUnlocalizedName(name);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxDamage(1200);
        this.maxStackSize = maxStackSize;
        GameRegistry.registerItem(this, name);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        player.addPotionEffect(new PotionEffect(Potion.digSpeed.id, 400, 0));
        player.addPotionEffect(new PotionEffect(Potion.hunger.id, 400, 2));

        if (world.isRemote) {
            for (int i = 0; i < 20; i++) {
                world.spawnParticle("witchMagic",
                        player.posX + (world.rand.nextDouble() - 0.5) * 2.0,
                        player.posY + (world.rand.nextDouble() * 0.5) * -1.5,
                        player.posZ + (world.rand.nextDouble() - 0.5) * 2.0,
                        0.0, 0.1, 0.0);
            }

        }
        world.playSoundAtEntity(player, "mbo:aura", 1.0F, 1.0F);
        player.swingItem();
        itemStack.damageItem(50, player);

        return itemStack;
    }
}