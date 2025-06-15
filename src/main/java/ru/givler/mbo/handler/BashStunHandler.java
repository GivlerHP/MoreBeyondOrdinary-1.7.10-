package ru.givler.mbo.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ru.givler.mbo.potion.ModPotions;

public class BashStunHandler {

    /**
     * На каждый тик (LivingUpdateEvent) у игрока:
     * 1) Запоминаем старые координаты.
     * 2) Гасим ввод и обнуляем скорости.
     * 3) Позже, когда игра попытается переместить сущность,
     *    принудительно вернем её в старую точку.
     */
    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (!(event.entityLiving instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.entityLiving;
        World world = player.getEntityWorld();

        if (!player.isPotionActive(ModPotions.BASH_STUN)) {
            return;
        }
        // --- Начало «жёсткого» сброса передвижения ---
        // 3) Запомним, где игрок находился до перерасчёта движения:
        double prevX = player.posX;
        double prevY = player.posY;
        double prevZ = player.posZ;

        player.moveForward = 0.0F;
        player.moveStrafing = 0.0F;

        player.motionX = 0.0;
        player.motionZ = 0.0;

        player.setSprinting(false);
        player.setJumping(false);

        player.velocityChanged = true;

        // 8) После всех внутриигровых расчётов (в том же тике),
        //    возвращаем игрока в исходную точку:
        //    поскольку LivingUpdateEvent вызывается до непосредственного
        //    применения движения, нам нужно «телепортировать» обратно.
        //    Однако если мы вызывать setPositionAndUpdate здесь,
        //    это пересинхронизирует позицию на клиенте/сервере
        if (player instanceof EntityPlayerMP) {
            ((EntityPlayerMP) player).playerNetServerHandler.setPlayerLocation(prevX, prevY, prevZ, player.rotationYaw, player.rotationPitch);
        }
        // --- Конец жёсткого сброса ---
    }

    /**
     * Блокировка любого рукопашного удара (удар кулаком/оружием).
     */
    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        EntityPlayer player = event.entityPlayer;
        if (player.isPotionActive(ModPotions.BASH_STUN)) {
            event.setCanceled(true);
        }
    }

    /**
     * Блокировка всех правых кликов (использование предмета и правый клик по блоку).
     */
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        EntityPlayer player = event.entityPlayer;
        Action action = event.action;

        if (player.isPotionActive(ModPotions.BASH_STUN)) {
            if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
                event.setCanceled(true);
            }
        }
    }
}