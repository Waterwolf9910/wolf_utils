package com.waterwolfies.wolf_utils;

import org.bukkit.configuration.file.FileConfiguration;

public class Config {
    
    public static void setup(Plugin plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        if (checkSave(config)) {
            config.options().copyDefaults(true);
            config.set("version", config.getDefaults().get("version"));
            plugin.saveConfig();
            plugin.reloadConfig();
        }
    }

    /**
     * @return True when the config version is the less than then default config
     */
    protected static boolean checkSave(FileConfiguration config) {
        String curVer = config.getString("version");
        int majorCur = Integer.parseInt(((Character) curVer.charAt(0)).toString());
        float minorPatchCur = Float.parseFloat(curVer.substring(curVer.indexOf(".") + 1));
        String thisVer = config.getDefaults().getString("version");
        int majorThis = Integer.parseInt(((Character)thisVer.charAt(0)).toString());
        float minorPatchThis = Float.parseFloat(thisVer.substring(thisVer.indexOf(".") + 1));

        if (majorCur < majorThis) {
            return true;
        }

        return minorPatchCur < minorPatchThis;
    }

    private Config() {}
}
