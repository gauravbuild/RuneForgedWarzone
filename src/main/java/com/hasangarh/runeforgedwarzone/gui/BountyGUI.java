package com.hasangarh.runeforgedwarzone.gui;

import com.hasangarh.runeforgedwarzone.RuneForgedWarzone;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BountyGUI {

    public static void openGUI(Player player, RuneForgedWarzone plugin) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_RED + "Active Bounties");
        Map<UUID, Double> bounties = plugin.getBountyManager().getActiveBounties();

        for (Map.Entry<UUID, Double> entry : bounties.entrySet()) {
            Player target = Bukkit.getPlayer(entry.getKey());
            if (target == null) continue;

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(target);
            meta.setDisplayName(ChatColor.RED + target.getName());

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Reward: " + ChatColor.GOLD + entry.getValue() + " Aether");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to track this player!");
            meta.setLore(lore);
            head.setItemMeta(meta);

            gui.addItem(head);
        }

        player.openInventory(gui);
    }
}