package com.imoonday.injuryrecord.network;

import com.imoonday.injuryrecord.client.ClientDamageRecordsCache;
import com.imoonday.injuryrecord.client.ClientUtils;
import com.imoonday.injuryrecord.data.DamageRecord;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

/**
 * 服务端向客户端发送更新的数据
 */
public class UpdateRecordsS2CPacket implements NetworkPacket {

    private final UUID uuid;
    private final DamageRecord record;

    public UpdateRecordsS2CPacket(UUID uuid, DamageRecord record) {
        this.uuid = uuid;
        this.record = record;
    }

    public UpdateRecordsS2CPacket(FriendlyByteBuf buffer) {
        this(buffer.readUUID(), DamageRecord.fromNbt(buffer.readNbt()));
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(uuid);
        buffer.writeNbt(record.toNbt());
    }

    @Override
    public void handle(NetworkEvent.Context ctx) {
        ClientDamageRecordsCache.INSTANCE.updateRecord(uuid, record);
        ClientUtils.updateDamageRecordList();
    }
}
