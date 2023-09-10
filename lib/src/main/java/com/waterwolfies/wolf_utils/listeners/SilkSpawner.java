package com.waterwolfies.wolf_utils.listeners;

import com.waterwolfies.wolf_utils.Plugin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
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

public class SilkSpawner extends BaseListener {

    public SilkSpawner(Plugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!config.getBoolean("silk_spawner")) {
            return;
        }
        Block block = event.getBlockPlaced();
        ItemStack item = event.getItemInHand();

        // Check if the block placed is a spawner
        if (!((block.getState()) instanceof CreatureSpawner blockState)) {
            return;
        }
        
        // Check if the spawner has the custom meta data
        if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(NamespacedKey.fromString("type", plugin))) {
            String entityType = item.getItemMeta().getPersistentDataContainer().get(NamespacedKey.fromString("type", plugin), PersistentDataType.STRING);
            blockState.setSpawnedType(EntityType.valueOf(entityType));
            blockState.update(true);
        }
        // block.getWorld().setBlockData(null, null);
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!config.getBoolean("silk_spawner")) {
            return;
        }
        Block block = event.getBlock();
        Player player = event.getPlayer();
        // Check if the block breaking is and spawner and the player is holding a silk touch item
        if (block.getType() == Material.SPAWNER && player.getInventory().getItemInMainHand().getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
            // Drop the spawner with the entity type attached
            dropSpawner(block, player);
            // Disable xp drop from breaking the spawner
            event.setDropItems(false);
            event.setExpToDrop(0);
        }
    }
        
    public void dropSpawner(Block block, Player player) {
        // String string  = ((CreatureSpawner) block.getState()).getSpawnedType().getKey().asString();
        // Get the entity type of the spawner
        EntityType type = ((CreatureSpawner) block.getState()).getSpawnedType();
        // Create a spawner with the entity enclosed
        ItemStack item = Bukkit.getItemFactory().createItemStack("minecraft:spawner{BlockEntityTag:{SpawnData:{entity:{id:\"" + type.getKey().asString() + "\"}}}}");
        ItemMeta meta = item.getItemMeta();//Bukkit.getItemFactory().getItemMeta(Material.SPAWNER);
        List<Component> lore = new ArrayList<>();
        // try {
        //     lore.add(Component.translatable(type.translationKey(), NamedTextColor.AQUA));
        // } catch (Exception e) {/* Catch incase custom entity */}
        meta.lore(lore);
        // Write the name of the entity type to persistent storage
        meta.getPersistentDataContainer().set(NamespacedKey.fromString("type", plugin), PersistentDataType.STRING, type.name());
        // Write the item metedata to the item
        item.setItemMeta(meta);
        // Spawn the item where the spawner was broken
        player.getWorld().dropItem(block.getLocation(), item.ensureServerConversions());
    }   
}
