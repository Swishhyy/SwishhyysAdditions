package me.swishhyy.swishhyysAdditions.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.Particle;
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
        logger.info("onCrystalUse: action " + e.getAction() + " by player " + e.getPlayer().getName());
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = e.getItem();
        if (item == null) {
            logger.info("onCrystalUse: no item in hand");
            return;
        }
        var meta = item.getItemMeta();
        if (meta != null) {
            Component displayNameComp = meta.displayName();
            if (displayNameComp == null) {
                logger.info("onCrystalUse: item has no displayName");
            } else {
                String rawName = LegacyComponentSerializer.legacySection().serialize(displayNameComp);
                String name = PlainTextComponentSerializer.plainText().serialize(displayNameComp);
                logger.info("onCrystalUse: rawDisplayName=" + rawName + ", strippedName=" + name);
                if ("Growing Crystal".equalsIgnoreCase(name)) {
                    logger.info("onCrystalUse: matched Growing Crystal");
                    e.setCancelled(true);
                    Block clicked = e.getClickedBlock();
                    if (clicked == null) return;
                    World world = clicked.getWorld();
                    Location spawnLoc = clicked.getLocation().add(0.5, 1.2, 0.5);
                    // consume one crystal
                    item.setAmount(item.getAmount() - 1);
                    logger.info("onCrystalUse: consumed one crystal, remaining=" + item.getAmount());
                    // spawn floating crystal
                    ArmorStand stand = world.spawn(spawnLoc, ArmorStand.class, as -> {
                        as.setInvisible(true);
                        as.setMarker(true);
                        as.setGravity(false);
                        as.getEquipment().setHelmet(new ItemStack(Material.PLAYER_HEAD));
                    });
                    logger.info("onCrystalUse: spawned ArmorStand at " + spawnLoc);
                    // schedule growth every 10 seconds, for 240 seconds total
                    BukkitRunnable task = new BukkitRunnable() {
                        int ticks = 0;
                        @Override
                        public void run() {
                            logger.info("Growth cycle at ticks=" + ticks + " at " + stand.getLocation());
                            if (ticks >= 240) {
                                stand.remove();
                                this.cancel();
                                logger.info("Crystal expired at " + stand.getLocation());
                                return;
                            }
                            Location center = stand.getLocation();
                            for (int dx = -2; dx <= 2; dx++) {
                                for (int dz = -2; dz <= 2; dz++) {
                                    for (int dy = -5; dy <= 5; dy++) {
                                        Location loc = center.clone().add(dx, dy, dz);
                                        Block block = world.getBlockAt(loc);
                                        // handle ageable growth replaced by direct data logic
                                        BlockData data = block.getBlockData();
                                        if (data instanceof Ageable ageable) {
                                            int max = ageable.getMaximumAge();
                                            if (ageable.getAge() < max) {
                                                ageable.setAge(ageable.getAge() + 1);
                                                block.setBlockData(ageable);
                                                // visual particle effect (happy villager as bone-meal feedback)
                                                world.spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().add(0.5, 1.0, 0.5), 1);
                                            }
                                        }
                                    }
                                }
                            }
                            ticks += 10;
                        }
                    };
                    logger.info("onCrystalUse: scheduling growth task");
                    task.runTaskTimer(this.plugin, 200L, 200L);
                    logger.info("Growth task scheduled with interval=200 ticks");
                }
            }
        }
    }
}
