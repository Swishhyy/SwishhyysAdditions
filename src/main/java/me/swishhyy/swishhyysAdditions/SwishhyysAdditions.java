package me.swishhyy.swishhyysAdditions;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import me.arcaniax.hdb.api.DatabaseLoadEvent;
import me.swishhyy.swishhyysAdditions.listeners.CrystalListener.Tier1GCrystalListener;
import me.swishhyy.swishhyysAdditions.commands.CommandLoader;

public final class SwishhyysAdditions extends JavaPlugin implements Listener {

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

        // Initialize and register commands
        CommandLoader commandLoader = new CommandLoader(this);
        commandLoader.registerCommands();

        getLogger().info("HeadDatabase loaded - SwishhyysAdditions enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("SwishhyysAdditions disabled");
    }
}
