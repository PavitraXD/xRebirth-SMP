package com.xrebirth.smp.soulsystem;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

/**
 * PlaceholderAPI expansion for the Soul System plugin.
 * Provides dynamic placeholders for soul-related statistics.
 */
public class SoulPlaceholderExpansion extends PlaceholderExpansion {
    private final SoulSystemPlugin plugin;
    
    /**
     * Creates a new SoulPlaceholderExpansion instance.
     * 
     * @param plugin The main plugin instance
     */
    public SoulPlaceholderExpansion(SoulSystemPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getIdentifier() {
        return SoulConstants.PLACEHOLDER_NAMESPACE;
    }
    
    @Override
    public String getAuthor() {
        return "xRebirth SMP";
    }
    
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) {
            return "";
        }
        
        switch (params.toLowerCase()) {
            case SoulConstants.PLACEHOLDER_SOULS:
                return String.valueOf(plugin.getSoulManager().getSouls(player.getUniqueId()));
            case SoulConstants.PLACEHOLDER_SOULS_COLLECTED:
                return String.valueOf(plugin.getSoulManager().getCollected(player.getUniqueId()));
            case SoulConstants.PLACEHOLDER_SOULS_MAX:
                return String.valueOf(plugin.getMaxSouls());
            default:
                return "";
        }
    }
}