package me.swishhyy.swishhyysAdditions.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import java.util.List;

public class GrowingCrystal {
    public static ItemStack create() {
        ItemStack crystal = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) crystal.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Growing Crystal").color(NamedTextColor.GREEN));
            meta.lore(List.of(
                Component.text("Duration: 250 seconds").color(NamedTextColor.GRAY),
                Component.text("Interval: 10 seconds").color(NamedTextColor.GRAY)
            ));
            crystal.setItemMeta(meta);
        }
        return crystal;
    }
}

