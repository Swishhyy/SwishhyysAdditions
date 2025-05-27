package me.swishhyy.swishhyysAdditions.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Logger;

public class CrystalListener implements Listener {
    private final JavaPlugin plugin;
    private final Logger logger;

    public CrystalListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    @EventHandler
    public void onCrystalUse(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = e.getItem();
        if (item == null) return;
        var meta = item.getItemMeta();
        if (meta != null) {
            Component displayNameComp = meta.displayName();
            if (displayNameComp != null) {
                String name = LegacyComponentSerializer.legacySection().serialize(displayNameComp);
                if ("Growing Crystal".equals(name)) {
                    e.setCancelled(true);
                    Block clicked = e.getClickedBlock();
                    if (clicked == null) return;
                    World world = clicked.getWorld();
                    Location spawnLoc = clicked.getLocation().add(0.5, 1.2, 0.5);
                    // consume one crystal
                    item.setAmount(item.getAmount() - 1);
                    // spawn floating crystal
                    ArmorStand stand = world.spawn(spawnLoc, ArmorStand.class, as -> {
                        as.setInvisible(true);
                        as.setMarker(true);
                        as.setGravity(false);
                        as.getEquipment().setHelmet(new ItemStack(Material.PLAYER_HEAD));
                    });
                    // schedule growth every 10 seconds, for 250 seconds total
                    BukkitRunnable task = new BukkitRunnable() {
                        int ticks = 0;
                        @Override
                        public void run() {
                            if (ticks >= 250) {
                                stand.remove();
                                this.cancel();
                                logger.info("Crystal expired at " + stand.getLocation());
                                return;
                            }
                            Location center = stand.getLocation();
                            for (int dx = -5; dx <= 5; dx++) {
                                for (int dz = -5; dz <= 5; dz++) {
                                    Block block = world.getBlockAt(center.getBlockX() + dx, center.getBlockY() - 1, center.getBlockZ() + dz);
                                    if (block.getBlockData() instanceof Ageable ageable) {
                                        int max = ageable.getMaximumAge();
                                        if (ageable.getAge() < max) {
                                            ageable.setAge(Math.min(max, ageable.getAge() + 1));
                                            block.setBlockData(ageable);
                                        }
                                    }
                                }
                            }
                            ticks += 10;
                        }
                    };
                    task.runTaskTimer(this.plugin, 200L, 200L);
                    logger.info("Crystal placed at " + spawnLoc);
                }
            }
        }
    }
}
