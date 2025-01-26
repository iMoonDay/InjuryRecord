package com.imoonday.injuryrecord.network;

import com.imoonday.injuryrecord.Config;
import com.imoonday.injuryrecord.data.DamageRecord;
import com.imoonday.injuryrecord.data.HistoricalInjuryData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

/**
 * 客户端请求删除记录
 */
public class RemoveRecordC2SRequest implements NetworkPacket {

    @Nullable
    private final UUID targetUuid;
    private final boolean includeOffline;

    public RemoveRecordC2SRequest(boolean includeOffline) {
        this(null, includeOffline);
    }

    public RemoveRecordC2SRequest(@Nullable UUID targetUuid, boolean includeOffline) {
        this.targetUuid = targetUuid;
        this.includeOffline = includeOffline;
    }

    public RemoveRecordC2SRequest(FriendlyByteBuf buffer) {
        this(buffer.readBoolean() ? buffer.readUUID() : null, buffer.readBoolean());
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
            Boolean permission = Config.removePermission.get();
            if (permission != null && permission && !player.hasPermissions(2)) {
                player.sendSystemMessage(Component.translatable("message.injuryrecord.no_permission"));
                return;
            }
            MinecraftServer server = player.server;
            HistoricalInjuryData data = HistoricalInjuryData.fromServer(server);
            Map<UUID, DamageRecord> damageRecords = data.getDamageRecords();
            if (targetUuid != null) {
                damageRecords.remove(targetUuid);
            } else {
                damageRecords.clear();
            }
            new RequestRecordsC2SRequest(includeOffline).handle(ctx);
        }
    }
}
