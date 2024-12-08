package com.imoonday.injuryrecord.network;

import com.imoonday.injuryrecord.InjuryRecord;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Supplier;

public class Network {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(InjuryRecord.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int i = 0;
        INSTANCE.registerMessage(i++, RequestRecordsC2SRequest.class, RequestRecordsC2SRequest::encode, RequestRecordsC2SRequest::new, Network::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(i++, SendRecordsS2CPacket.class, SendRecordsS2CPacket::encode, SendRecordsS2CPacket::new, Network::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(i++, UpdateRecordsS2CPacket.class, UpdateRecordsS2CPacket::encode, UpdateRecordsS2CPacket::new, Network::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    private static <MSG extends NetworkPacket> void handle(MSG message, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> message.handle(context));
        context.setPacketHandled(true);
    }
}
