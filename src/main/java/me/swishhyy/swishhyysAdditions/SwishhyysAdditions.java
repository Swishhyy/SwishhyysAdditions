package me.swishhyy.swishhyysAdditions;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import me.arcaniax.hdb.api.DatabaseLoadEvent;
import me.swishhyy.swishhyysAdditions.listeners.CrystalListener.Tier1GCrystalListener;
import me.swishhyy.swishhyysAdditions.listeners.CrystalListener.Tier2GCrystalListener;
import me.swishhyy.swishhyysAdditions.listeners.CrystalListener.Tier3GCrystalListener;
import me.swishhyy.swishhyysAdditions.commands.CommandLoader;
import me.swishhyy.swishhyysAdditions.recipe.GCrystal.Tier1GCrystalRecipe;
import me.swishhyy.swishhyysAdditions.recipe.GCrystal.Tier2GCrystalRecipe;
import me.swishhyy.swishhyysAdditions.recipe.GCrystal.Tier3GCrystalRecipe;

public final class SwishhyysAdditions extends JavaPlugin implements Listener {

    private Tier1GCrystalRecipe tier1GCrystalRecipe;
    private Tier2GCrystalRecipe tier2GCrystalRecipe;
    private Tier3GCrystalRecipe tier3GCrystalRecipe;

    @Override
    public void onEnable() {
        // Save default config if it doesn't exist
        saveDefaultConfig();

        // Register for HeadDatabase's load event
        getServer().getPluginManager().registerEvents(this, this);

        // Add to plugin.yml softdepend
        if (getServer().getPluginManager().getPlugin("HeadDatabase") == null) {
            getLogger().warning("HeadDatabase plugin not found! Some features may not work correctly.");
        }

        getLogger().info("SwishhyysAdditions waiting for HeadDatabase to load...");
    }

    @EventHandler
    public void onDatabaseLoad(DatabaseLoadEvent e) {
        // HeadDatabase is now loaded, we can register our listeners and commands
        getServer().getPluginManager().registerEvents(new Tier1GCrystalListener(this), this);
        getServer().getPluginManager().registerEvents(new Tier2GCrystalListener(this), this);
        getServer().getPluginManager().registerEvents(new Tier3GCrystalListener(this), this);

        // Initialize and register commands
        CommandLoader commandLoader = new CommandLoader(this);
        commandLoader.registerCommands();

        // Register recipes
        registerRecipes();

        getLogger().info("HeadDatabase loaded - SwishhyysAdditions enabled");
    }

    private void registerRecipes() {
        // Register the Tier1GCrystal recipe
        tier1GCrystalRecipe = new Tier1GCrystalRecipe(this);
        tier1GCrystalRecipe.register();

        // Register the Tier2GCrystal recipe
        tier2GCrystalRecipe = new Tier2GCrystalRecipe(this);
        tier2GCrystalRecipe.register();

        // Register the Tier3GCrystal recipe
        tier3GCrystalRecipe = new Tier3GCrystalRecipe(this);
        tier3GCrystalRecipe.register();
    }

    @Override
    public void onDisable() {
        // Unregister recipes to avoid duplicates on reload
        if (tier1GCrystalRecipe != null) {
            tier1GCrystalRecipe.unregister();
        }
        if (tier2GCrystalRecipe != null) {
            tier2GCrystalRecipe.unregister();
        }
        if (tier3GCrystalRecipe != null) {
            tier3GCrystalRecipe.unregister();
        }

        getLogger().info("SwishhyysAdditions disabled");
    }
}
