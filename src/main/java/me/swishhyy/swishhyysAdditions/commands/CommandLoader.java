package me.swishhyy.swishhyysAdditions.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.swishhyy.swishhyysAdditions.commands.admin.GiveCommand;
import me.swishhyy.swishhyysAdditions.commands.admin.ReloadConfigCommand;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Handles registration and routing of all plugin commands
 */
public class CommandLoader implements TabCompleter {
    private final JavaPlugin plugin;
    private final ReloadConfigCommand reloadConfigCommand;
    private final GiveCommand giveCommand;

    public CommandLoader(JavaPlugin plugin) {
        this.plugin = plugin;

        // Initialize command handlers
        this.reloadConfigCommand = new ReloadConfigCommand(plugin);
        this.giveCommand = new GiveCommand(plugin);
    }

    /**
     * Register all commands with the server
     */
    public void registerCommands() {
        // Register main command executor and tab completer
        Objects.requireNonNull(plugin.getCommand("sa")).setExecutor(this::onSwishhyysAdditionsCommand);
        Objects.requireNonNull(plugin.getCommand("sa")).setTabCompleter(this);

        plugin.getLogger().info("Registered all commands");
    }

    /**
     * Main command handler for /sa command
     */
    private boolean onSwishhyysAdditionsCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Handle /sa with no arguments - show plugin info
            String prefix = plugin.getConfig().getString("messages.prefix", "&7[&5SwishhyysAdditions&7] ").replace('&', '§');
            sender.sendMessage(prefix + "§aSwishhyysAdditions v" + plugin.getDescription().getVersion());

            if (sender.hasPermission("sa.admin")) {
                sender.sendMessage(prefix + "§7Usage: /sa <reload|give>");
            }

            return true;
        }

        // Route to appropriate command handler
        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("reload")) {
            return reloadConfigCommand.onCommand(sender, command, label, args);
        } else if (subCommand.equals("give")) {
            return giveCommand.onCommand(sender, command, label, args);
        }

        // Unknown subcommand
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String alias, String @NotNull [] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("sa")) {
            if (args.length == 1) {
                // First argument - subcommands
                if (sender.hasPermission("sa.admin")) {
                    completions.addAll(Arrays.asList("reload", "give"));
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
                // Second argument for "give" - player names or item names
                if (sender instanceof Player) {
                    // If command sender is a player, offer player names AND item names
                    completions.addAll(plugin.getServer().getOnlinePlayers().stream()
                            .map(Player::getName)
                            .toList());

                    // Add item names for tab completion
                    completions.addAll(giveCommand.getItemNames());
                } else {
                    // If sender is console, only show player names (as console must specify a player)
                    completions.addAll(plugin.getServer().getOnlinePlayers().stream()
                            .map(Player::getName)
                            .toList());
                }
            } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
                // Third argument for "give" (after player name) - item names
                completions.addAll(giveCommand.getItemNames());
            }
        }

        // Filter by what the player has typed so far
        if (!args[args.length - 1].isEmpty()) {
            String lastArg = args[args.length - 1].toLowerCase();
            completions = completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(lastArg))
                    .collect(Collectors.toList());
        }

        return completions;
    }
}
