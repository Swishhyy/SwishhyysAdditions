package me.swishhyy.swishhyysAdditions.commands.admin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class GiveCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final Logger logger;
    private final Map<String, Class<?>> itemClasses;

    public GiveCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.itemClasses = new HashMap<>();

        // Scan for item classes when the command is initialized
        try {
            scanItemClasses();
        } catch (Exception e) {
            logger.severe("Error scanning item classes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        // Check if the command is "sa give"
        if (args.length >= 1 && args[0].equalsIgnoreCase("give")) {
            // Check permission
            if (!sender.hasPermission("sa.admin")) {
                String noPermMessage = plugin.getConfig().getString("messages.no_permission", "&cYou don't have permission to do that!");
                sender.sendMessage(noPermMessage.replace('&', '§'));
                return true;
            }

            // Get the prefix from config
            String prefix = plugin.getConfig().getString("messages.prefix", "&7[&5SwishhyysAdditions&7] ").replace('&', '§');

            // Handle "/sa give" without arguments - show usage
            if (args.length == 1) {
                sender.sendMessage(prefix + "§cUsage: /sa give [player] <item>");
                sender.sendMessage(prefix + "§cAvailable items: " + String.join(", ", itemClasses.keySet()));
                return true;
            }

            Player targetPlayer;
            String itemName;

            // Handle both "/sa give <item>" and "/sa give <player> <item>"
            if (args.length == 2) {
                // Format: /sa give <item> - give to sender
                if (!(sender instanceof Player)) {
                    sender.sendMessage(prefix + "§cConsole must specify a player: /sa give <player> <item>");
                    return true;
                }

                targetPlayer = (Player) sender;
                itemName = args[1].toLowerCase();
            } else {
                // Format: /sa give <player> <item>
                String playerName = args[1];
                targetPlayer = Bukkit.getPlayer(playerName);

                if (targetPlayer == null) {
                    sender.sendMessage(prefix + "§cPlayer '" + playerName + "' not found or offline.");
                    return true;
                }

                itemName = args[2].toLowerCase();
            }

            // Check if the item exists
            if (!itemClasses.containsKey(itemName)) {
                sender.sendMessage(prefix + "§cItem '" + itemName + "' not found.");
                sender.sendMessage(prefix + "§cAvailable items: " + String.join(", ", itemClasses.keySet()));
                return true;
            }

            // Create and give the item
            try {
                Class<?> itemClass = itemClasses.get(itemName);
                Method createMethod = itemClass.getDeclaredMethod("create");
                ItemStack item = (ItemStack) createMethod.invoke(null);

                targetPlayer.getInventory().addItem(item);

                // Get message from config
                String itemGivenMessage = plugin.getConfig().getString("messages.item_given", "&aYou have received &e{item}&a!")
                    .replace("{item}", itemName)
                    .replace('&', '§');

                targetPlayer.sendMessage(prefix + itemGivenMessage);

                // If the sender is not the target, notify the sender too
                if (sender != targetPlayer) {
                    sender.sendMessage(prefix + "§aGave " + itemName + " to " + targetPlayer.getName());
                }

                logger.info("Gave " + itemName + " to " + targetPlayer.getName() + " via /sa give command");
            } catch (Exception e) {
                sender.sendMessage(prefix + "§cError creating item: " + e.getMessage());
                logger.severe("Error creating item " + itemName + ": " + e.getMessage());
                e.printStackTrace();
            }

            return true;
        }
        return false;
    }

    /**
     * Scans the items package to find all item classes with a static create() method
     */
    private void scanItemClasses() throws Exception {
        String packageName = "me.swishhyy.swishhyysAdditions.items";
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');

        // Get all resources in the package
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }

        // Go through all directories and find classes
        for (File directory : dirs) {
            findItemClasses(directory, packageName);
        }

        logger.info("Found " + itemClasses.size() + " item classes: " + String.join(", ", itemClasses.keySet()));
    }

    /**
     * Recursively finds all classes in a directory
     */
    private void findItemClasses(File directory, String packageName) throws Exception {
        if (!directory.exists()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // If the file is a directory, recurse into it
                findItemClasses(file, packageName + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                // If it's a class file, check if it has a create() method
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);

                try {
                    Class<?> clazz = Class.forName(className);

                    // Check if the class has a static create() method that returns ItemStack
                    try {
                        Method createMethod = clazz.getDeclaredMethod("create");
                        if (ItemStack.class.isAssignableFrom(createMethod.getReturnType())) {
                            // Get the simple name for the command (e.g., "tier1gcrystal" from "Tier1GCrystal")
                            String itemName = clazz.getSimpleName().toLowerCase();
                            itemClasses.put(itemName, clazz);
                            logger.info("Found item class: " + clazz.getSimpleName());
                        }
                    } catch (NoSuchMethodException ignored) {
                        // Skip classes without a create() method
                    }
                } catch (ClassNotFoundException ignored) {
                    // Skip if class can't be loaded
                }
            }
        }
    }
}
