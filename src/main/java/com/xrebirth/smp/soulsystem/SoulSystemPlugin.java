package com.xrebirth.smp.soulsystem;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.Arrays;

import java.util.Locale;

public class SoulSystemPlugin extends JavaPlugin implements Listener {
    private SoulDataStore dataStore;
    private SoulManager soulManager;
    private AntiFarmTracker antiFarmTracker;
    private ReviveManager reviveManager;
    private int startingSouls;
    private int maxSouls;
    private int minSouls;
    private boolean pvpOnly;
    private long antiFarmCooldownMs;
    private boolean antiFarmEnabled;
    private double attackBonusPerSoulCollected;
    private double damageTakenPerMissingSoul;
    private boolean scoreboardEnabled;
    private String scoreboardObjective;
    private String scoreboardTitle;
    private DisplaySlot scoreboardSlot;
    private boolean actionbarEnabled;
    private int actionbarIntervalTicks;
    private String actionbarMessage;
    private boolean banEnabled;
    private boolean banUseTemporary;
    private String banCommandPermanent;
    private String banCommandTemporary;
    private BukkitTask actionbarTask;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();
        
        dataStore = new SoulDataStore(this);
        dataStore.load();
        
        soulManager = new SoulManager(this, dataStore);
        antiFarmTracker = new AntiFarmTracker();
        reviveManager = new ReviveManager(this);
        reviveManager.registerRecipe();
        
        Bukkit.getPluginManager().registerEvents(this, this);
        SoulCommand soulCommand = new SoulCommand(this);
        getCommand("souls").setExecutor(soulCommand);
        getCommand("souls").setTabCompleter(soulCommand);
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            soulManager.initPlayer(player);
        }
        
        if (actionbarEnabled) {
            startActionbarTask();
        }
        
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new SoulPlaceholderExpansion(this).register();
        }
        
        // Start anti-farm cleanup task
        startAntiFarmCleanupTask();
        
        getLogger().info("Soul System plugin enabled successfully");
    }
    
    @Override
    public void onDisable() {
        // Save all player data before shutdown
        if (soulManager != null) {
            soulManager.saveAll();
        }
        
        // Cancel actionbar task safely
        if (actionbarTask != null) {
            try {
                actionbarTask.cancel();
                actionbarTask = null;
            } catch (Exception e) {
                getLogger().warning("Failed to cancel actionbar task: " + e.getMessage());
            }
        }
        
        // Clear anti-farm tracker to free memory
        if (antiFarmTracker != null) {
            antiFarmTracker.clear();
        }
        
        getLogger().info("Soul System plugin disabled successfully");
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        soulManager.initPlayer(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerLogin(org.bukkit.event.player.PlayerLoginEvent event) {
        Player player = event.getPlayer();
        
        if (event.getResult() == org.bukkit.event.player.PlayerLoginEvent.Result.KICK_BANNED) {
            int souls = soulManager.getSouls(player.getUniqueId());
            
            if (souls == 0) {
                // Check if player has revive item in their inventory (from last session)
                // Since player is banned, we need to check offline data
                // For now, we'll handle this through unban command only
                event.setKickMessage(color("&cYou lost all your souls!\n&eUse /souls unban or craft a Soul Revive item\n&eto get unbanned by an admin."));
            }
        }
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        soulManager.savePlayer(event.getPlayer().getUniqueId());
        dataStore.save();
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        if (pvpOnly && killer == null) {
            return;
        }
        
        int preSouls = soulManager.getSouls(victim.getUniqueId());
        if (preSouls <= 0) {
            if (killer != null && !killer.equals(victim)) {
                killer.sendMessage(color(getMessage("no_souls_to_steal")
                    .replace("%victim%", victim.getName())));
            }
            return;
        }
        
        // Check anti-farm first before removing soul
        if (killer != null && !killer.equals(victim)) {
            if (antiFarmEnabled && antiFarmTracker.isInCooldown(killer.getUniqueId(), victim.getUniqueId(), antiFarmCooldownMs)) {
                killer.sendMessage(color(getMessage("anti_farm")
                    .replace("%victim%", victim.getName())));
                return; // Don't remove soul from victim if anti-farm is active
            }
        }
        
        // Remove soul from victim
        int removed = soulManager.removeSouls(victim, 1);
        if (removed > 0) {
            sendLossFeedback(victim);
            victim.sendMessage(color(getMessage("lost")));
            
            // Give to killer if exists and not self-kill
            if (killer != null && !killer.equals(victim)) {
                int added = soulManager.addSouls(killer, 1, true);
                if (added > 0) {
                    antiFarmTracker.recordKill(killer.getUniqueId(), victim.getUniqueId());
                    sendGainFeedback(killer);
                    killer.sendMessage(color(getMessage("gained")
                        .replace("%victim%", victim.getName())));
                } else {
                    // Killer has max souls, drop soul item
                    ItemStack soulItem = createSoulItem();
                    killer.getWorld().dropItemNaturally(killer.getLocation(), soulItem);
                    antiFarmTracker.recordKill(killer.getUniqueId(), victim.getUniqueId());
                }
            }
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Player attacker = getAttackingPlayer(event);
        if (attacker == null) {
            return;
        }
        
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player victim = (Player) event.getEntity();
        int missing = maxSouls - soulManager.getSouls(victim.getUniqueId());
        if (missing <= 0) {
            return;
        }
        
        double extra = missing * damageTakenPerMissingSoul;
        if (extra <= 0) {
            return;
        }
        
        event.setDamage(event.getDamage() + extra);
    }
    
    private Player getAttackingPlayer(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            return (Player) event.getDamager();
        }
        if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                return (Player) projectile.getShooter();
            }
        }
        return null;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        
        // Handle Soul Item
        if (isSoulItem(item)) {
            int currentSouls = soulManager.getSouls(player.getUniqueId());
            
            if (currentSouls >= maxSouls) {
                player.sendMessage(color(getMessage(SoulConstants.MESSAGE_FULL_SOULS)));
                return;
            }
            
            int added = soulManager.addSouls(player, 1, true);
            if (added > 0) {
                item.setAmount(item.getAmount() - 1);
                sendGainFeedback(player);
                player.sendMessage(color("&aYou consumed a soul! Current souls: " + soulManager.getSouls(player.getUniqueId())));
            }
            
            event.setCancelled(true);
            return;
        }
        
        // Handle Revive Item
        if (reviveManager.isReviveItem(item)) {
            reviveManager.openReviveGUI(player);
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        String title = event.getView().getTitle();
        if (!title.equals(color("&6&lRevive Player"))) {
            return;
        }
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.PLAYER_HEAD) {
            return;
        }
        
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        String playerName = color(meta.getDisplayName()).replace("&e", "").replace("§e", "");
        
        // Check if player has revive item in inventory
        ItemStack reviveItem = null;
        for (ItemStack item : player.getInventory().getContents()) {
            if (reviveManager.isReviveItem(item)) {
                reviveItem = item;
                break;
            }
        }
        
        if (reviveItem == null) {
            player.sendMessage(color("&cYou need a Soul Revive item!"));
            player.closeInventory();
            return;
        }
        
        reviveManager.revivePlayer(player, playerName);
        reviveItem.setAmount(reviveItem.getAmount() - 1);
        player.closeInventory();
    }
    
    public void handleSoulsZero(String playerName) {
        if (!banEnabled) {
            return;
        }
        
        Player online = Bukkit.getPlayerExact(playerName);
        if (online != null) {
            online.sendMessage(color(getMessage(SoulConstants.MESSAGE_SOULS_ZERO)));
        }
        
        String command = banUseTemporary ? banCommandTemporary : banCommandPermanent;
        String sanitizedPlayerName = sanitizePlayerName(playerName);
        String filled = command.replace(SoulConstants.PLAYER_PLACEHOLDER, sanitizedPlayerName);
        
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        Bukkit.dispatchCommand(console, filled);
    }
    
    public void reloadPlugin() {
        reloadConfig();
        loadSettings();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            soulManager.initPlayer(player);
        }
        
        if (actionbarEnabled) {
            startActionbarTask();
        }
    }
    
    private void startActionbarTask() {
        if (actionbarTask != null) {
            actionbarTask.cancel();
        }
        
        actionbarTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!actionbarEnabled) {
                    cancel();
                    return;
                }
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    int souls = soulManager.getSouls(player.getUniqueId());
                    int collected = soulManager.getCollected(player.getUniqueId());
                    String msg = actionbarMessage
                        .replace("%souls%", String.valueOf(souls))
                        .replace("%max%", String.valueOf(maxSouls))
                        .replace("%collected%", String.valueOf(collected));
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(color(msg)));
                }
            }
        }.runTaskTimer(this, 20L, actionbarIntervalTicks);
    }
    
    private void sendGainFeedback(Player player) {
        String title = getConfig().getString("feedback.gain.title", "");
        String subtitle = getConfig().getString("feedback.gain.subtitle", "");
        String sound = getConfig().getString("feedback.gain.sound", "");
        String particle = getConfig().getString("feedback.gain.particle", "");
        
        if (!title.isEmpty() || !subtitle.isEmpty()) {
            player.sendTitle(color(title), color(subtitle), 5, 30, 10);
        }
        
        playSound(player, sound);
        spawnParticle(player, particle);
    }
    
    private void sendLossFeedback(Player player) {
        String title = getConfig().getString("feedback.lose.title", "");
        String subtitle = getConfig().getString("feedback.lose.subtitle", "");
        String sound = getConfig().getString("feedback.lose.sound", "");
        String particle = getConfig().getString("feedback.lose.particle", "");
        
        if (!title.isEmpty() || !subtitle.isEmpty()) {
            player.sendTitle(color(title), color(subtitle), 5, 30, 10);
        }
        
        playSound(player, sound);
        spawnParticle(player, particle);
    }
    
    private void playSound(Player player, String soundName) {
        if (soundName == null || soundName.isEmpty()) {
            return;
        }
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase(Locale.ROOT));
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException ignored) {
        }
    }
    
    private void spawnParticle(Player player, String particleName) {
        if (particleName == null || particleName.isEmpty()) {
            return;
        }
        try {
            Particle particle = Particle.valueOf(particleName.toUpperCase(Locale.ROOT));
            player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.01);
        } catch (IllegalArgumentException ignored) {
        }
    }
    
    public void loadSettings() {
        startingSouls = getConfig().getInt("souls.starting", 5);
        maxSouls = getConfig().getInt("souls.max", 10);
        minSouls = getConfig().getInt("souls.min", 0);
        pvpOnly = getConfig().getBoolean("souls.pvp_only", true);
        antiFarmCooldownMs = getConfig().getLong("souls.anti_farm.cooldown_seconds", 300) * 1000L;
        antiFarmEnabled = getConfig().getBoolean("souls.anti_farm.enabled", true);
        attackBonusPerSoulCollected = getConfig().getDouble("souls.bonus.attack_damage_per_soul_collected", 0.5);
        damageTakenPerMissingSoul = getConfig().getDouble("souls.bonus.damage_taken_per_missing_soul", 0.5);
        
        scoreboardEnabled = getConfig().getBoolean("display.scoreboard.enabled", true);
        scoreboardObjective = getConfig().getString("display.scoreboard.objective", "xrebirth_souls");
        scoreboardTitle = getConfig().getString("display.scoreboard.title", "&6Souls");
        String slotName = getConfig().getString("display.scoreboard.slot", "SIDEBAR");
        try {
            scoreboardSlot = DisplaySlot.valueOf(slotName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            scoreboardSlot = DisplaySlot.SIDEBAR;
        }
        
        actionbarEnabled = getConfig().getBoolean("display.actionbar.enabled", true);
        actionbarIntervalTicks = getConfig().getInt("display.actionbar.interval_ticks", 40);
        actionbarMessage = getConfig().getString("display.actionbar.message", "&6Souls: &e%souls%&7/&e%max%");
        
        banEnabled = getConfig().getBoolean("ban.enabled", true);
        banUseTemporary = getConfig().getBoolean("ban.use_temporary", false);
        banCommandPermanent = getConfig().getString("ban.command_permanent", "ban %player% Lost all souls.");
        banCommandTemporary = getConfig().getString("ban.command_temporary", "tempban %player% 7d Lost all souls.");
    }
    
    public String color(String input) {
        return input == null ? "" : input.replace("&", "§");
    }
    
    public String getMessage(String key) {
        ConfigurationSection section = getConfig().getConfigurationSection("messages");
        if (section == null) {
            return "";
        }
        return section.getString(key, "");
    }
    
    public SoulManager getSoulManager() {
        return soulManager;
    }
    
    public int getStartingSouls() {
        return startingSouls;
    }
    
    public int getMaxSouls() {
        return maxSouls;
    }
    
    public int getMinSouls() {
        return minSouls;
    }
    
    public double getAttackBonusPerSoulCollected() {
        return attackBonusPerSoulCollected;
    }
    
    public double getDamageTakenPerMissingSoul() {
        return damageTakenPerMissingSoul;
    }
    
    public boolean isScoreboardEnabled() {
        return scoreboardEnabled;
    }
    
    public String getScoreboardObjective() {
        return scoreboardObjective;
    }
    
    public String getScoreboardTitle() {
        return scoreboardTitle;
    }
    
    public DisplaySlot getScoreboardSlot() {
        return scoreboardSlot;
    }
    
    public boolean isPvpOnly() {
        return pvpOnly;
    }
    
    public boolean isAntiFarmEnabled() {
        return antiFarmEnabled;
    }
    
    public void setAntiFarmEnabled(boolean enabled) {
        this.antiFarmEnabled = enabled;
        getConfig().set("souls.anti_farm.enabled", enabled);
        saveConfig();
    }
    
    public ReviveManager getReviveManager() {
        return reviveManager;
    }
    
    /**
     * Starts the anti-farm cleanup task to prevent memory leaks.
     */
    private void startAntiFarmCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (antiFarmTracker != null) {
                    antiFarmTracker.cleanupOldEntries(SoulConstants.ANTI_FARM_CLEANUP_INTERVAL_MS);
                }
            }
        }.runTaskTimerAsynchronously(this, 20 * 60 * 60, 20 * 60 * 60); // Run every hour
    }
    
    private ItemStack createSoulItem() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color("&6Soul"));
            meta.setLore(Arrays.asList(
                color("&7Right-click to consume"),
                color("&7and gain 1 soul")
            ));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private boolean isSoulItem(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        
        return meta.getDisplayName().equals(color("&6Soul"));
    }
    
    /**
     * Sanitizes a player name to prevent command injection.
     * 
     * @param playerName The player name to sanitize
     * @return The sanitized player name
     */
    private String sanitizePlayerName(String playerName) {
        if (playerName == null) {
            return "unknown";
        }
        // Remove any characters that could be used for command injection
        return playerName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}