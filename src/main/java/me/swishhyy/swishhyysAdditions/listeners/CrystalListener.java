package me.swishhyy.swishhyysAdditions.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
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
import org.bukkit.Sound;
import me.arcaniax.hdb.api.HeadDatabaseAPI;

import java.util.logging.Logger;

public class CrystalListener implements Listener {
    private final JavaPlugin plugin;
    private final Logger logger;
    private final boolean debug;

    public CrystalListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.debug = plugin.getConfig().getBoolean("debug", false);
    }

    @EventHandler
    public void onCrystalUse(PlayerInteractEvent e) {
        if (debug) logger.info("onCrystalUse: action " + e.getAction() + " by player " + e.getPlayer().getName());
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = e.getItem();
        if (item == null) {
            if (debug) logger.info("onCrystalUse: no item in hand");
            return;
        }
        var meta = item.getItemMeta();
        if (meta != null) {
            Component displayNameComp = meta.displayName();
            if (displayNameComp == null) {
                if (debug) logger.info("onCrystalUse: item has no displayName");
            } else {
                String rawName = LegacyComponentSerializer.legacySection().serialize(displayNameComp);
                String name = PlainTextComponentSerializer.plainText().serialize(displayNameComp);
                if (debug) logger.info("onCrystalUse: rawDisplayName=" + rawName + ", strippedName=" + name);
                if ("Growing Crystal".equalsIgnoreCase(name)) {
                    if (debug) logger.info("onCrystalUse: matched Growing Crystal");
                    e.setCancelled(true);
                    Block clicked = e.getClickedBlock();
                    if (clicked == null) return;
                    World world = clicked.getWorld();
                    Location spawnLoc = clicked.getLocation().add(0.5, 1.2, 0.5);
                    // consume one crystal
                    item.setAmount(item.getAmount() - 1);
                    if (debug) logger.info("onCrystalUse: consumed one crystal, remaining=" + item.getAmount());
                    // spawn floating crystal
                    ArmorStand stand = world.spawn(spawnLoc, ArmorStand.class, as -> {
                        as.setInvisible(true);
                        as.setMarker(true);
                        as.setGravity(false);
                        try {
                            HeadDatabaseAPI hdb = new HeadDatabaseAPI();
                            // The ID should be the head ID from HeadDatabase (e.g., "7129")
                            // NOT the texture hash
                            ItemStack head = hdb.getItemHead("7129");
                            as.getEquipment().setHelmet(head);
                        } catch (NullPointerException ex) {
                            if (debug) logger.warning("Failed to get head from HeadDatabase: " + ex.getMessage());
                            // Fallback to a regular player head if HeadDatabase fails
                            as.getEquipment().setHelmet(new ItemStack(org.bukkit.Material.PLAYER_HEAD));
                        }
                    });
                    if (debug) logger.info("onCrystalUse: spawned ArmorStand at " + spawnLoc);
                    // spawn hologram above the crystal for countdown
                    ArmorStand holo = world.spawn(spawnLoc.clone().add(0, 2.5, 0), ArmorStand.class, as2 -> {
                        as2.setInvisible(true);
                        as2.setMarker(true);
                        as2.setGravity(false);
                        as2.customName(Component.text("2:00", TextColor.color(0x9966CC)));
                        as2.setCustomNameVisible(true);
                    });
                    // total lifetime in ticks
                    long lifeTicks = 120L * 20L;
                    // update countdown every second
                    new BukkitRunnable() {
                        int remaining = 120;
                        @Override
                        public void run() {
                            if (holo.isDead() || stand.isDead()) { this.cancel(); return; }
                            if (remaining <= 0) { holo.remove(); this.cancel(); return; }
                            int mins = remaining / 60, secs = remaining % 60;
                            holo.customName(Component.text(String.format("%d:%02d", mins, secs), TextColor.color(0x9966CC)));
                            // holo remains fixed above crystal, no teleport needed
                            remaining--;
                        }
                    }.runTaskTimer(plugin, 0L, 20L);
                    // remove hologram when crystal expires
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!holo.isDead()) holo.remove();
                        }
                    }.runTaskLater(plugin, lifeTicks);
                    // animate spin and bob
                    Location baseLoc = stand.getLocation().clone();
                    new BukkitRunnable() {
                        int tick = 0;
                        @Override
                        public void run() {
                            if (stand.isDead()) {
                                this.cancel();
                                return;
                            }
                            tick++;
                            double bob = Math.sin(tick * 0.1) * 0.15;
                            Location newLoc = baseLoc.clone().add(0, bob, 0);
                            stand.teleport(newLoc);
                            float yaw = (tick * 5f) % 360f;
                            stand.setRotation(yaw, 0f);
                        }
                    }.runTaskTimer(plugin, 0L, 1L);
                    // schedule growth every 10 seconds until removal
                    BukkitRunnable task = new BukkitRunnable() {
                        int ticks = 0;
                        @Override
                        public void run() {
                            if (stand.isDead()) {
                                this.cancel();
                                return;
                            }
                            // growth cycle
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
                            // play growth sound every cycle
                            world.playSound(center, Sound.BLOCK_CHORUS_FLOWER_GROW, 1.0F, 1.0F);
                            ticks += 10;
                        }
                    };
                    if (debug) logger.info("Growth task scheduled with interval=200 ticks");
                    task.runTaskTimer(this.plugin, 200L, 200L);
                    // schedule warning effect 15 ticks before crystal expires (120 seconds total = 2400 ticks)
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Location warnLoc = stand.getLocation();
                            World warnWorld = warnLoc.getWorld();
                            if (warnWorld != null) {
                                warnWorld.spawnParticle(Particle.EXPLOSION, warnLoc, 10, 0.5, 0.5, 0.5, 0.1);
                                warnWorld.playSound(warnLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
                            }
                        }
                    }.runTaskLater(this.plugin, lifeTicks - 15L);
                    // schedule final removal with explosion at 120 seconds
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Location expireLoc = stand.getLocation();
                            World expireWorld = expireLoc.getWorld();
                            if (expireWorld != null) {
                                expireWorld.spawnParticle(Particle.EXPLOSION, expireLoc, 10, 0.5, 0.5, 0.5, 0.1);
                                expireWorld.playSound(expireLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
                            }
                            stand.remove();
                            if (debug) logger.info("Crystal expired at " + expireLoc);
                        }
                    }.runTaskLater(this.plugin, lifeTicks);
                }
            }
        }
    }
}
