package com.hasangarh.runeforgedwarzone.managers;

import com.hasangarh.runeforgedwarzone.RuneForgedWarzone;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class SupplyManager {

    private final RuneForgedWarzone plugin;
    private final Random random = new Random();
    private Location activeCrateLoc;

    public SupplyManager(RuneForgedWarzone plugin) {
        this.plugin = plugin;
    }

    public void startScheduler() {
        int interval = plugin.getConfig().getInt("supply-drop.interval", 1800) * 20;
        new BukkitRunnable() {
            @Override
            public void run() { spawnSupplyDrop(); }
        }.runTaskTimer(plugin, interval, interval);
    }

    public void spawnSupplyDrop() {
        FileConfiguration config = plugin.getConfig();
        int x = random.nextInt(config.getInt("supply-drop.max-x") - config.getInt("supply-drop.min-x")) + config.getInt("supply-drop.min-x");
        int z = random.nextInt(config.getInt("supply-drop.max-z") - config.getInt("supply-drop.min-z")) + config.getInt("supply-drop.min-z");
        World world = Bukkit.getWorld(config.getString("warzone-location.world", "world"));

        Location loc = new Location(world, x, config.getInt("supply-drop.drop-height", 150), z);
        FallingBlock fb = world.spawnFallingBlock(loc, Material.CHEST.createBlockData());
        fb.setMetadata("supply_drop", new FixedMetadataValue(plugin, true));
        Bukkit.broadcastMessage(ChatColor.AQUA + "Supply Drop falling at X: " + x + " Z: " + z);
    }

    public void fillChest(Block block) {
        if (block.getType() != Material.CHEST) return;
        Chest chest = (Chest) block.getState();
        FileConfiguration loot = plugin.getSupplyRewardsConfig();

        if (loot.getConfigurationSection("loot-table.items") != null) {
            for (String key : loot.getConfigurationSection("loot-table.items").getKeys(false)) {
                if (random.nextDouble() <= loot.getDouble("loot-table.items." + key + ".chance")) {
                    Material mat = Material.valueOf(loot.getString("loot-table.items." + key + ".material"));
                    int amt = loot.getInt("loot-table.items." + key + ".amount");
                    chest.getInventory().addItem(new ItemStack(mat, amt));
                }
            }
        }
        activeCrateLoc = block.getLocation();
        block.getWorld().strikeLightningEffect(block.getLocation());
    }

    public void resetActiveCrate() { this.activeCrateLoc = null; }
}