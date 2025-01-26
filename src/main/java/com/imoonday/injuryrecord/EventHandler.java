package com.imoonday.injuryrecord;

import com.imoonday.injuryrecord.data.DamageData;
import com.imoonday.injuryrecord.data.HistoricalInjuryData;
import com.imoonday.injuryrecord.network.Network;
import com.imoonday.injuryrecord.network.SyncNewDataS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {

    /**
     * 玩家受伤时，记录伤害数据，并向客户端同步数据
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MinecraftServer server = player.server;
            HistoricalInjuryData data = HistoricalInjuryData.fromServer(server);
            DamageData damageData = data.addInjury(player, event.getSource(), false, event.getAmount(), System.currentTimeMillis());
            Network.INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncNewDataS2CPacket(player.getUUID(), damageData));
        }
    }

    /**
     * 玩家死亡时，记录伤害数据，并向客户端同步数据
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MinecraftServer server = player.server;
            HistoricalInjuryData data = HistoricalInjuryData.fromServer(server);
            DamageData damageData = data.addInjury(player, event.getSource(), true, -1, System.currentTimeMillis());
            Network.INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncNewDataS2CPacket(player.getUUID(), damageData));
        }
    }
}
