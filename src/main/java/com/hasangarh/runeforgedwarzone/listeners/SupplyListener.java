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
        // Check if the falling entity is our supply drop
        if (entity instanceof FallingBlock && entity.hasMetadata("supply_drop")) {
            // Wait one tick to ensure the block has actually placed in the world
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (event.getBlock().getType() == Material.CHEST) {
                    supplyManager.fillChest(event.getBlock());
                }
            });
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Location activeLoc = supplyManager.getActiveCrateLoc();
        if (activeLoc == null) return;

        // Check if the closed inventory belongs to the active supply crate
        if (event.getInventory().getLocation() != null &&
                event.getInventory().getLocation().getBlock().getLocation().equals(activeLoc.getBlock().getLocation())) {

            // If the chest is now empty, despawn it
            if (event.getInventory().isEmpty()) {
                activeLoc.getBlock().setType(Material.AIR);

                activeLoc.getWorld().playSound(activeLoc, Sound.BLOCK_CHEST_CLOSE, 1f, 0.5f);
                activeLoc.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, activeLoc.clone().add(0.5, 0.5, 0.5), 15, 0.5, 0.5, 0.5, 0.1);

                supplyManager.resetActiveCrate();
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Location activeLoc = supplyManager.getActiveCrateLoc();
        if (activeLoc != null && event.getBlock().getLocation().equals(activeLoc)) {
            supplyManager.resetActiveCrate();
        }
    }
}