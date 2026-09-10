package ru.givler.mbo.dungeon;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.world.ExplosionEvent;

public final class DungeonTriggerEventHandler {
    @SubscribeEvent public void onExplosion(ExplosionEvent.Detonate event){if(event.world==null||event.world.isRemote||event.explosion==null)return;DungeonAreaSavedData data=DungeonAreaSavedData.get(event.world);boolean changed=false;for(DungeonAreaRecord area:data.all())if(area.triggerExplosion(event.world,event.explosion.explosionX,event.explosion.explosionY,event.explosion.explosionZ))changed=true;if(changed)data.changed(event.world);}
}
