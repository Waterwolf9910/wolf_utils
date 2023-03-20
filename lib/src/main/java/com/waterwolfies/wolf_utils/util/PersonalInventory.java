package com.waterwolfies.wolf_utils.util;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;

public class PersonalInventory implements InventoryHolder {
    
    private final Inventory inventory;
    private static int _id = -1;
    private final int id = ++_id;

    public PersonalInventory(String title) {
        inventory = Bukkit.createInventory(this, InventoryType.ENDER_CHEST, Component.text(title));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public int getID() {
        return id;
    }

}
