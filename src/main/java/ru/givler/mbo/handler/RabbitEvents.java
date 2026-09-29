package ru.givler.mbo.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.ai.EntityAITargetNonTamed;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import ru.givler.mbo.entity.EntityRabbit;

/** Adds the rabbit prey target to wild wolves. */
public final class RabbitEvents {
    @SubscribeEvent
    public void onWolfJoin(EntityJoinWorldEvent event) {
        if (event.entity instanceof EntityWolf && !event.world.isRemote) {
            EntityWolf wolf = (EntityWolf) event.entity;
            wolf.targetTasks.addTask(4, new EntityAITargetNonTamed(wolf, EntityRabbit.class, 200, false));
        }
    }
}
