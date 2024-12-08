package com.imoonday.injuryrecord.network;

import com.imoonday.injuryrecord.client.ClientDamageRecordsCache;
import com.imoonday.injuryrecord.client.ClientUtils;
import com.imoonday.injuryrecord.data.DamageRecord;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.UUID;

/**
 * 服务端向客户端发送数据
 */
public class SendRecordsS2CPacket implements NetworkPacket {

    private final Map<UUID, DamageRecord> damageRecords;
    private final int limit;

    public SendRecordsS2CPacket(Map<UUID, DamageRecord> damageRecords, int limit) {
        this.damageRecords = damageRecords;
        this.limit = limit;
    }

    public SendRecordsS2CPacket(FriendlyByteBuf buffer) {
        this(buffer.readMap(FriendlyByteBuf::readUUID, buf -> DamageRecord.fromNbt(buf.readNbt())), buffer.readInt());
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeMap(damageRecords, FriendlyByteBuf::writeUUID, (buf, damageRecord) -> buf.writeNbt(damageRecord.toLimitedNbt(limit)));
        buffer.writeInt(limit);
    }

    @Override
    public void handle(NetworkEvent.Context ctx) {
        ClientDamageRecordsCache.INSTANCE.setRecords(damageRecords);
        ClientUtils.updateDamageRecordList();
    }
}
