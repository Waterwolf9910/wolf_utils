package com.waterwolfies.wolf_utils;

import com.waterwolfies.wolf_utils.listeners.BaseListener;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

public class Plugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        var pluginManager = Bukkit.getPluginManager();
        Config.setup(this);
        // pluginManager.registerEvents(this, this);
        try (ScanResult result = new ClassGraph().acceptPackages("com.waterwolfies.wolf_utils.listeners").scan()) {
            for (ClassInfo info : result.getSubclasses(BaseListener.class)) {
                try {
                    Class<?> clazz = Class.forName(info.getName());
                    Listener listener = (Listener) clazz.getConstructor(getClass()).newInstance(this);
                    pluginManager.registerEvents(listener, this);
                } catch (Exception e) {/* Catch errors for any invalid plugin */e.printStackTrace();}
            }
        }
    }
    
}
