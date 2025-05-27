package me.swishhyy.swishhyysAdditions.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import java.util.List;

public class GrowingCrystal {
    public static ItemStack create() {
        ItemStack crystal;

        try {
            // Get the custom head from HeadDatabase
            HeadDatabaseAPI hdb = new HeadDatabaseAPI();
            crystal = hdb.getItemHead("7129"); // Replace with your desired head ID

            // Apply custom name and lore
            SkullMeta meta = (SkullMeta) crystal.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text("Growing Crystal").color(NamedTextColor.GREEN));
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
                meta.displayName(Component.text("Growing Crystal").color(NamedTextColor.GREEN));
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

