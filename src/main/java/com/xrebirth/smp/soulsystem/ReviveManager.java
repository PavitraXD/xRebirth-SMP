package com.xrebirth.smp.soulsystem;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.Set;

public class ReviveManager {
    private final SoulSystemPlugin plugin;
    
    public ReviveManager(SoulSystemPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void registerRecipe() {
        ItemStack reviveItem = createReviveItem();
        NamespacedKey key = new NamespacedKey(plugin, "soul_revive");
        
        ShapedRecipe recipe = new ShapedRecipe(key, reviveItem);
        recipe.shape("DND", "GTG", "DND");
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('G', Material.GOLD_BLOCK);
        recipe.setIngredient('T', Material.TOTEM_OF_UNDYING);
        
        Bukkit.addRecipe(recipe);
    }
    
    public ItemStack createReviveItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.color("&6&lSoul Reviver"));
            meta.setLore(Arrays.asList(
                plugin.color("&7Right-click to open revive menu"),
                plugin.color("&7Select a banned player to revive"),
                plugin.color("&eRestores 3 souls to the player")
            ));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    public boolean isReviveItem(ItemStack item) {
        if (item == null || item.getType() != Material.NETHER_STAR) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        
        return meta.getDisplayName().equals(plugin.color("&6&lSoul Reviver"));
    }
    
    public void openReviveGUI(Player player) {
        Set<OfflinePlayer> bannedPlayers = Bukkit.getBannedPlayers();
        
        if (bannedPlayers.isEmpty()) {
            player.sendMessage(plugin.color("&cNo banned players found!"));
            return;
        }
        
        int size = Math.min(54, ((bannedPlayers.size() + 8) / 9) * 9);
        Inventory gui = Bukkit.createInventory(null, size, plugin.color("&6&lRevive Player"));
        
        int slot = 0;
        for (OfflinePlayer banned : bannedPlayers) {
            if (slot >= size) break;
            
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            if (skullMeta != null) {
                skullMeta.setOwningPlayer(banned);
                skullMeta.setDisplayName(plugin.color("&e" + (banned.getName() != null ? banned.getName() : "Unknown")));
                skullMeta.setLore(Arrays.asList(
                    plugin.color("&7Click to revive this player"),
                    plugin.color("&aRestores 3 souls")
                ));
                skull.setItemMeta(skullMeta);
            }
            
            gui.setItem(slot++, skull);
        }
        
        player.openInventory(gui);
    }
    
    public void revivePlayer(Player reviver, String playerName) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        plugin.getSoulManager().setSouls(target, 3, false);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pardon " + playerName);
        reviver.sendMessage(plugin.color("&a" + playerName + " unbanned."));
    }
}
