package com.xrebirth.smp.soulsystem;

import org.bukkit.attribute.Attribute;

/**
 * Constants used throughout the Soul System plugin.
 */
public class SoulConstants {
    
    // Plugin identifiers
    public static final String PLUGIN_NAME = "xRebirthSMPSoulSystem";
    public static final String PLACEHOLDER_NAMESPACE = "xrebirthsouls";
    public static final String SCOREBOARD_OBJECTIVE = "xrebirth_souls";
    
    // Permission nodes
    public static final String PERMISSION_ADMIN = "xrebirthsmp.souls.admin";
    public static final String PERMISSION_GIVE = "xrebirthsmp.souls.give";
    public static final String PERMISSION_CHECK = "xrebirthsmp.souls.check";
    
    // Configuration keys
    public static final String CONFIG_SOULS_STARTING = "souls.starting";
    public static final String CONFIG_SOULS_MAX = "souls.max";
    public static final String CONFIG_SOULS_MIN = "souls.min";
    public static final String CONFIG_SOULS_PVP_ONLY = "souls.pvp_only";
    public static final String CONFIG_SOULS_ANTI_FARM_COOLDOWN = "souls.anti_farm.cooldown_seconds";
    public static final String CONFIG_SOULS_BONUS_ATTACK = "souls.bonus.attack_damage_per_soul_collected";
    public static final String CONFIG_SOULS_BONUS_DAMAGE_TAKEN = "souls.bonus.damage_taken_per_missing_soul";
    
    // Display configuration keys
    public static final String CONFIG_DISPLAY_SCOREBOARD_ENABLED = "display.scoreboard.enabled";
    public static final String CONFIG_DISPLAY_SCOREBOARD_OBJECTIVE = "display.scoreboard.objective";
    public static final String CONFIG_DISPLAY_SCOREBOARD_TITLE = "display.scoreboard.title";
    public static final String CONFIG_DISPLAY_SCOREBOARD_SLOT = "display.scoreboard.slot";
    public static final String CONFIG_DISPLAY_ACTIONBAR_ENABLED = "display.actionbar.enabled";
    public static final String CONFIG_DISPLAY_ACTIONBAR_INTERVAL = "display.actionbar.interval_ticks";
    public static final String CONFIG_DISPLAY_ACTIONBAR_MESSAGE = "display.actionbar.message";
    
    // Ban configuration keys
    public static final String CONFIG_BAN_ENABLED = "ban.enabled";
    public static final String CONFIG_BAN_USE_TEMPORARY = "ban.use_temporary";
    public static final String CONFIG_BAN_COMMAND_PERMANENT = "ban.command_permanent";
    public static final String CONFIG_BAN_COMMAND_TEMPORARY = "ban.command_temporary";
    
    // Message keys
    public static final String MESSAGE_GAINED = "gained";
    public static final String MESSAGE_LOST = "lost";
    public static final String MESSAGE_NO_SOULS_TO_STEAL = "no_souls_to_steal";
    public static final String MESSAGE_ANTI_FARM = "anti_farm";
    public static final String MESSAGE_SOULS_ZERO = "souls_zero";
    public static final String MESSAGE_FULL_SOULS = "full_souls";
    public static final String MESSAGE_CHECK = "check";
    public static final String MESSAGE_GIVE = "give";
    public static final String MESSAGE_REMOVE = "remove";
    public static final String MESSAGE_SET = "set";
    public static final String MESSAGE_RELOAD = "reload";
    
    // Feedback configuration keys
    public static final String CONFIG_FEEDBACK_GAIN_TITLE = "feedback.gain.title";
    public static final String CONFIG_FEEDBACK_GAIN_SUBTITLE = "feedback.gain.subtitle";
    public static final String CONFIG_FEEDBACK_GAIN_SOUND = "feedback.gain.sound";
    public static final String CONFIG_FEEDBACK_GAIN_PARTICLE = "feedback.gain.particle";
    public static final String CONFIG_FEEDBACK_LOSE_TITLE = "feedback.lose.title";
    public static final String CONFIG_FEEDBACK_LOSE_SUBTITLE = "feedback.lose.subtitle";
    public static final String CONFIG_FEEDBACK_LOSE_SOUND = "feedback.lose.sound";
    public static final String CONFIG_FEEDBACK_LOSE_PARTICLE = "feedback.lose.particle";
    
    // Data store paths
    public static final String DATA_PATH_PLAYERS = "players";
    public static final String DATA_PATH_SOULS = "souls";
    public static final String DATA_PATH_COLLECTED = "collected";
    
    // Attribute modifiers
    public static final String ATTACK_BONUS_NAME = "xrebirth_soul_bonus";
    public static final String ATTACK_BONUS_UUID_STRING = "1c70f87a-6df1-4af2-8b33-2f1f6bfe6f18";
    
    // Default values
    public static final int DEFAULT_STARTING_SOULS = 5;
    public static final int DEFAULT_MAX_SOULS = 10;
    public static final int DEFAULT_MIN_SOULS = 0;
    public static final boolean DEFAULT_PVP_ONLY = true;
    public static final long DEFAULT_ANTI_FARM_COOLDOWN_MS = 300000L; // 5 minutes
    public static final double DEFAULT_ATTACK_BONUS_PER_SOUL = 0.5;
    public static final double DEFAULT_DAMAGE_TAKEN_PER_MISSING_SOUL = 0.5;
    
    // Display defaults
    public static final boolean DEFAULT_SCOREBOARD_ENABLED = true;
    public static final String DEFAULT_SCOREBOARD_TITLE = "&6Souls";
    public static final String DEFAULT_SCOREBOARD_SLOT = "SIDEBAR";
    public static final boolean DEFAULT_ACTIONBAR_ENABLED = true;
    public static final int DEFAULT_ACTIONBAR_INTERVAL_TICKS = 40;
    public static final String DEFAULT_ACTIONBAR_MESSAGE = "&6Souls: &e%souls%&7/&e%max%";
    
    // Ban defaults
    public static final boolean DEFAULT_BAN_ENABLED = true;
    public static final boolean DEFAULT_BAN_USE_TEMPORARY = false;
    public static final String DEFAULT_BAN_COMMAND_PERMANENT = "ban %player% Lost all souls.";
    public static final String DEFAULT_BAN_COMMAND_TEMPORARY = "tempban %player% 7d Lost all souls.";
    
    // Placeholder names
    public static final String PLACEHOLDER_SOULS = "souls";
    public static final String PLACEHOLDER_SOULS_COLLECTED = "souls_collected";
    public static final String PLACEHOLDER_SOULS_MAX = "souls_max";
    
    // Event handling
    public static final int TITLE_FADE_IN = 5;
    public static final int TITLE_STAY = 30;
    public static final int TITLE_FADE_OUT = 10;
    public static final float PARTICLE_COUNT = 20.0f;
    public static final float PARTICLE_OFFSET = 0.5f;
    public static final float PARTICLE_SPEED = 0.01f;
    
    // Utility
    public static final String COLOR_CODE = "&";
    public static final String SECTION_SYMBOL = "§";
    public static final String PLAYER_PLACEHOLDER = "%player%";
    public static final String VICTIM_PLACEHOLDER = "%victim%";
    public static final String AMOUNT_PLACEHOLDER = "%amount%";
    public static final String SOULS_PLACEHOLDER = "%souls%";
    public static final String MAX_PLACEHOLDER = "%max%";
    
    // Anti-farm cleanup
    public static final long ANTI_FARM_CLEANUP_INTERVAL_MS = 3600000L; // 1 hour
    
    private SoulConstants() {
        // Prevent instantiation
    }
}