package com.xrebirth.smp.soulsystem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks player kill cooldowns to prevent soul farming.
 * Includes automatic cleanup to prevent memory leaks.
 */
public class AntiFarmTracker {
    private final Map<UUID, Map<UUID, Long>> lastKillTimes = new HashMap<>();
    
    /**
     * Checks if the killer is in cooldown for killing the victim.
     * 
     * @param killer The UUID of the player attempting the kill
     * @param victim The UUID of the player being killed
     * @param cooldownMs The cooldown duration in milliseconds
     * @return true if the killer is in cooldown, false otherwise
     */
    public boolean isInCooldown(UUID killer, UUID victim, long cooldownMs) {
        Map<UUID, Long> victimMap = lastKillTimes.get(killer);
        if (victimMap == null) {
            return false;
        }
        Long last = victimMap.get(victim);
        if (last == null) {
            return false;
        }
        return System.currentTimeMillis() - last < cooldownMs;
    }
    
    /**
     * Records a kill between two players.
     * 
     * @param killer The UUID of the player who made the kill
     * @param victim The UUID of the player who was killed
     */
    public void recordKill(UUID killer, UUID victim) {
        lastKillTimes.computeIfAbsent(killer, k -> new HashMap<>())
            .put(victim, System.currentTimeMillis());
    }
    
    /**
     * Cleans up old entries to prevent memory leaks.
     * Removes entries older than the specified max age.
     * 
     * @param maxAgeMs Maximum age in milliseconds for entries to be kept
     */
    public void cleanupOldEntries(long maxAgeMs) {
        long currentTime = System.currentTimeMillis();
        long cutoffTime = currentTime - maxAgeMs;
        
        Iterator<Map.Entry<UUID, Map<UUID, Long>>> killerIterator = lastKillTimes.entrySet().iterator();
        
        while (killerIterator.hasNext()) {
            Map.Entry<UUID, Map<UUID, Long>> killerEntry = killerIterator.next();
            Map<UUID, Long> victimMap = killerEntry.getValue();
            
            Iterator<Map.Entry<UUID, Long>> victimIterator = victimMap.entrySet().iterator();
            while (victimIterator.hasNext()) {
                Map.Entry<UUID, Long> victimEntry = victimIterator.next();
                if (victimEntry.getValue() < cutoffTime) {
                    victimIterator.remove();
                }
            }
            
            // Remove the killer entry if it has no victims left
            if (victimMap.isEmpty()) {
                killerIterator.remove();
            }
        }
    }
    
    /**
     * Clears all tracked data. Use with caution.
     */
    public void clear() {
        lastKillTimes.clear();
    }
}