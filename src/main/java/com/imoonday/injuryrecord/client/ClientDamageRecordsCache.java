package com.imoonday.injuryrecord.client;

import com.imoonday.injuryrecord.data.DamageData;
import com.imoonday.injuryrecord.data.DamageRecord;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 客户端缓存的伤害记录
 */
public class ClientDamageRecordsCache {

    public static final ClientDamageRecordsCache INSTANCE = new ClientDamageRecordsCache();

    private final Map<UUID, DamageRecord> clientDamageRecords = new HashMap<>();

    public void addRecord(DamageRecord damageRecord) {
        clientDamageRecords.put(damageRecord.getUuid(), damageRecord);
    }

    public Map<UUID, DamageRecord> getRecords() {
        return new HashMap<>(clientDamageRecords);
    }

    public void clearRecords() {
        clientDamageRecords.clear();
    }

    public void setRecords(Map<UUID, DamageRecord> damageRecords) {
        clientDamageRecords.clear();
        clientDamageRecords.putAll(damageRecords);
    }

    public void updateRecord(UUID uuid, DamageRecord damageRecord) {
        clientDamageRecords.put(uuid, damageRecord);
    }

    public boolean addInjury(UUID uuid, DamageData data) {
        DamageRecord record = clientDamageRecords.get(uuid);
        if (record != null) {
            record.addInjury(data);
            return true;
        } else {
            ClientUtils.requestRecords(uuid, true);
            return false;
        }
    }
}
