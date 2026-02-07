package com.xrebirth.smp.soulsystem;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the /souls command and its subcommands.
 * Provides soul management functionality for administrators and players.
 */
public class SoulCommand implements CommandExecutor, TabCompleter {
    private final SoulSystemPlugin plugin;
    
    /**
     * Creates a new SoulCommand instance.
     * 
     * @param plugin The main plugin instance
     */
    public SoulCommand(SoulSystemPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                int souls = plugin.getSoulManager().getSouls(player.getUniqueId());
                sender.sendMessage(plugin.color(
                    plugin.getMessage("check")
                        .replace("%player%", player.getName())
                        .replace("%souls%", String.valueOf(souls))
                ));
            } else {
                sender.sendMessage("Usage: /souls check <player>");
            }
            return true;
        }
        
        String sub = args[0].toLowerCase();
        if (sub.equals("reload")) {
            if (!sender.hasPermission("xrebirthsmp.souls.admin")) {
                sender.sendMessage("No permission.");
                return true;
            }
            plugin.reloadPlugin();
            sender.sendMessage(plugin.color(plugin.getMessage("reload")));
            return true;
        }
        
        if (sub.equals("antifarm")) {
            if (!sender.hasPermission("xrebirthsmp.souls.admin")) {
                sender.sendMessage("No permission.");
                return true;
            }
            boolean current = plugin.isAntiFarmEnabled();
            plugin.setAntiFarmEnabled(!current);
            sender.sendMessage(plugin.color("&aAnti-farm system is now " + (!current ? "&aenabled" : "&cdisabled") + "&a."));
            return true;
        }
        
        if (sub.equals("unban")) {
            if (!sender.hasPermission("xrebirthsmp.souls.admin")) {
                sender.sendMessage("No permission.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("Usage: /souls unban <player>");
                return true;
            }
            String playerName = args[1];
            OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
            plugin.getSoulManager().setSouls(target, 3, false);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pardon " + playerName);
            sender.sendMessage(plugin.color("&aUnbanned " + playerName + " and restored 3 souls."));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage("Usage: /souls " + sub + " <player|item> [player] [amount]");
            return true;
        }
        
        // Handle give command with item type
        if (sub.equals("give")) {
            if (!sender.hasPermission("xrebirthsmp.souls.admin")) {
                sender.sendMessage("No permission.");
                return true;
            }
            
            String type = args[1].toLowerCase();
            
            // Give Soul Revive item
            if (type.equals("reviver")) {
                if (args.length < 3) {
                    sender.sendMessage("Usage: /souls give reviver <player> [amount]");
                    return true;
                }
                
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    sender.sendMessage("Player not online.");
                    return true;
                }
                
                int amount = 1;
                if (args.length >= 4) {
                    try {
                        amount = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("Amount must be a number.");
                        return true;
                    }
                }
                
                for (int i = 0; i < amount; i++) {
                    target.getInventory().addItem(plugin.getReviveManager().createReviveItem());
                }
                
                sender.sendMessage(plugin.color("&aGave " + amount + " Soul Reviver to " + target.getName()));
                return true;
            }
            
            // Give souls
            if (type.equals("soul")) {
                if (args.length < 4) {
                    sender.sendMessage("Usage: /souls give soul <player> <amount>");
                    return true;
                }
                
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
                int amount;
                try {
                    amount = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Amount must be a number.");
                    return true;
                }
                
                amount = Math.max(0, amount);
                if (target.isOnline()) {
                    Player online = target.getPlayer();
                    if (online != null) {
                        plugin.getSoulManager().addSouls(online, amount, false);
                    }
                } else {
                    int current = plugin.getSoulManager().getSouls(target.getUniqueId());
                    plugin.getSoulManager().setSouls(target, current + amount, false);
                }
                sender.sendMessage(plugin.color(plugin.getMessage("give")
                    .replace("%amount%", String.valueOf(amount))
                    .replace("%player%", target.getName() == null ? args[2] : target.getName())
                ));
                return true;
            }
            
            sender.sendMessage("Usage: /souls give <soul|reviver> <player> <amount>");
            return true;
        }
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (sub.equals("check")) {
            int souls = plugin.getSoulManager().getSouls(target.getUniqueId());
            sender.sendMessage(plugin.color(
                plugin.getMessage("check")
                    .replace("%player%", target.getName() == null ? args[1] : target.getName())
                    .replace("%souls%", String.valueOf(souls))
            ));
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage("Usage: /souls " + sub + " <player> <amount>");
            return true;
        }
        
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("Amount must be a number.");
            return true;
        }
        
        amount = Math.max(0, amount);
        switch (sub) {
            case "remove":
                if (!sender.hasPermission("xrebirthsmp.souls.admin")) {
                    sender.sendMessage("No permission.");
                    return true;
                }
                if (target.isOnline()) {
                    Player online = target.getPlayer();
                    if (online != null) {
                        plugin.getSoulManager().removeSouls(online, amount);
                    }
                } else {
                    int current = plugin.getSoulManager().getSouls(target.getUniqueId());
                    plugin.getSoulManager().setSouls(target, current - amount, true);
                }
                sender.sendMessage(plugin.color(plugin.getMessage("remove")
                    .replace("%amount%", String.valueOf(amount))
                    .replace("%player%", target.getName() == null ? args[1] : target.getName())
                ));
                return true;
            case "set":
                if (!sender.hasPermission("xrebirthsmp.souls.admin")) {
                    sender.sendMessage("No permission.");
                    return true;
                }
                plugin.getSoulManager().setSouls(target, amount, true);
                sender.sendMessage(plugin.color(plugin.getMessage("set")
                    .replace("%amount%", String.valueOf(amount))
                    .replace("%player%", target.getName() == null ? args[1] : target.getName())
                ));
                return true;
            default:
                sender.sendMessage("Unknown subcommand.");
                return true;
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>(Arrays.asList("check"));
            if (sender.hasPermission("xrebirthsmp.souls.admin")) {
                subcommands.addAll(Arrays.asList("give", "remove", "set", "reload", "antifarm", "unban"));
            }
            return subcommands.stream()
                .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2 && !args[0].equalsIgnoreCase("reload") && !args[0].equalsIgnoreCase("antifarm")) {
            String subCmd = args[0].toLowerCase();
            if (subCmd.equals("unban")) {
                return Bukkit.getBannedPlayers().stream()
                    .map(OfflinePlayer::getName)
                    .filter(name -> name != null && name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
            if (subCmd.equals("give")) {
                List<String> options = new ArrayList<>(Arrays.asList("reviver", "soul"));
                return options.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 3 && args[0].equalsIgnoreCase("give") && (args[1].equalsIgnoreCase("reviver") || args[1].equalsIgnoreCase("soul"))) {
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        return completions;
    }
}