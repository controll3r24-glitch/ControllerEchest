package com.controll3r24.controllerechest;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private final Map<String, String> messageCache = new HashMap<>();
    private String language;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    private void loadConfigs() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        language = config.getString("language", "en");
        loadMessages();
    }

    private void loadMessages() {
        String langFile = "messages_" + language + ".yml";
        File messagesFile = new File(plugin.getDataFolder(), langFile);
        if (!messagesFile.exists()) {
            plugin.saveResource(langFile, false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        messageCache.clear();
        for (String key : messages.getKeys(true)) {
            if (messages.isString(key)) {
                messageCache.put(key, ChatColor.translateAlternateColorCodes('&', messages.getString(key)));
            }
        }
    }

    public void reload() {
        loadConfigs();
    }

    public String getMessage(String path) {
        return messageCache.getOrDefault(path, "&cMissing: " + path);
    }

    public Material getEmptyGlass() {
        String mat = config.getString("glass.empty", "LIME_STAINED_GLASS_PANE");
        Material m = Material.getMaterial(mat.toUpperCase());
        return m != null ? m : Material.LIME_STAINED_GLASS_PANE;
    }

    public Material getHalfGlass() {
        String mat = config.getString("glass.half", "YELLOW_STAINED_GLASS_PANE");
        Material m = Material.getMaterial(mat.toUpperCase());
        return m != null ? m : Material.YELLOW_STAINED_GLASS_PANE;
    }

    public Material getFullGlass() {
        String mat = config.getString("glass.full", "RED_STAINED_GLASS_PANE");
        Material m = Material.getMaterial(mat.toUpperCase());
        return m != null ? m : Material.RED_STAINED_GLASS_PANE;
    }

    public String getMenuTitle() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("gui.menu-title", "&1Enderchest of %player%"));
    }

    public String getChestTitle() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("gui.chest-title", "&5EnderChest #%number%"));
    }

    public String getAdminChestTitle() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("gui.admin-chest-title", "&5EnderChest #%number% (%player%)"));
    }

    public int getDefaultChestSize() {
        int size = config.getInt("chest.default-size", 27);
        return Math.max(9, Math.min(54, size));
    }

    public int getStaffChestSize() {
        int size = config.getInt("chest.staff-size", 54);
        return Math.max(9, Math.min(54, size));
    }

    public String getGlassName() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("gui.glass-name", "&6Enderchest %number%  (%occupied% / %max%)"));
    }
}
