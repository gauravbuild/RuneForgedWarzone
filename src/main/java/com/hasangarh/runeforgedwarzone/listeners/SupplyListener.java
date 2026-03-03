package com.hasangarh.runeforgedwarzone.listeners;

import com.hasangarh.runeforgedwarzone.RuneForgedWarzone;
import com.hasangarh.runeforgedwarzone.managers.SupplyManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class SupplyListener implements Listener {

    private final RuneForgedWarzone plugin;
    private final SupplyManager supplyManager;

    public SupplyListener(RuneForgedWarzone plugin, SupplyManager supplyManager) {
        this.plugin = plugin;
        this.supplyManager = supplyManager;
    }

    @EventHandler
    public void onLand(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof FallingBlock && entity.hasMetadata("supply_drop")) {
            if (event.getTo() == Material.CHEST) {
                plugin.getServer().getScheduler().runTask(plugin, () -> supplyManager.fillChest(event.getBlock()));
            }
        }
    }

    // --- NEW: CLEANUP LOGIC ---
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Location activeLoc = supplyManager.getActiveCrateLoc();
        if (activeLoc == null) return;

        // Is this inventory the Supply Crate?
        if (event.getInventory().getLocation() != null && event.getInventory().getLocation().equals(activeLoc)) {

            // Is it empty? (Looted)
            if (event.getInventory().isEmpty()) {
                // DELETE IT
                activeLoc.getBlock().setType(Material.AIR);

                // Effects
                activeLoc.getWorld().playSound(activeLoc, Sound.BLOCK_CHEST_CLOSE, 1f, 0.5f);
                activeLoc.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, activeLoc.add(0.5, 0.5, 0.5), 15, 0.5, 0.5, 0.5, 0.1);

                // Reset Tracker so new one can spawn
                supplyManager.resetActiveCrate();
            }
        }
    }

    // Safety: If they break it manually
    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Location activeLoc = supplyManager.getActiveCrateLoc();
        if (activeLoc != null && event.getBlock().getLocation().equals(activeLoc)) {
            supplyManager.resetActiveCrate();
        }
    }
}