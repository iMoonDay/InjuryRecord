package com.imoonday.injuryrecord.data;

import com.imoonday.injuryrecord.InjuryRecord;
import com.mojang.logging.LogUtils;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 将所有玩家的受伤记录保存到存档的数据类
 */
public class HistoricalInjuryData extends SavedData {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<UUID, DamageRecord> damageRecords = new HashMap<>();

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        CompoundTag damageRecordsTag = new CompoundTag();
        for (Map.Entry<UUID, DamageRecord> entry : damageRecords.entrySet()) {
            damageRecordsTag.put(entry.getKey().toString(), entry.getValue().toNbt());
        }
        tag.put("damageRecords", damageRecordsTag);
        return tag;
    }

    public static HistoricalInjuryData load(CompoundTag tag) {
        HistoricalInjuryData data = new HistoricalInjuryData();
        CompoundTag damageRecordsTag = tag.getCompound("damageRecords");
        for (String key : damageRecordsTag.getAllKeys()) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (Exception e) {
                LOGGER.error("Invalid UUID: {}", key, e);
                continue;
            }
            DamageRecord damageRecord = DamageRecord.fromNbt(damageRecordsTag.getCompound(key));
            data.damageRecords.put(uuid, damageRecord);
        }
        return data;
    }

    public Map<UUID, DamageRecord> getDamageRecords() {
        return new HashMap<>(damageRecords);
    }

    public Map<UUID, DamageRecord> getOnlineRecords(MinecraftServer server) {
        return damageRecords.entrySet().stream().filter(e -> server.getPlayerList().getPlayer(e.getKey()) != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public DamageRecord getDamageRecord(UUID uuid) {
        return damageRecords.get(uuid);
    }

    public void addInjury(Player player, DamageData data) {
        damageRecords.computeIfAbsent(player.getUUID(), uuid -> new DamageRecord(uuid, player.getDisplayName())).addInjury(data);
    }

    public DamageData addInjury(Player player, DamageSource source, boolean isDead, float amount, long time) {
        return damageRecords.computeIfAbsent(player.getUUID(), uuid -> new DamageRecord(uuid, player.getDisplayName())).addInjury(source, amount, GlobalPos.of(player.level.dimension(), player.blockPosition()), time, isDead, source.getLocalizedDeathMessage(player));
    }

    public List<DamageData> getInjuries(Player player) {
        return damageRecords.computeIfAbsent(player.getUUID(), uuid -> new DamageRecord(uuid, player.getDisplayName())).getInjuries();
    }

    public static HistoricalInjuryData fromServer(MinecraftServer server) {
        HistoricalInjuryData data = server.overworld().getDataStorage().computeIfAbsent(HistoricalInjuryData::load, HistoricalInjuryData::new, InjuryRecord.MODID);
        data.setDirty();
        return data;
    }
}
