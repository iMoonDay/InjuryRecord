package com.imoonday.injuryrecord.network;

import com.imoonday.injuryrecord.client.ClientDamageRecordsCache;
import com.imoonday.injuryrecord.client.ClientUtils;
import com.imoonday.injuryrecord.data.DamageRecord;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * 服务端向客户端发送更新的数据
 */
public class UpdateRecordsS2CPacket implements NetworkPacket {

    private final DamageRecord record;
    private final boolean overwrite;
    private final boolean clear;
    private final boolean finished;

    public UpdateRecordsS2CPacket(DamageRecord record, boolean overwrite, boolean clear, boolean finished) {
        this.record = record;
        this.overwrite = overwrite;
        this.clear = clear;
        this.finished = finished;
    }

    public UpdateRecordsS2CPacket(FriendlyByteBuf buffer) {
        this(DamageRecord.fromNbt(buffer.readNbt()), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeNbt(record.toNbt());
        buffer.writeBoolean(overwrite);
        buffer.writeBoolean(clear);
        buffer.writeBoolean(finished);
    }

    @Override
    public void handle(NetworkEvent.Context ctx) {
        if (clear) {
            ClientDamageRecordsCache.INSTANCE.clearRecords();
        }
        ClientDamageRecordsCache.INSTANCE.updateRecord(record, overwrite);
        if (finished) {
            ClientUtils.updateDamageRecordList();
        }
    }
}
