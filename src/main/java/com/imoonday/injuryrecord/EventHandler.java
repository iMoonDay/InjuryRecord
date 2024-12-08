package com.imoonday.injuryrecord;

import com.imoonday.injuryrecord.data.HistoricalInjuryData;
import com.imoonday.injuryrecord.network.Network;
import com.imoonday.injuryrecord.network.SendRecordsS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {

    private static final int MAX_RECORD_COUNT = 20;

    /**
     * 玩家加入服务器时，向客户端发送最近的记录
     */
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            MinecraftServer server = serverPlayer.server;
            HistoricalInjuryData data = HistoricalInjuryData.fromServer(server);
            Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new SendRecordsS2CPacket(data.getOnlineRecords(server), MAX_RECORD_COUNT));
        }
    }


    /**
     * 玩家受伤时，记录伤害数据
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MinecraftServer server = player.server;
            HistoricalInjuryData data = HistoricalInjuryData.fromServer(server);
            data.addInjury(player, event.getSource(), false, event.getAmount(), System.currentTimeMillis());
        }
    }

    /**
     * 玩家死亡时，记录伤害数据
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MinecraftServer server = player.server;
            HistoricalInjuryData data = HistoricalInjuryData.fromServer(server);
            data.addInjury(player, event.getSource(), true, -1, System.currentTimeMillis());
        }
    }
}
