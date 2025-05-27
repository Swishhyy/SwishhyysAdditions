package me.swishhyy.swishhyysAdditions.commands.user;

import me.swishhyy.swishhyysAdditions.items.GrowingCrystal;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class GCrystalCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final Logger logger;

    public GCrystalCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        logger.info("GCrystalCommand: executed by " + sender.getName() + ", label=" + label + ", args=" + String.join(",", args));
        if (sender instanceof Player player) {
            logger.info("GCrystalCommand: giving Growing Crystal to player " + player.getName());
            player.getInventory().addItem(GrowingCrystal.create());
            player.sendMessage("§aYou have been given a Growing Crystal!");
        } else {
            logger.warning("GCrystalCommand: non-player sender " + sender.getName());
            plugin.getLogger().info("Only players can use /gcrystal");
        }
        return true;
    }
}
