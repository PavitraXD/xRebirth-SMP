package com.xrebirth.smp.soulsystem;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages soul data and player statistics for the Soul System.
 * Handles soul collection, attack bonuses, and scoreboard updates.
 */
public class SoulManager {
    private static final UUID ATTACK_BONUS_UUID = UUID.fromString(SoulConstants.ATTACK_BONUS_UUID_STRING);
    private final SoulSystemPlugin plugin;
    private final SoulDataStore dataStore;
    private final Map<UUID, SoulPlayerData> cache = new HashMap<>();
    
    /**
     * Creates a new SoulManager instance.
     * 
     * @param plugin The main plugin instance
     * @param dataStore The data store for persistent storage
     */
    public SoulManager(SoulSystemPlugin plugin, SoulDataStore dataStore) {
        this.plugin = plugin;
        this.dataStore = dataStore;
    }
    
    public void initPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        SoulPlayerData data = dataStore.get(uuid, plugin.getStartingSouls());
        data.setSouls(clampSouls(data.getSouls()));
        cache.put(uuid, data);
        dataStore.set(uuid, data);
        dataStore.save();
        updateAttackBonus(player);
    }
    
    public void savePlayer(UUID uuid) {
        SoulPlayerData data = cache.get(uuid);
        if (data != null) {
            dataStore.set(uuid, data);
        }
    }
    
    public void saveAll() {
        for (Map.Entry<UUID, SoulPlayerData> entry : cache.entrySet()) {
            dataStore.set(entry.getKey(), entry.getValue());
        }
        dataStore.save();
    }
    
    public int getSouls(UUID uuid) {
        SoulPlayerData data = cache.get(uuid);
        if (data == null) {
            data = dataStore.get(uuid, plugin.getStartingSouls());
            cache.put(uuid, data);
        }
        return data.getSouls();
    }
    
    public int getCollected(UUID uuid) {
        SoulPlayerData data = cache.get(uuid);
        if (data == null) {
            data = dataStore.get(uuid, plugin.getStartingSouls());
            cache.put(uuid, data);
        }
        return data.getCollected();
    }
    
    public int addSouls(Player player, int amount, boolean countCollected) {
        SoulPlayerData data = cache.get(player.getUniqueId());
        if (data == null) {
            initPlayer(player);
            data = cache.get(player.getUniqueId());
        }
        
        int max = plugin.getMaxSouls();
        int before = data.getSouls();
        int actual = Math.max(0, Math.min(amount, max - before));
        
        if (actual <= 0) {
            return 0;
        }
        
        data.setSouls(before + actual);
        if (countCollected) {
            data.setCollected(data.getCollected() + actual);
            updateAttackBonus(player);
        }
        
        dataStore.set(player.getUniqueId(), data);
        dataStore.save();
        return actual;
    }
    
    public int removeSouls(Player player, int amount) {
        SoulPlayerData data = cache.get(player.getUniqueId());
        if (data == null) {
            initPlayer(player);
            data = cache.get(player.getUniqueId());
        }
        
        int min = plugin.getMinSouls();
        int before = data.getSouls();
        int actual = Math.max(0, Math.min(amount, before - min));
        
        if (actual <= 0) {
            return 0;
        }
        
        int after = before - actual;
        data.setSouls(after);
        dataStore.set(player.getUniqueId(), data);
        dataStore.save();
        
        if (after == 0 && before > 0) {
            plugin.handleSoulsZero(player.getName());
        }
        
        return actual;
    }
    
    public void setSouls(OfflinePlayer player, int newSouls, boolean allowBan) {
        UUID uuid = player.getUniqueId();
        SoulPlayerData data = cache.get(uuid);
        if (data == null) {
            data = dataStore.get(uuid, plugin.getStartingSouls());
            cache.put(uuid, data);
        }
        
        int before = data.getSouls();
        int clamped = clampSouls(newSouls);
        data.setSouls(clamped);
        
        if (player.isOnline()) {
            Player online = player.getPlayer();
            if (online != null) {
                updateAttackBonus(online);
            }
        }
        
        dataStore.set(uuid, data);
        dataStore.save();
        
        if (allowBan && clamped == 0 && before > 0) {
            plugin.handleSoulsZero(player.getName());
        }
    }
    
    public void updateAttackBonus(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attribute == null) {
            return;
        }
        
        attribute.getModifiers().stream()
            .filter(mod -> mod.getUniqueId().equals(ATTACK_BONUS_UUID))
            .forEach(attribute::removeModifier);
        
        double bonus = getCollected(player.getUniqueId()) * plugin.getAttackBonusPerSoulCollected();
        if (bonus > 0) {
            AttributeModifier modifier = new AttributeModifier(
                ATTACK_BONUS_UUID,
                "xrebirth_soul_bonus",
                bonus,
                AttributeModifier.Operation.ADD_NUMBER
            );
            attribute.addModifier(modifier);
        }
    }
    
    private int clampSouls(int souls) {
        int min = plugin.getMinSouls();
        int max = plugin.getMaxSouls();
        if (souls < min) return min;
        if (souls > max) return max;
        return souls;
    }
}