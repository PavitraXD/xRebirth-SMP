package com.xrebirth.smp.soulsystem;

/**
 * Represents a player's soul data including current souls and total collected.
 */
public class SoulPlayerData {
    private int souls;
    private int collected;
    
    /**
     * Creates a new SoulPlayerData instance.
     * 
     * @param souls The current number of souls
     * @param collected The total number of souls collected
     */
    public SoulPlayerData(int souls, int collected) {
        this.souls = souls;
        this.collected = collected;
    }
    
    public int getSouls() {
        return souls;
    }
    
    public void setSouls(int souls) {
        this.souls = souls;
    }
    
    public int getCollected() {
        return collected;
    }
    
    public void setCollected(int collected) {
        this.collected = collected;
    }
}