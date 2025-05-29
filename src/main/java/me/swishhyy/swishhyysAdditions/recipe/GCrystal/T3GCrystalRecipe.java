package me.swishhyy.swishhyysAdditions.recipe.GCrystal;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import me.swishhyy.swishhyysAdditions.items.GCrystal.Tier3GCrystal;

public class T3GCrystalRecipe {
    private final JavaPlugin plugin;
    private final NamespacedKey recipeKey;

    public T3GCrystalRecipe(JavaPlugin plugin) {
        this.plugin = plugin;
        this.recipeKey = new NamespacedKey(plugin, "tier3_growing_crystal");
    }

    /**
     * Registers the crafting recipe for Tier 3 Growing Crystal
     */
    public void register() {
        // Create the item to be crafted
        ItemStack result = Tier3GCrystal.create();

        // Create the recipe
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, result);

        // Define the shape (3x3 crafting grid)
        recipe.shape("BNB", "NTN", "BNB");

        // Define the ingredients
        recipe.setIngredient('B', Material.NETHERITE_BLOCK); // Significantly more expensive
        recipe.setIngredient('N', Material.NETHER_STAR); // Very expensive item
        recipe.setIngredient('T', Material.PLAYER_HEAD); // This will be replaced with the Tier 2 crystal

        // Register the recipe with the server
        Bukkit.addRecipe(recipe);

        plugin.getLogger().info("Tier 3 Growing Crystal recipe registered");
    }

    /**
     * Unregisters the recipe when needed (e.g., on plugin disable or reload)
     */
    public void unregister() {
        Bukkit.removeRecipe(recipeKey);
    }
}
