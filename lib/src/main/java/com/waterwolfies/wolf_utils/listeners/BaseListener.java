package com.waterwolfies.wolf_utils.listeners;

import com.waterwolfies.wolf_utils.Plugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;

public abstract class BaseListener implements Listener {
    
    protected Plugin plugin;
    protected FileConfiguration config;

    protected BaseListener(Plugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }
}
