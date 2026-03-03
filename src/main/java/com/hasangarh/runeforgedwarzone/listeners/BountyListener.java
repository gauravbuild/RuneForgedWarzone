package com.hasangarh.runeforgedwarzone.listeners;

import com.hasangarh.runeforgedwarzone.RuneForgedWarzone;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class BountyListener implements Listener {

    private final RuneForgedWarzone plugin;

    public BountyListener(RuneForgedWarzone plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Use equals() to match the GUI title exactly
        if (!event.getView().getTitle().equals(ChatColor.DARK_RED + "Active Bounties")) return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player hunter = (Player) event.getWhoClicked();

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) return;

        SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();
        // FIXED: Changed setOwningPlayer() to getOwningPlayer()
        if (meta == null || meta.getOwningPlayer() == null) return;

        Player target = Bukkit.getPlayer(meta.getOwningPlayer().getUniqueId());
        if (target == null || !target.isOnline()) {
            hunter.sendMessage(ChatColor.RED + "That player is no longer online!");
            hunter.closeInventory();
            return;
        }

        if (target.equals(hunter)) {
            hunter.sendMessage(ChatColor.RED + "You cannot track yourself!");
            return;
        }

        plugin.getBountyManager().startTracking(hunter, target);
        hunter.closeInventory();
    }
}