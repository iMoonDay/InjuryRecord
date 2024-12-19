package com.imoonday.injuryrecord.data;

import com.mojang.logging.LogUtils;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
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
    private final ItemStack attackerMainHandItem;
    @Nullable
    private final ItemStack attackerOffHandItem;
    @Nullable
    private final Component directEntityName;
    @Nullable
    private final ItemStack directEntityMainHandItem;
    @Nullable
    private final ItemStack directEntityOffHandItem;
    private final boolean isRemote;
    private final String msgId;
    private final float amount;
    private final GlobalPos location;
    private final long time;
    private final boolean isDead;
    @Nullable
    private final Component deathMessage;

    public DamageData(DamageSource source, float amount, GlobalPos location, long time, boolean isDead, @Nullable Component deathMessage) {
        Entity entity = source.getEntity();
        if (entity != null) {
            this.attackerName = entity.getDisplayName();
        } else {
            this.attackerName = null;
        }
        if (entity instanceof LivingEntity living) {
            this.attackerMainHandItem = living.getMainHandItem().copy();
            this.attackerOffHandItem = living.getOffhandItem().copy();
        } else {
            this.attackerMainHandItem = null;
            this.attackerOffHandItem = null;
        }
        Entity directEntity = source.getDirectEntity();
        if (directEntity != null) {
            this.directEntityName = directEntity.getDisplayName();
        } else {
            this.directEntityName = null;
        }
        if (directEntity instanceof LivingEntity living) {
            this.directEntityMainHandItem = living.getMainHandItem().copy();
            this.directEntityOffHandItem = living.getOffhandItem().copy();
        } else {
            this.directEntityMainHandItem = null;
            this.directEntityOffHandItem = null;
        }
        this.isRemote = entity != directEntity;
        this.msgId = source.getMsgId();
        this.amount = amount;
        this.location = location;
        this.time = time;
        this.isDead = isDead;
        this.deathMessage = deathMessage;
    }

    public DamageData(@Nullable Component attackerName, @Nullable ItemStack attackerMainHandItem, @Nullable ItemStack attackerOffHandItem, @Nullable Component directEntityName, @Nullable ItemStack directEntityMainHandItem, @Nullable ItemStack directEntityOffHandItem, boolean isRemote, String msgId, float amount, GlobalPos location, long time, boolean isDead, @Nullable Component deathMessage) {
        this.attackerName = attackerName;
        this.attackerMainHandItem = attackerMainHandItem;
        this.attackerOffHandItem = attackerOffHandItem;
        this.directEntityName = directEntityName;
        this.directEntityMainHandItem = directEntityMainHandItem;
        this.directEntityOffHandItem = directEntityOffHandItem;
        this.isRemote = isRemote;
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
        if (attackerMainHandItem != null) {
            tag.put("attackerMainHandItem", attackerMainHandItem.save(new CompoundTag()));
        }
        if (attackerOffHandItem != null) {
            tag.put("attackerOffHandItem", attackerOffHandItem.save(new CompoundTag()));
        }
        if (directEntityName != null) {
            tag.putString("directEntityName", Component.Serializer.toJson(directEntityName));
        }
        if (directEntityMainHandItem != null) {
            tag.put("directEntityMainHandItem", directEntityMainHandItem.save(new CompoundTag()));
        }
        if (directEntityOffHandItem != null) {
            tag.put("directEntityOffHandItem", directEntityOffHandItem.save(new CompoundTag()));
        }
        tag.putBoolean("isRemote", isRemote);
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

    public @Nullable ItemStack getAttackerMainHandItem() {
        return attackerMainHandItem;
    }

    public @Nullable ItemStack getAttackerOffHandItem() {
        return attackerOffHandItem;
    }

    public @Nullable Component getDirectEntityName() {
        return directEntityName;
    }

    public @Nullable ItemStack getDirectEntityMainHandItem() {
        return directEntityMainHandItem;
    }

    public @Nullable ItemStack getDirectEntityOffHandItem() {
        return directEntityOffHandItem;
    }

    public boolean isRemote() {
        return isRemote;
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
        ItemStack attackerMainHandItem = tag.contains("attackerMainHandItem") ? ItemStack.of(tag.getCompound("attackerMainHandItem")) : null;
        ItemStack attackerOffHandItem = tag.contains("attackerOffHandItem") ? ItemStack.of(tag.getCompound("attackerOffHandItem")) : null;
        Component directEntityName = tag.contains("directEntityName") ? Component.Serializer.fromJson(tag.getString("directEntityName")) : null;
        ItemStack directEntityMainHandItem = tag.contains("directEntityMainHandItem") ? ItemStack.of(tag.getCompound("directEntityMainHandItem")) : null;
        ItemStack directEntityOffHandItem = tag.contains("directEntityOffHandItem") ? ItemStack.of(tag.getCompound("directEntityOffHandItem")) : null;
        boolean isRemote = tag.getBoolean("isRemote");
        String msgId = tag.getString("msgId");
        float amount = tag.getFloat("amount");
        GlobalPos location = GlobalPos.CODEC.parse(NbtOps.INSTANCE, tag.get("location")).resultOrPartial(LOGGER::error).orElse(null);
        long time = tag.getLong("time");
        boolean isDead = tag.getBoolean("isDead");
        Component deathMessage = tag.contains("deathMessage") ? Component.Serializer.fromJson(tag.getString("deathMessage")) : null;
        return new DamageData(attackerName, attackerMainHandItem, attackerOffHandItem, directEntityName, directEntityMainHandItem, directEntityOffHandItem, isRemote, msgId, amount, location, time, isDead, deathMessage);
    }
}
