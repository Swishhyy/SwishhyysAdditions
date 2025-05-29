package me.swishhyy.swishhyysAdditions.items.GCrystal;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.swishhyy.swishhyysAdditions.SwishhyysAdditions;
import java.util.List;

public class Tier3GCrystal {
    // Access the plugin instance to get the config
    private static final JavaPlugin plugin = SwishhyysAdditions.getPlugin(SwishhyysAdditions.class);
    private static final NamespacedKey CRYSTAL_KEY = new NamespacedKey("swishhyysadditions", "growing_crystal_tier3");

    public static ItemStack create() {
        ItemStack crystal;

        // Read crystal head ID from config with proper tier structure
        String crystalHeadId = plugin.getConfig().getString("items.growing_crystal.tier_3.head_id", "74317");
        String colorHex = plugin.getConfig().getString("items.growing_crystal.tier_3.name_color", "E0115F");
        int nameColor = Integer.parseInt(colorHex, 16);

        try {
            // Get the custom head from HeadDatabase using the config-defined ID
            HeadDatabaseAPI hdb = new HeadDatabaseAPI();
            crystal = hdb.getItemHead(crystalHeadId);

            // Apply custom name and lore with significantly increased duration and improved interval
            SkullMeta meta = (SkullMeta) crystal.getItemMeta();
            if (meta != null) {
                String displayName = plugin.getConfig().getString("items.growing_crystal.tier_3.display_name", "T3 Growth Crystal");
                meta.displayName(Component.text(displayName).color(TextColor.color(nameColor)));
                meta.lore(List.of(
                    Component.text("Duration: 480 seconds").color(NamedTextColor.GRAY),
                    Component.text("Interval: 5 seconds").color(NamedTextColor.GRAY)
                ));

                // Add the tier-specific tag so the listener can identify it
                meta.getPersistentDataContainer().set(CRYSTAL_KEY, PersistentDataType.BYTE, (byte)1);

                crystal.setItemMeta(meta);
            }
        } catch (Exception e) {
            // Fallback to regular player head if HeadDatabase isn't loaded
            crystal = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) crystal.getItemMeta();
            if (meta != null) {
                String displayName = plugin.getConfig().getString("items.growing_crystal.tier_3.display_name", "T3 Growth Crystal");
                meta.displayName(Component.text(displayName).color(TextColor.color(nameColor)));
                meta.lore(List.of(
                    Component.text("Duration: 480 seconds").color(NamedTextColor.GRAY),
                    Component.text("Interval: 5 seconds").color(NamedTextColor.GRAY)
                ));

                // Add the tier-specific tag so the listener can identify it
                meta.getPersistentDataContainer().set(CRYSTAL_KEY, PersistentDataType.BYTE, (byte)1);

                crystal.setItemMeta(meta);
            }
        }

        return crystal;
    }
}
