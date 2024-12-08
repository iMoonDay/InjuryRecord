package com.imoonday.injuryrecord.data;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * 所有受伤记录的数据类
 */
public class DamageRecord {

    private static final int MAX_DATA_COUNT = 1000;
    private final UUID uuid;
    private final Component name;
    private final List<DamageData> injuries = new ArrayList<>();
    private boolean isIncomplete;

    public DamageRecord(UUID uuid, Component name) {
        this.uuid = uuid;
        this.name = name;
    }

    public DamageRecord(UUID uuid, Component name, List<DamageData> injuries) {
        this.uuid = uuid;
        this.name = name;
        this.injuries.addAll(injuries);
        handleUpLimit(MAX_DATA_COUNT);
    }

    public UUID getUuid() {
        return uuid;
    }

    public Component getName() {
        return name;
    }

    public List<DamageData> getInjuries() {
        return new ArrayList<>(injuries);
    }

    public void addInjury(DamageData injury) {
        injuries.add(injury);
        handleUpLimit(MAX_DATA_COUNT);
    }

    public DamageData addInjury(DamageSource source, float amount, GlobalPos location, long time, boolean isDead, @Nullable Component deathMessage) {
        DamageData data = new DamageData(source, amount, location, time, isDead, deathMessage);
        injuries.add(data);
        handleUpLimit(MAX_DATA_COUNT);
        return data;
    }

    public void setInjuries(List<DamageData> injuries) {
        this.injuries.clear();
        this.injuries.addAll(injuries);
    }

    private void handleUpLimit(int limit) {
        if (limit <= 0 || injuries.size() <= limit) {
            return;
        }
        List<DamageData> list = injuries.stream().sorted(Comparator.comparing(DamageData::getTime).reversed()).toList();
        int size = list.size();
        if (limit > size) {
            limit = size;
        }
        list = list.subList(0, limit);
        setInjuries(list);
    }

    public boolean isIncomplete() {
        return isIncomplete;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("uuid", uuid);
        tag.putString("name", Component.Serializer.toJson(name));
        ListTag listTag = new ListTag();
        for (DamageData injury : injuries) {
            listTag.add(injury.toNbt());
        }
        tag.put("injuries", listTag);
        tag.putBoolean("isIncomplete", false);
        return tag;
    }

    public CompoundTag toLimitedNbt(int limit) {
        if (limit <= 0 || injuries.size() <= limit) {
            return toNbt();
        }
        List<DamageData> list = injuries.stream().sorted(Comparator.comparing(DamageData::getTime)).toList();
        int size = list.size();
        if (limit > size) {
            limit = size;
        }
        list = list.subList(size - limit, size);
        CompoundTag tag = new CompoundTag();
        tag.putUUID("uuid", uuid);
        tag.putString("name", Component.Serializer.toJson(name));
        ListTag listTag = new ListTag();
        for (DamageData injury : list) {
            listTag.add(injury.toNbt());
        }
        tag.put("injuries", listTag);
        tag.putBoolean("isIncomplete", true);
        return tag;
    }

    public static DamageRecord fromNbt(CompoundTag tag) {
        if (tag == null || !tag.contains("uuid")) return null;

        MutableComponent name = Component.Serializer.fromJson(tag.getString("name"));
        if (name == null) name = Component.empty();
        DamageRecord record = new DamageRecord(tag.getUUID("uuid"), name);
        ListTag listTag = tag.getList("injuries", Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag injuryTag = listTag.getCompound(i);
            DamageData injury = DamageData.fromNbt(injuryTag);
            record.addInjury(injury);
        }
        record.isIncomplete = tag.getBoolean("isIncomplete");
        return record;
    }
}
