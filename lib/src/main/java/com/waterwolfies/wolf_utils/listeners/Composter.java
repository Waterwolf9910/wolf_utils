package com.waterwolfies.wolf_utils.listeners;

import com.waterwolfies.wolf_utils.Plugin;
import com.waterwolfies.wolf_utils.util.Utils;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Composter extends BaseListener {

    /**
     * <p> Used to track the inventories and the worlds of the composters
     * <p> Format: ${world_name}: { ${location}: ${inventory} }
     */
    public Map<String, Map<Location, Inventory>> composterInventories = new HashMap<>(); 
    public NamespacedKey composter_key = NamespacedKey.fromString("composter", plugin);
    private Gson gson = new GsonBuilder().create();

    public Composter(Plugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onSave(WorldSaveEvent event) {
        if (!config.getBoolean("composter_gui")) {
            return;
        }
        World world = event.getWorld();
        Map<String, Map<Integer, byte[]>> inventories = new HashMap<>();
        String worldName = world.getName();
        composterInventories.computeIfAbsent(worldName, k -> new HashMap<>());
        for (var composters : composterInventories.get(worldName).entrySet()) {
            ItemStack[] _inventory = composters.getValue().getContents();
            Map<Integer, byte[]> inventory = new HashMap<>();
            for (int i = 0; i < _inventory.length; ++i) {
                if (_inventory[i] != null) {
                    inventory.put(i, _inventory[i].serializeAsBytes());
                }
            }
            inventories.put(gson.toJson(composters.getKey().serialize()), inventory);
        }
        world.getPersistentDataContainer().set(composter_key, PersistentDataType.STRING, gson.toJson(inventories));
    }

    @EventHandler
    public void onLoad(WorldLoadEvent event) {
        if (!config.getBoolean("composter_gui")) {
            return;
        }
        World world = event.getWorld();
        String worldName = world.getName();
        if (!world.getPersistentDataContainer().has(composter_key)) {
            return;
        }
        String json = world.getPersistentDataContainer().get(composter_key, PersistentDataType.STRING);
        HashMap<Location, Inventory> composters = new HashMap<>();
        Map<String, Map<Integer, byte[]>> inventories = new Gson().fromJson(
            json,
            new TypeToken<Map<String, Map<Integer, byte[]>>>() {}
        );
        for (var composter : inventories.entrySet()) {
            var inventory = new ComposterInventory();
            for (var cinv : composter.getValue().entrySet()) {
                inventory.getInventory().setItem(cinv.getKey(), ItemStack.deserializeBytes(cinv.getValue()));
            }
            composters.put(Location.deserialize(gson.fromJson(composter.getKey(), new TypeToken<HashMap<String, Object>>() {})), inventory.getInventory());
        }
        composterInventories.put(worldName, composters);
        
    }

    @EventHandler
    public void onBlockDestory(BlockBreakEvent event) {
        if (!config.getBoolean("composter_gui")) {
            return;
        }
        Block block = event.getBlock();
        if (!composterInventories.containsKey(block.getWorld().getName())) {
            return;
        }
        if (!composterInventories.get(block.getWorld().getName()).containsKey(block.getLocation())) {
            return;
        }
        Inventory inv = composterInventories.get(block.getWorld().getName()).get(block.getLocation());
        
        for (ItemStack item : inv.getContents()) {
            if (item == null || item.getType() == Material.GRAY_STAINED_GLASS_PANE || item.getType() == Material.BONE_MEAL) {
                continue;
            }
            block.getWorld().dropItemNaturally(block.getLocation(), item);
        }
        composterInventories.get(block.getWorld().getName()).remove(block.getLocation());
    }

    @EventHandler
    public void onBlockClick(PlayerInteractEvent event) {
        if (!config.getBoolean("composter_gui") || event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getInteractionPoint() == null || event.getPlayer().isSneaking()) {
            return;
        }
        Block block = event.getPlayer().getTargetBlockExact(5);// event.getClickedBlock();

        if (block == null) {
            return;
        }
        if (block.getType() != Material.COMPOSTER) {
            // System.out.println(block.getType());
            return;
        }

        String worldName = block.getWorld().getName();
        composterInventories.computeIfAbsent(worldName, k -> new HashMap<>());
        Map<Location, Inventory> composters = composterInventories.get(worldName);
        composters.computeIfAbsent(block.getLocation(), k -> new ComposterInventory().getInventory());
        Inventory inventory = composters.get(block.getLocation());
        event.getPlayer().openInventory(inventory);
        
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof ComposterInventory) || !config.getBoolean("composter_gui")) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClick() == ClickType.DOUBLE_CLICK || event.isShiftClick()) {
            if (event.getView().getTopInventory().getHolder() instanceof ComposterInventory) {
                event.setCancelled(true);
            }
            return;
        }
        if (event.getClickedInventory() == null || !(event.getClickedInventory().getHolder() instanceof ComposterInventory inventory) || !config.getBoolean("composter_gui")) {
            return;
        }
        ItemStack cursor = event.getCursor();
        int slot = event.getSlot();
        if (slot == -999) {
            return;
        } else if ((slot % 9) == 4 || (!Utils.isCompostable(cursor) && cursor.getType() != Material.AIR)) {
            event.setCancelled(true);
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            onInventoryUpdate(slot, event.getCurrentItem(), event.getWhoClicked().getOpenInventory().getCursor(), inventory);
        });
    }

    /**
     * Checks for inventory updates and manages them
     * @param slot the slot affected
     * @param slotItem The Item Clicked
     * @param cursorItem The Item in the cursor
     * @param composterInventory The composter inventory
     */
    public void onInventoryUpdate(int slot, ItemStack slotItem, ItemStack cursorItem, ComposterInventory composterInventory) {
        Inventory inventory = composterInventory.getInventory();
        if (slotItem == null) {
            if ((slot % 9) > 4 && cursorItem.getType() == Material.BONE_MEAL) {
                int remove = cursorItem.getAmount() * 4;
                for (int _slot = 0; remove > 0 && _slot < 26; ++_slot) {
                    if ((_slot % 9) > 3) {
                        continue;
                    }
                    ItemStack item = inventory.getItem(_slot);
                    if (item == null) {
                        continue;
                    }
                    int amt = Math.max(item.getAmount() - remove, 0);
                    remove -= item.getAmount();
                    if (amt == 0) {
                        inventory.setItem(_slot, new ItemStack(Material.AIR));
                        continue;
                    } else {
                        item.setAmount(amt);
                    }
                    if (remove < 1) {
                        break;
                    }
                    // inventory.setItem(_slot, item);
                    // inventory.getContents();
                }
            } else {
                int amt = cursorItem.getAmount();
                inventory.removeItem(new ItemStack(Material.BONE_MEAL, amt / 4));
                composterInventory.remainder = Math.max(composterInventory.remainder - amt % 4, 0); 
            }
        } else if ((slot % 9) > 4 && cursorItem.getType() == Material.BONE_MEAL) {
            int remove = cursorItem.getAmount() * 4;
            for (int _slot = 0; remove > 0; ++_slot) {
                if ((_slot % 9) > 3) {
                    continue;
                }
                ItemStack item = inventory.getItem(_slot);
                if (item == null) {
                    continue;
                }
                int amt = Math.max(item.getAmount() - remove, 0);
                remove -= item.getAmount();
                if (amt == 0) {
                    inventory.setItem(_slot, new ItemStack(Material.AIR));
                    break;
                }
                item.setAmount(amt);
                // inventory.setItem(_slot, item);
                // inventory.getContents();
            }
        } else {
            int amt = (slotItem.getAmount() + composterInventory.remainder);
            composterInventory.remainder = amt % 4;
            amt /= 4;
            int _slot = inventory.first(Material.BONE_MEAL);
            if (_slot == -1) {
                int add = Math.min(amt, 64);
                amt -= add;
                inventory.setItem(5, new ItemStack(Material.BONE_MEAL, add));
                _slot = 5;
            } else {
                ItemStack item = inventory.getItem(_slot);
                amt += item.getAmount();
                int add = Math.min(amt, 64);
                amt -= add;
                item.setAmount(add);
            }
            while (amt > 1) {
                ++_slot;
                if ((_slot % 9) < 5) {
                    continue;
                }
                ItemStack item = inventory.getItem(_slot);
                if (item == null) {
                    item = new ItemStack(Material.BONE_MEAL);
                    inventory.setItem(_slot, item);
                    item = inventory.getItem(_slot);
                    --amt;
                }
                amt += item.getAmount();
                int add = Math.min(amt, 64);
                amt -= add;
                item.setAmount(add);
            }
        }
    }
    
    protected class ComposterInventory implements InventoryHolder {
        private final Inventory inventory;
        public int remainder;

        public ComposterInventory() {
            inventory = Bukkit.createInventory(this, 27, Component.text("Composter", NamedTextColor.DARK_GREEN));
            ItemStack none = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta noneMeta = none.getItemMeta();
            noneMeta.displayName(Component.text(" "));
            none.setItemMeta(noneMeta);
            int placement = 4;
            while (placement < 27) {
                inventory.setItem(placement, none.ensureServerConversions());
                placement = placement + 9;
            }
        }

        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }
    }

}
