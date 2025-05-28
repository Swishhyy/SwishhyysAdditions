package me.swishhyy.swishhyysAdditions.commands.admin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class ReloadConfigCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final Logger logger;

    public ReloadConfigCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        // Check if the command is "sa reload"
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            // Check if the sender has permission
            if (!sender.hasPermission("sa.admin")) {
                String noPermMessage = plugin.getConfig().getString("messages.no_permission", "&cYou don't have permission to do that!");
                sender.sendMessage(noPermMessage.replace('&', '§'));
                return true;
            }

            // Reload the config
            plugin.reloadConfig();

            // Get prefix from config, use default if not found
            String prefix = plugin.getConfig().getString("messages.prefix", "&7[&5SwishhyysAdditions&7] ");
            String reloadMessage = prefix.replace('&', '§') + "§aConfiguration reloaded!";

            // Send success message to sender
            sender.sendMessage(reloadMessage);
            logger.info("Configuration reloaded by " + sender.getName());

            return true;
        }
        return false;
    }
}
