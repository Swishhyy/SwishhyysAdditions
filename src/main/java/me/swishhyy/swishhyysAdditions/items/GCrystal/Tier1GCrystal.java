package me.swishhyy.swishhyysAdditions.items.GCrystal;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.swishhyy.swishhyysAdditions.SwishhyysAdditions;
import java.util.List;

public class Tier1GCrystal {
    // Access the plugin instance to get the config
    private static final JavaPlugin plugin = SwishhyysAdditions.getPlugin(SwishhyysAdditions.class);
    private static final NamespacedKey CRYSTAL_KEY = new NamespacedKey("swishhyysadditions", "growing_crystal");

    public static ItemStack create() {
        ItemStack crystal;

        // Read crystal head ID from config with proper tier structure
        String crystalHeadId = plugin.getConfig().getString("items.growing_crystal.tier_1.head_id", "74344");

        try {
            // Get the custom head from HeadDatabase using the config-defined ID
            HeadDatabaseAPI hdb = new HeadDatabaseAPI();
            crystal = hdb.getItemHead(crystalHeadId);

            // Apply custom name and lore
            SkullMeta meta = (SkullMeta) crystal.getItemMeta();
            if (meta != null) {
                String displayName = plugin.getConfig().getString("items.growing_crystal.tier_1.display_name", "T1 Growth Crystal");
                String colorHex = plugin.getConfig().getString("items.growing_crystal.tier_1.name_color", "9966CC");
                int nameColor = Integer.parseInt(colorHex, 16);
                meta.displayName(Component.text(displayName).color(TextColor.color(nameColor)));
                meta.lore(List.of(
                    Component.text("Duration: 120 seconds").color(NamedTextColor.GRAY),
                    Component.text("Interval: 10 seconds").color(NamedTextColor.GRAY)
                ));
                crystal.setItemMeta(meta);
            }
        } catch (Exception e) {
            // Fallback to regular player head if HeadDatabase isn't loaded
            crystal = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) crystal.getItemMeta();
            if (meta != null) {
                String displayName = plugin.getConfig().getString("items.growing_crystal.tier_1.display_name", "T1 Growth Crystal");
                String colorHex = plugin.getConfig().getString("items.growing_crystal.tier_1.name_color", "9966CC");
                int nameColor = Integer.parseInt(colorHex, 16);
                meta.displayName(Component.text(displayName).color(TextColor.color(nameColor)));
                meta.lore(List.of(
                    Component.text("Duration: 120 seconds").color(NamedTextColor.GRAY),
                    Component.text("Interval: 10 seconds").color(NamedTextColor.GRAY)
                ));
                crystal.setItemMeta(meta);
            }
        }

        return crystal;
    }
}

