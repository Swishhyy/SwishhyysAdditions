package me.swishhyy.swishhyysAdditions.commands.user;

import me.swishhyy.swishhyysAdditions.items.GrowingCrystal;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class GCrystalCommand implements CommandExecutor {
    private final JavaPlugin plugin;

    public GCrystalCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender instanceof Player player) {
            player.getInventory().addItem(GrowingCrystal.create());
            player.sendMessage("§aYou have been given a Growing Crystal!");
        } else {
            plugin.getLogger().info("Only players can use /gcrystal");
        }
        return true;
    }
}
