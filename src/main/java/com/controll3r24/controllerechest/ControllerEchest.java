package com.controll3r24.controllerechest;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;

public class ControllerEchest extends JavaPlugin {
    private static ControllerEchest instance;
    private DataManager dataManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        dataManager = new DataManager();
        getCommand("ec").setExecutor(new CommandEC());
        getCommand("controlechest").setExecutor(new CommandControlechest());
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        String version = getDescription().getVersion();
        getLogger().log(Level.INFO, ChatColor.translateAlternateColorCodes('&',
            "&7-------------------------------------\n" +
            "&7Hey! welcome to &bControllerEchest!\n" +
            "&7 You are currently using the version " + version + ".\n" +
            "&7-------------------------------------"));
    }

    @Override
    public void onDisable() {
        if (dataManager != null) dataManager.saveAll();
        getLogger().info("ControllerEchest disabled");
    }

    public static ControllerEchest getInstance() { return instance; }
    public DataManager getDataManager() { return dataManager; }
    public ConfigManager getConfigManager() { return configManager; }
}
