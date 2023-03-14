package com.waterwolfies.wolf_utils.listeners;

import com.waterwolfies.wolf_utils.Plugin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class SilkSpawner extends BaseListener {

    public SilkSpawner(Plugin plugin, FileConfiguration config) {
        super(plugin, config);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (config.getBoolean("silk_spawner")) {
            return;
        }
        Block block = event.getBlockPlaced();
        ItemStack item = event.getItemInHand();
        if ((block.getState()) instanceof CreatureSpawner blockState) {
            if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(NamespacedKey.fromString("type", plugin))) {
                String entityType = item.getItemMeta().getPersistentDataContainer().get(NamespacedKey.fromString("type", plugin), PersistentDataType.STRING);
                blockState.setSpawnedType(EntityType.valueOf(entityType));
                blockState.update(true);
            }
        }
        // block.getWorld().setBlockData(null, null);
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (config.getBoolean("silk_spawner")) {
            return;
        }
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (block.getType() == Material.SPAWNER && player.getInventory().getItemInMainHand().getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
            dropSpawner(block, player);
            event.setDropItems(false);
            event.setExpToDrop(0);
        }
    }
        
    public void dropSpawner(Block block, Player player) {
        // String string  = ((CreatureSpawner) block.getState()).getSpawnedType().getKey().asString();
        EntityType type = ((CreatureSpawner) block.getState()).getSpawnedType();
        ItemStack item = Bukkit.getItemFactory().createItemStack("minecraft:spawner{BlockEntityTag:{SpawnData:{entity:{id:\"" + type.getKey().asString() + "\"}}}}");
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.SPAWNER);
        List<Component> lore = new ArrayList<>();
        try {
            Component.translatable(type.translationKey(), NamedTextColor.AQUA);
        } catch (Exception e) {}
        meta.lore(lore);
        meta.getPersistentDataContainer().set(NamespacedKey.fromString("type", plugin), PersistentDataType.STRING, type.name());
        item.setItemMeta(meta);
        player.getWorld().dropItem(block.getLocation(), item.ensureServerConversions());
    }   
}
