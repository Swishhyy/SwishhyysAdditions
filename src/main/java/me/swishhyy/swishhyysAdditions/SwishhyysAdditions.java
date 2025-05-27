package me.swishhyy.swishhyysAdditions;

import org.bukkit.plugin.java.JavaPlugin;
import me.swishhyy.swishhyysAdditions.listeners.CrystalListener;
import me.swishhyy.swishhyysAdditions.commands.user.GCrystalCommand;

public final class SwishhyysAdditions extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        // Register event listener
        getServer().getPluginManager().registerEvents(new CrystalListener(this), this);
        // Register /gcrystal command executor
        this.getCommand("gcrystal").setExecutor(new GCrystalCommand(this));
        getLogger().info("SwishhyysAdditions enabled");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("SwishhyysAdditions disabled");
    }
}
