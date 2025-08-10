package ru.givler.mbo.item.totems;

        import cpw.mods.fml.common.registry.GameRegistry;
        import net.minecraft.entity.Entity;
        import net.minecraft.entity.EntityLivingBase;
        import net.minecraft.entity.player.EntityPlayer;
        import net.minecraft.item.EnumRarity;
        import net.minecraft.item.Item;
        import net.minecraft.item.ItemStack;
        import net.minecraft.potion.Potion;
        import net.minecraft.potion.PotionEffect;

        import net.minecraft.world.World;
        import ru.givler.mbo.MoreBeyondOrdinary;
        import ru.givler.mbo.registry.CreativeTabRegistry;

//класс для создания предметов
public class ItemTotemMiner extends Item {

    public ItemTotemMiner(String name, String texture, int maxStackSize) {
        this.canRepair = false;
        this.setUnlocalizedName(name);
        this.setTextureName(MoreBeyondOrdinary.MODID + ":" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxDamage(1200);
        this.maxStackSize = maxStackSize;
        this.setFull3D();
        GameRegistry.registerItem(this, name);
    }

    //при ударе этим предметом по существу, предмет потеряет прочность 1 ед.
    public boolean hitEntity(ItemStack par1ItemStack, EntityLivingBase player, EntityLivingBase Entity) {
        par1ItemStack.damageItem(1, Entity);
        return false;
    }

    //что будет происходить при ударе по существу
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        return false;
    }

    //что будет происходить при нажатие правой кнопки. Например включается полёт
    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        // Применяем эффекты как на клиенте, так и на сервере
        player.addPotionEffect(new PotionEffect(Potion.digSpeed.id, 400, 0));
        player.addPotionEffect(new PotionEffect(Potion.hunger.id, 400, 2));


        // Создаём эффекты только на сервере
        if (world.isRemote) {
            for (int i = 0; i < 20; i++) {
                world.spawnParticle("witchMagic",
                        player.posX + (world.rand.nextDouble() - 0.5) * 2.0,
                        player.posY + (world.rand.nextDouble() * 0.5) * -1.5,
                        player.posZ + (world.rand.nextDouble() - 0.5) * 2.0,
                        0.0, 0.1, 0.0);
            }

        }
        // Проигрываем звук НА СЕРВЕРЕ, чтобы он был слышен всем
        world.playSoundAtEntity(player, "mbo:aura", 1.0F, 1.0F);
        player.swingItem();
        itemStack.damageItem(50, player);

        return itemStack;
    }

    //что происходит если нажать правой кнопкой по существу. Например включается, отключается урон
    public boolean itemInteractionForEntity(ItemStack itemStack, EntityPlayer player, EntityLivingBase Entity) {
        return false;
    }


    //если предмет скрафчен. Например пишет в чат.
    public void onCreated(ItemStack itemStack, World world, EntityPlayer player) {

    }

    //редкость предмета
    public EnumRarity getRarity(ItemStack itemStack) {
        return EnumRarity.epic;
    }
}