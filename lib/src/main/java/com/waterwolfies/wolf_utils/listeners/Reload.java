package com.waterwolfies.wolf_utils.listeners;

import com.waterwolfies.wolf_utils.Plugin;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.kyori.adventure.text.Component;

public class Reload extends BaseListener implements TabCompleter, CommandExecutor {

    public Reload(Plugin plugin) {
        super(plugin);
        var command = plugin.getCommand("wu_reload");
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        sender.sendMessage(Component.text("Reloading config..."));
        // Read the config from disk
        plugin.reloadConfig();
        sender.sendMessage(Component.text("Done"));
        return true;
    }
    
}
