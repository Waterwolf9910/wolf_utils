package com.waterwolfies.wolf_utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    public static List<String> wrench_blacklist = new ArrayList<>();
    
    Config(Plugin plugin, FileConfiguration config) {
        // config.addDefault("wrench_blacklist", wrench_blacklist);
        plugin.saveDefaultConfig();
        config.options().copyDefaults(true);
        if (!config.getDefaults().getString("version").equals(config.getString("version"))) {
            plugin.saveConfig();
        }
    }

    static {

    }
}
