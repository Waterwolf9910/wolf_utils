package com.waterwolfies.wolf_utils;

import com.waterwolfies.wolf_utils.listeners.BaseListener;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
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
        try (ScanResult result = new ClassGraph().acceptPackages("com.waterwolfies.wolf_utils.listeners").scan()) { // Scan the listeners package
            for (ClassInfo info : result.getSubclasses(BaseListener.class)) { // Get all subclasses that extend BaseListener
                try {
                    Class<?> clazz = Class.forName(info.getName()); // Get the class using the fully qualified path
                    Listener listener = (Listener) clazz.getConstructor(getClass()).newInstance(this); // Create a new instance of the class
                    pluginManager.registerEvents(listener, this); // register using the instance
                } catch (Exception e) {/* Catch errors for any invalid plugin */e.printStackTrace();}
            }
        }
    }
    
}
