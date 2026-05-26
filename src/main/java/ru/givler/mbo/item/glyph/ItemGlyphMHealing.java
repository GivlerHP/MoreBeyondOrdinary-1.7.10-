package ru.givler.mbo.item.glyph;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketSpawnParticle;
import ru.givler.mbo.registry.CreativeTabRegistry;

import java.util.List;

public class ItemGlyphMHealing extends ItemGlyphBasic {

    public ItemGlyphMHealing(String name, String texture, int maxStackSize) {
        super(name, texture, maxStackSize);
        this.canRepair = false;
        this.setUnlocalizedName(name);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxDamage(240);
        this.maxStackSize = maxStackSize;
        GameRegistry.registerItem(this, name);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) {
        super.onUpdate(stack, world, entity, par4, par5);

        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            if (player.getCurrentEquippedItem() == stack) {
                if (!world.isRemote) {
                    double radius = 8.0D;
                    List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class,
                            player.boundingBox.expand(radius, radius, radius));

                    for (EntityPlayer target : players) {
                        if (player.getDistanceToEntity(target) <= radius && target.getHealth() < target.getMaxHealth() && player.worldObj.getTotalWorldTime() % 60 == 0) {
                            target.heal(1.0F);
                        }
                        if (player.getDistanceToEntity(target) <= radius) {
                            target.addPotionEffect(new PotionEffect(Potion.regeneration.id, 60, 0, true));
                        }
                    }

                    player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 100, 0));
                    player.addPotionEffect(new PotionEffect(Potion.weakness.id, 200, 2));

                    if (world.rand.nextInt(6) == 0) {
                        NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(
                                player.dimension,
                                player.posX, player.posY, player.posZ, 64.0
                        );

                        for (int i = 0; i < 2; i++) {
                            double angle = world.rand.nextDouble() * Math.PI * 2;
                            double r = 8.0;
                            double xOffset = Math.cos(angle) * r * (world.rand.nextDouble() - 0.5) * 2;
                            double yOffset = world.rand.nextDouble() * 1.0;
                            double zOffset = Math.sin(angle) * r * (world.rand.nextDouble() - 0.5) * 2;

                            PacketManager.INSTANCE.sendToAllAround(
                                    new PacketSpawnParticle(
                                            EnumParticleType.VANILLA_HEART,
                                            player.posX + xOffset,
                                            player.posY + yOffset,
                                            player.posZ + zOffset,
                                            0.0, 0.1, 0.0
                                    ), point
                            );
                        }
                    }
                }
            }
        }
    }
}