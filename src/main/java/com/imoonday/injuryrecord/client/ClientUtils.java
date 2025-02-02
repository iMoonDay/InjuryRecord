package com.imoonday.injuryrecord.client;

import com.imoonday.injuryrecord.data.DamageData;
import com.imoonday.injuryrecord.network.Network;
import com.imoonday.injuryrecord.network.RemoveRecordC2SRequest;
import com.imoonday.injuryrecord.network.RequestRecordsC2SRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 客户端工具类
 */
public class ClientUtils {

    public static void openDamageRecordList() {
        Minecraft.getInstance().setScreen(new DamageRecordListScreen());
    }

    public static void updateDamageRecordList() {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof DamageRecordListScreen recordListScreen) {
            recordListScreen.updateList();
        }
    }

    public static void onReceivedNewData(UUID uuid, DamageData data) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof DamageRecordListScreen recordListScreen) {
            recordListScreen.updateData(uuid, data);
        }
    }

    public static void requestRecords(boolean includeOffline) {
        Network.INSTANCE.sendToServer(new RequestRecordsC2SRequest(includeOffline));
    }

    public static void requestRecords(UUID uuid, boolean includeOffline) {
        Network.INSTANCE.sendToServer(new RequestRecordsC2SRequest(uuid, includeOffline));
    }

    public static void requestRemoveRecord(@Nullable UUID uuid, boolean includeOffline) {
        Network.INSTANCE.sendToServer(new RemoveRecordC2SRequest(uuid, includeOffline));
    }

    public static boolean isOffline(UUID uuid) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            return !connection.getOnlinePlayerIds().contains(uuid);
        }
        return true;
    }
}
