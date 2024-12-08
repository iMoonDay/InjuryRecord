package com.imoonday.injuryrecord.data;

import com.mojang.logging.LogUtils;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 单次受伤的数据类
 */
public class DamageData {

    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private final Component attackerName;
    @Nullable
    private final Component directEntityName;
    private final String msgId;
    private final float amount;
    private final GlobalPos location;
    private final long time;
    private final boolean isDead;
    @Nullable
    private final Component deathMessage;

    public DamageData(DamageSource source, float amount, GlobalPos location, long time, boolean isDead, @Nullable Component deathMessage) {
        Entity entity = source.getEntity();
        this.attackerName = entity != null ? entity.getDisplayName() : null;
        Entity directEntity = source.getDirectEntity();
        this.directEntityName = directEntity != null ? directEntity.getDisplayName() : null;
        this.msgId = source.getMsgId();
        this.amount = amount;
        this.location = location;
        this.time = time;
        this.isDead = isDead;
        this.deathMessage = deathMessage;
    }

    public DamageData(@Nullable Component attackerName, @Nullable Component directEntityName, String msgId, float amount, GlobalPos location, long time, boolean isDead, @Nullable Component deathMessage) {
        this.attackerName = attackerName;
        this.directEntityName = directEntityName;
        this.msgId = msgId;
        this.amount = amount;
        this.location = location;
        this.time = time;
        this.isDead = isDead;
        this.deathMessage = deathMessage;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        if (attackerName != null) {
            tag.putString("attackerName", Component.Serializer.toJson(attackerName));
        }
        if (directEntityName != null) {
            tag.putString("directEntityName", Component.Serializer.toJson(directEntityName));
        }
        tag.putString("msgId", msgId);
        tag.putFloat("amount", amount);
        GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, location).resultOrPartial(LOGGER::error).ifPresent(locationTag -> tag.put("location", locationTag));
        tag.putLong("time", time);
        tag.putBoolean("isDead", isDead);
        if (deathMessage != null) {
            tag.putString("deathMessage", Component.Serializer.toJson(deathMessage));
        }
        return tag;
    }

    public @Nullable Component getAttackerName() {
        return attackerName;
    }

    public @Nullable Component getDirectEntityName() {
        return directEntityName;
    }

    public String getMsgId() {
        return msgId;
    }

    public float getAmount() {
        return amount;
    }

    public GlobalPos getLocation() {
        return location;
    }

    public long getTime() {
        return time;
    }

    public String getFormattedTime() {
        Date date = new Date(time);
        SimpleDateFormat ft = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return ft.format(date);
    }

    public boolean isDead() {
        return isDead;
    }

    @Nullable
    public Component getDeathMessage() {
        return deathMessage;
    }

    public static DamageData fromNbt(CompoundTag tag) {
        Component attackerName = tag.contains("attackerName") ? Component.Serializer.fromJson(tag.getString("attackerName")) : null;
        Component directEntityName = tag.contains("directEntityName") ? Component.Serializer.fromJson(tag.getString("directEntityName")) : null;
        String msgId = tag.getString("msgId");
        float amount = tag.getFloat("amount");
        GlobalPos location = GlobalPos.CODEC.parse(NbtOps.INSTANCE, tag.get("location")).resultOrPartial(LOGGER::error).orElse(null);
        long time = tag.getLong("time");
        boolean isDead = tag.getBoolean("isDead");
        Component deathMessage = tag.contains("deathMessage") ? Component.Serializer.fromJson(tag.getString("deathMessage")) : null;
        return new DamageData(attackerName, directEntityName, msgId, amount, location, time, isDead, deathMessage);
    }
}
