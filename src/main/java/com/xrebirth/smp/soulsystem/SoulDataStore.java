package com.xrebirth.smp.soulsystem;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manages persistent storage of soul data for players.
 * Handles loading, saving, and retrieving player soul information.
 */
public class SoulDataStore {
    private final File file;
    private FileConfiguration config;
    private final JavaPlugin plugin;
    
    /**
     * Creates a new SoulDataStore instance.
     * 
     * @param plugin The plugin instance
     */
    public SoulDataStore(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
    }
    
    /**
     * Loads the data file from disk.
     * Creates the file if it doesn't exist.
     */
    public void load() {
        try {
            if (!file.exists()) {
                File parent = file.getParentFile();
                if (!parent.exists() && !parent.mkdirs()) {
                    plugin.getLogger().warning("Failed to create data directory");
                }
                if (!file.createNewFile()) {
                    plugin.getLogger().warning("Failed to create data file");
                    return;
                }
            }
            this.config = YamlConfiguration.loadConfiguration(file);
            plugin.getLogger().info("Soul data loaded successfully");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load soul data", e);
        }
    }
    
    /**
     * Saves the current data to disk.
     */
    public void save() {
        if (config == null) {
            plugin.getLogger().warning("Attempted to save null configuration");
            return;
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save soul data", e);
        }
    }
    
    /**
     * Retrieves soul data for a player.
     * Creates default data if the player doesn't exist.
     * 
     * @param uuid The UUID of the player
     * @param startingSouls The default number of souls for new players
     * @return The player's soul data
     */
    public SoulPlayerData get(UUID uuid, int startingSouls) {
        if (uuid == null) {
            plugin.getLogger().warning("Attempted to get data for null UUID");
            return new SoulPlayerData(startingSouls, 0);
        }
        
        if (config == null) {
            plugin.getLogger().warning("Configuration is null, returning default data");
            return new SoulPlayerData(startingSouls, 0);
        }
        
        String path = "players." + uuid;
        if (!config.contains(path + ".souls")) {
            config.set(path + ".souls", startingSouls);
            config.set(path + ".collected", 0);
            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save new player data", e);
            }
        }
        
        int souls = config.getInt(path + ".souls", startingSouls);
        int collected = config.getInt(path + ".collected", 0);
        return new SoulPlayerData(souls, collected);
    }
    
    /**
     * Sets soul data for a player.
     * 
     * @param uuid The UUID of the player
     * @param data The soul data to set
     */
    public void set(UUID uuid, SoulPlayerData data) {
        if (uuid == null || data == null) {
            plugin.getLogger().warning("Attempted to set data with null UUID or data");
            return;
        }
        
        if (config == null) {
            plugin.getLogger().warning("Configuration is null, cannot set data");
            return;
        }
        
        String path = "players." + uuid;
        config.set(path + ".souls", data.getSouls());
        config.set(path + ".collected", data.getCollected());
    }
    
    /**
     * Checks if the data store has been initialized.
     * 
     * @return true if the configuration is loaded, false otherwise
     */
    public boolean isLoaded() {
        return config != null;
    }
    
    /**
     * Gets the underlying configuration object.
     * 
     * @return The FileConfiguration instance
     */
    public FileConfiguration getConfig() {
        return config;
    }
}