package me.swishhyy.swishhyysAdditions.recipe.GCrystal;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import me.swishhyy.swishhyysAdditions.items.GCrystal.Tier2GCrystal;


public class T2GCrystalRecipe {
    private final JavaPlugin plugin;
    private final NamespacedKey recipeKey;

    public T2GCrystalRecipe(JavaPlugin plugin) {
        this.plugin = plugin;
        this.recipeKey = new NamespacedKey(plugin, "tier2_growing_crystal");
    }

    /**
     * Registers the crafting recipe for Tier 2 Growing Crystal
     */
    public void register() {
        // Create the item to be crafted
        ItemStack result = Tier2GCrystal.create();

        // Create the recipe
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, result);

        // Define the shape (3x3 crafting grid)
        recipe.shape("NDN", "DTD", "NDN");

        // Define the ingredients
        recipe.setIngredient('D', Material.DIAMOND_BLOCK); // Upgraded from diamond
        recipe.setIngredient('N', Material.NETHERITE_INGOT); // More expensive than emerald
        recipe.setIngredient('T', Material.PLAYER_HEAD); // This will be replaced with the Tier 1 crystal

        // Register the recipe with the server
        Bukkit.addRecipe(recipe);

        plugin.getLogger().info("Tier 2 Growing Crystal recipe registered");
    }

    /**
     * Unregisters the recipe when needed (e.g., on plugin disable or reload)
     */
    public void unregister() {
        Bukkit.removeRecipe(recipeKey);
    }
}
