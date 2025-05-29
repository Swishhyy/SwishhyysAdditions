package me.swishhyy.swishhyysAdditions.listeners.CrystalListener;

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
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.logging.Logger;

public class Tier2GCrystalListener implements Listener {
    private final JavaPlugin plugin;
    private final Logger logger;
    private final boolean debug;
    private final String crystalHeadId;
    private final int nameColor;
    private static final NamespacedKey CRYSTAL_KEY;

    // Tier 2 specific values
    private static final int DURATION_SECONDS = 600; // 10 minutes
    private static final int INTERVAL_SECONDS = 10;
    private static final int RANGE = 5; // 5 blocks in each direction = 10x10x10 range

    public Tier2GCrystalListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.debug = plugin.getConfig().getBoolean("debug", false);
        // Read crystal head ID and name color from config
        this.crystalHeadId = plugin.getConfig().getString("items.growing_crystal.tier_2.head_id", "74338");
        String colorHex = plugin.getConfig().getString("items.growing_crystal.name_color", "9966CC");
        this.nameColor = Integer.parseInt(colorHex, 16);
    }

    // Static initializer to create the NamespacedKey
    static {
        CRYSTAL_KEY = new NamespacedKey("swishhyysadditions", "growing_crystal_tier2");
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
            // First check if this item has our persistent data tag
            boolean isTaggedCrystal = meta.getPersistentDataContainer().has(CRYSTAL_KEY, PersistentDataType.BYTE);

            // If it has our tag, we know it's a Tier 2 Growing Crystal
            if (isTaggedCrystal) {
                if (debug) logger.info("onCrystalUse: matched Tier 2 Growing Crystal by tag");
                placeCrystal(e, item);
                return;
            }

            // Fallback to display name check for backward compatibility
            Component displayNameComp = meta.displayName();
            if (displayNameComp == null) {
                if (debug) logger.info("onCrystalUse: item has no displayName");
            } else {
                String rawName = LegacyComponentSerializer.legacySection().serialize(displayNameComp);
                String name = PlainTextComponentSerializer.plainText().serialize(displayNameComp);
                if (debug) logger.info("onCrystalUse: rawDisplayName=" + rawName + ", strippedName=" + name);
                if ("Tier 2 Growing Crystal".equalsIgnoreCase(name)) {
                    if (debug) logger.info("onCrystalUse: matched Tier 2 Growing Crystal by name");
                    placeCrystal(e, item);
                }
            }
        }
    }

    /**
     * Places a Tier 2 Growing Crystal in the world
     * @param e The PlayerInteractEvent that triggered this
     * @param item The Tier 2 Growing Crystal item being used
     */
    private void placeCrystal(PlayerInteractEvent e, ItemStack item) {
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
                // Use the constant for consistency
                ItemStack head = hdb.getItemHead(crystalHeadId);
                as.getEquipment().setHelmet(head);
            } catch (Exception ex) {
                if (debug) logger.warning("Failed to get head from HeadDatabase: " + ex.getMessage());
                // Fallback to a regular player head if HeadDatabase fails
                as.getEquipment().setHelmet(new ItemStack(org.bukkit.Material.PLAYER_HEAD));
            }
        });

        if (debug) logger.info("onCrystalUse: spawned ArmorStand at " + spawnLoc);

        // spawn idle particles around the crystal
        spawnIdleParticles(spawnLoc, world);

        // spawn hologram above the crystal for countdown
        ArmorStand holo = world.spawn(spawnLoc.clone().add(0, 2.5, 0), ArmorStand.class, as2 -> {
            as2.setInvisible(true);
            as2.setMarker(true);
            as2.setGravity(false);
            as2.customName(Component.text("10:00", TextColor.color(nameColor)));
            as2.setCustomNameVisible(true);
        });

        // total lifetime in ticks
        long lifeTicks = DURATION_SECONDS * 20L;

        // update countdown every second
        new BukkitRunnable() {
            int remaining = DURATION_SECONDS;
            @Override
            public void run() {
                if (holo.isDead() || stand.isDead()) { this.cancel(); return; }
                if (remaining <= 0) { holo.remove(); this.cancel(); return; }
                int mins = remaining / 60, secs = remaining % 60;
                holo.customName(Component.text(String.format("%d:%02d", mins, secs), TextColor.color(nameColor)));
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

        // schedule growth every INTERVAL_SECONDS until removal
        long intervalTicks = INTERVAL_SECONDS * 20L;
        BukkitRunnable task = new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (stand.isDead()) {
                    this.cancel();
                    return;
                }
                // growth cycle with effects
                growWithEffects(stand.getLocation(), world);
                ticks += INTERVAL_SECONDS;
            }
        };

        if (debug) logger.info("Growth task scheduled with interval=" + intervalTicks + " ticks");
        task.runTaskTimer(this.plugin, intervalTicks, intervalTicks);

        // schedule warning effect 15 ticks before crystal expires
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

        // schedule final removal with explosion
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

    private void spawnIdleParticles(Location location, World world) {
        // Adjust idle particles to have a slightly larger size around the head of the armor stand
        new BukkitRunnable() {
            @Override
            public void run() {
                if (world.getNearbyEntities(location, 1, 1, 1).stream().noneMatch(e -> e instanceof ArmorStand)) {
                    this.cancel();
                    return;
                }
                world.spawnParticle(Particle.CRIT, location.clone().add(0, 1.8, 0), 5, 0.3, 0.3, 0.3, 0.01);
                world.spawnParticle(Particle.CLOUD, location.clone().add(0, 1.8, 0), 3, 0.2, 0.2, 0.2, 0.01);
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void spawnBurstEffect(Location location, World world) {
        // Create a moderate burst effect for Tier 2 using white particles
        world.spawnParticle(Particle.CLOUD, location, 50, RANGE, RANGE, RANGE, 0.1);
        world.spawnParticle(Particle.ASH, location, 30, RANGE, RANGE, RANGE, 0.05);
    }

    private void growWithEffects(Location center, World world) {
        spawnBurstEffect(center, world);

        // Schedule the growth and burst effect after the charging effect
        new BukkitRunnable() {
            @Override
            public void run() {
                // Growth logic
                for (int dx = -RANGE; dx <= RANGE; dx++) {
                    for (int dz = -RANGE; dz <= RANGE; dz++) {
                        for (int dy = -RANGE; dy <= RANGE; dy++) {
                            Location loc = center.clone().add(dx, dy, dz);
                            Block block = world.getBlockAt(loc);
                            BlockData data = block.getBlockData();
                            if (data instanceof Ageable ageable) {
                                int max = ageable.getMaximumAge();
                                if (ageable.getAge() < max) {
                                    ageable.setAge(ageable.getAge() + 1);
                                    block.setBlockData(ageable);
                                    world.spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().add(0.5, 1.0, 0.5), 1);
                                }
                            }
                        }
                    }
                }

                // Trigger the burst effect
                spawnBurstEffect(center, world);
            }
        }.runTaskLater(plugin, 20L); // Delay to match the charging effect duration
    }
}
