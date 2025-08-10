package ru.givler.mbo.handler;

import baubles.api.BaublesApi;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import ru.givler.mbo.item.IActivatableAmulet;

public class PacketActivateAmulet implements IMessage {

    public PacketActivateAmulet() {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    // Обработчик пакета
    public static class Handler implements IMessageHandler<PacketActivateAmulet, IMessage> {
        @Override
        public IMessage onMessage(PacketActivateAmulet message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
         //   System.out.println("[DEBUG] Пакет получен от: " + player.getCommandSenderName());

            if (AmuletCooldownTracker.isOnCooldown(player)) {
                long millis = AmuletCooldownTracker.getRemainingCooldown(player);
                int seconds = (int) Math.ceil(millis / 1000.0);
            //    System.out.println("[DEBUG] Амулеты на кулдауне. Осталось: " + millis + " мс");
                player.addChatMessage(new ChatComponentText(StatCollector.translateToLocalFormatted("message.amulet_ready_in", seconds)));

                return null;
            }

            for (int i = 0; i < 4; i++) {
                ItemStack stack = BaublesApi.getBaubles(player).getStackInSlot(i);

                if (stack != null) {
                //    System.out.println("[DEBUG] Найден предмет в слоте " + i + ": " + stack.getDisplayName());

                    if (stack.getItem() instanceof IActivatableAmulet) {
                    //    System.out.println("[DEBUG] Это активируемый амулет");

                        IActivatableAmulet amulet = (IActivatableAmulet) stack.getItem();

                        if (player.experienceLevel >= amulet.getExperienceCost()) {
                       //     System.out.println("[DEBUG] Достаточно опыта. Активируем эффект");

                            amulet.activate(player, stack);
                            player.addExperienceLevel(-amulet.getExperienceCost());
                            stack.damageItem(1, player);
                            if (stack.getItemDamage() >= stack.getMaxDamage()) {
                                BaublesApi.getBaubles(player).setInventorySlotContents(i, null);
                                player.worldObj.playSoundAtEntity(player, "random.break", 1.0F, 1.0F);
                            }
                            AmuletCooldownTracker.setCooldown(player, amulet.getCooldownTicks());

                          //  System.out.println("[DEBUG] Эффект активирован, опыт снят, урон нанесён, кулдаун установлен");
                        } else {
                            player.addChatMessage(new ChatComponentText(
                                    StatCollector.translateToLocalFormatted("message.amulet_not_xp", amulet.getExperienceCost())
                            ));
                        }
                    } else {
                       // System.out.println("[DEBUG] Предмет не реализует IActivatableAmulet");
                    }
                }
            }

            return null;
        }
    }
}
