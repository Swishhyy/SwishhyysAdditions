package me.swishhyy.swishhyysAdditions.recipe.GCrystal;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import me.swishhyy.swishhyysAdditions.items.GCrystal.Tier1GCrystal;

public class T1GCrystalRecipe {
    private final JavaPlugin plugin;
    private final NamespacedKey recipeKey;

    public T1GCrystalRecipe(JavaPlugin plugin) {
        this.plugin = plugin;
        this.recipeKey = new NamespacedKey(plugin, "tier1_growing_crystal");
    }

    /**
     * Registers the crafting recipe for Tier 1 Growing Crystal
     */
    public void register() {
        // Create the item to be crafted
        ItemStack result = Tier1GCrystal.create();

        // Create the recipe
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, result);

        // Define the shape (3x3 crafting grid)
        recipe.shape("DED", "EGE", "DED");

        // Define the ingredients
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('E', Material.EMERALD);
        recipe.setIngredient('G', Material.GLASS);

        // Register the recipe with the server
        Bukkit.addRecipe(recipe);

        plugin.getLogger().info("Tier 1 Growing Crystal recipe registered");
    }

    /**
     * Unregisters the recipe when needed (e.g., on plugin disable or reload)
     */
    public void unregister() {
        Bukkit.removeRecipe(recipeKey);
    }
}
