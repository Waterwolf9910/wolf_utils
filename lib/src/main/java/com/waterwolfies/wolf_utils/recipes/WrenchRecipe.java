package com.waterwolfies.wolf_utils.recipes;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class WrenchRecipe extends ShapedRecipe {
    
    public WrenchRecipe(NamespacedKey recipe_key, ItemStack item) {
        super(recipe_key, item);
        this.shape("ees", "i|i", "g|g");
        this.setIngredient('e', Material.EMERALD);
        this.setIngredient('i', Material.IRON_INGOT);
        this.setIngredient('g', Material.GOLD_INGOT);
        this.setIngredient('|', Material.STICK);
        this.setIngredient('s', Material.STRING);
    }
}
