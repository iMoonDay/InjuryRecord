package com.imoonday.injuryrecord.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public interface NetworkPacket {

    void encode(FriendlyByteBuf buffer);

    void handle(NetworkEvent.Context ctx);
}
