package com.imoonday.injuryrecord.network;

import com.imoonday.injuryrecord.data.DamageRecord;
import com.imoonday.injuryrecord.data.HistoricalInjuryData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 客户端向服务端请求数据
 */
public class RequestRecordsC2SRequest implements NetworkPacket {

    private static final int MAX_RECORD_COUNT = 100;
    @Nullable
    private final UUID targetUuid;
    private final boolean includeOffline;

    public RequestRecordsC2SRequest(boolean includeOffline) {
        this(null, includeOffline);
    }

    public RequestRecordsC2SRequest(@Nullable UUID targetUuid, boolean includeOffline) {
        this.targetUuid = targetUuid;
        this.includeOffline = includeOffline;
    }

    public RequestRecordsC2SRequest(FriendlyByteBuf buffer) {
        this.targetUuid = buffer.readBoolean() ? buffer.readUUID() : null;
        this.includeOffline = buffer.readBoolean();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(targetUuid != null);
        if (targetUuid != null) {
            buffer.writeUUID(targetUuid);
        }
        buffer.writeBoolean(includeOffline);
    }

    @Override
    public void handle(NetworkEvent.Context ctx) {
        ServerPlayer player = ctx.getSender();
        if (player != null) {
            MinecraftServer server = player.server;
            HistoricalInjuryData data = HistoricalInjuryData.fromServer(server);
            if (targetUuid != null) {
                DamageRecord record = data.getDamageRecord(targetUuid);
                if (record != null) {
                    List<DamageRecord> split = record.split(MAX_RECORD_COUNT);
                    int size = split.size();
                    for (int i = 0; i < size; i++) {
                        DamageRecord damageRecord = split.get(i);
                        Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new UpdateRecordsS2CPacket(damageRecord, i == 0, false, i == size - 1));
                    }
                } else {
                    player.sendSystemMessage(Component.translatable("message.injuryrecord.no_record_found", targetUuid));
                }
            } else {
                boolean cleared = false;
                Map<UUID, DamageRecord> records = includeOffline ? data.getDamageRecords() : data.getOnlineRecords(server);

                if (records.isEmpty() && !includeOffline) {
                    records = data.getDamageRecords();
                }

                Iterator<DamageRecord> iterator = records.values().iterator();
                if (iterator.hasNext()) {
                    do {
                        DamageRecord damageRecord = iterator.next();
                        List<DamageRecord> split = damageRecord.split(MAX_RECORD_COUNT);
                        int size = split.size();
                        for (int i = 0; i < size; i++) {
                            DamageRecord record = split.get(i);
                            Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new UpdateRecordsS2CPacket(record, i == 0, !cleared, i == size - 1 && !iterator.hasNext()));
                            cleared = true;
                        }
                    } while (iterator.hasNext());
                } else {
                    Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new UpdateRecordsS2CPacket(data.getDamageRecord(player), true, true, true));
                }
            }
        }
    }
}
