package com.imoonday.injuryrecord.network;

import com.imoonday.injuryrecord.client.ClientDamageRecordsCache;
import com.imoonday.injuryrecord.client.ClientUtils;
import com.imoonday.injuryrecord.data.DamageData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;

/**
 * 服务端向客户端发送新的伤害数据包
 */
public class SyncNewDataS2CPacket implements NetworkPacket {

    private final UUID uuid;
    private final DamageData data;

    public SyncNewDataS2CPacket(UUID uuid, DamageData data) {
        this.uuid = uuid;
        this.data = data;
    }

    public SyncNewDataS2CPacket(FriendlyByteBuf buffer) {
        this.uuid = buffer.readUUID();
        this.data = DamageData.fromNbt(Objects.requireNonNull(buffer.readNbt()));
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(uuid);
        buffer.writeNbt(data.toNbt());
    }

    @Override
    public void handle(NetworkEvent.Context ctx) {
        if (ClientDamageRecordsCache.INSTANCE.addInjury(uuid, data)) {
            ClientUtils.onReceivedNewData(uuid, data);
        }
    }
}
