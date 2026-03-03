package com.hasangarh.runeforgedwarzone.managers;

import com.hasangarh.runeforgedwarzone.RuneForgedWarzone;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class SupplyManager {

    private final RuneForgedWarzone plugin;
    private final Random random = new Random();

    private Location activeCrateLoc;
    private boolean isDropping = false;

    public SupplyManager(RuneForgedWarzone plugin) {
        this.plugin = plugin;
    }

    public void startScheduler() {
        int interval = plugin.getConfig().getInt("supply-drop.interval", 1800) * 20;

        new BukkitRunnable() {
            @Override
            public void run() {
                spawnSupplyDrop();
            }
        }.runTaskTimer(plugin, interval, interval);
    }

    public void spawnSupplyDrop() {
        if (activeCrateLoc != null) {
            if (activeCrateLoc.getBlock().getType() == Material.CHEST) {
                Bukkit.broadcastMessage(ChatColor.RED + "Supply Drop skipped: Previous crate is still unlooted!");
                return;
            } else {
                activeCrateLoc = null;
            }
        }

        if (isDropping) return;

        isDropping = true;

        int minX = plugin.getConfig().getInt("supply-drop.min-x");
        int maxX = plugin.getConfig().getInt("supply-drop.max-x");
        int minZ = plugin.getConfig().getInt("supply-drop.min-z");
        int maxZ = plugin.getConfig().getInt("supply-drop.max-z");
        int y = plugin.getConfig().getInt("supply-drop.drop-height", 150);
        String worldName = plugin.getConfig().getString("warzone-location.world", "world");

        int x = random.nextInt(maxX - minX + 1) + minX;
        int z = random.nextInt(maxZ - minZ + 1) + minZ;

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            isDropping = false;
            return;
        }

        Location dropLoc = new Location(world, x + 0.5, y, z + 0.5);

        FallingBlock fallingChest = world.spawnFallingBlock(dropLoc, Material.CHEST.createBlockData());
        fallingChest.setDropItem(false);
        fallingChest.setMetadata("supply_drop", new FixedMetadataValue(plugin, true));

        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                "&b&lSUPPLY DROP &8» &7A crate is falling at &eX:" + x + " Z:" + z + "&7!"));

        for(Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.3f, 2f);
        }
    }

    public void fillChest(Block block) {
        isDropping = false;
        if (block.getType() != Material.CHEST) return;

        activeCrateLoc = block.getLocation();

        Chest chest = (Chest) block.getState();

        // Loot Table
        chest.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 2));
        chest.getInventory().addItem(new ItemStack(Material.DIAMOND, 4));
        chest.getInventory().addItem(new ItemStack(Material.EXPERIENCE_BOTTLE, 16));

        if (random.nextBoolean()) {
            if (!block.getWorld().getPlayers().isEmpty()) {
                // Placeholder for actual Rune (Use API ideally)
                chest.getInventory().addItem(new ItemStack(Material.NETHER_STAR));
            }
        }

        block.getWorld().strikeLightningEffect(block.getLocation());
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&b&lSUPPLY DROP &8» &aThe Crate has landed!"));

        // --- NEW: AUTO DESPAWN TIMER ---
        int despawnSeconds = plugin.getConfig().getInt("supply-drop.despawn-time", 600); // Default 10 mins

        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if this specific chest location is STILL the active one (meaning it wasn't looted/reset)
                if (activeCrateLoc != null && activeCrateLoc.equals(block.getLocation())) {

                    if (block.getType() == Material.CHEST) {
                        // Delete it
                        block.setType(Material.AIR);

                        // Effects
                        block.getWorld().spawnParticle(Particle.CLOUD, block.getLocation().add(0.5, 0.5, 0.5), 20, 0.5, 0.5, 0.5, 0.1);
                        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1f);

                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&6&lWARZONE &8» &eThe Supply Drop decayed!"));
                    }

                    // Allow new drops to spawn
                    resetActiveCrate();
                }
            }
        }.runTaskLater(plugin, despawnSeconds * 20L); // Convert seconds to ticks
    }

    public Location getActiveCrateLoc() {
        return activeCrateLoc;
    }

    public void resetActiveCrate() {
        this.activeCrateLoc = null;
    }
}