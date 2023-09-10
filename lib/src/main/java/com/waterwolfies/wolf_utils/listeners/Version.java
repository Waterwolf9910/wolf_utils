package com.waterwolfies.wolf_utils.listeners;

import com.waterwolfies.wolf_utils.Plugin;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Version extends BaseListener implements TabCompleter, CommandExecutor {
    
    public Version(Plugin plugin) {
        super(plugin);
        var command = plugin.getCommand("wu_version");
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // if (!sender.hasPermission("wolf_utils.trade__inventory") || !(sender instanceof ConsoleCommandSender)) {
        //     sender.sendMessage(Component.text("You do not have permission to run this command"));
        //     return true;
        // }
        sender.sendMessage(Component.text("Wolf Utils Version: ").append(Component.text(plugin.getPluginMeta().getVersion(), NamedTextColor.GREEN)));
        return true;
    }


}
