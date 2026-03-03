package com.hasangarh.runeforgedwarzone.commands;

import com.hasangarh.runeforgedwarzone.RuneForgedWarzone;
import com.hasangarh.runeforgedwarzone.gui.BountyGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BountyCommand implements CommandExecutor {

    private final RuneForgedWarzone plugin;

    public BountyCommand(RuneForgedWarzone plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // 1. Open the Bounty GUI (No arguments)
        if (args.length == 0) {
            BountyGUI.openGUI(player, plugin);
            return true;
        }

        // 2. Manual Bounty Placement (/bounty <player> <amount>)
        if (args.length == 2) {
            Player target = Bukkit.getPlayer(args[0]);

            if (target == null || !target.isOnline()) {
                player.sendMessage(ChatColor.RED + "Player not found or offline.");
                return true;
            }

            if (target.equals(player)) {
                player.sendMessage(ChatColor.RED + "You cannot put a bounty on yourself!");
                return true;
            }

            try {
                double amount = Double.parseDouble(args[1]);

                // Get minimum bounty requirement from bounty.yml
                double minBounty = plugin.getBountyConfig().getDouble("settings.min-manual-bounty", 100.0);

                if (amount < minBounty) {
                    player.sendMessage(ChatColor.RED + "The minimum bounty amount is " + minBounty + " Aether.");
                    return true;
                }

                // Add the bounty through the manager
                boolean success = plugin.getBountyManager().addManualBounty(player, target, amount);

                if (success) {
                    player.sendMessage(ChatColor.GREEN + "Successfully placed bounty on " + target.getName() + "!");
                } else {
                    player.sendMessage(ChatColor.RED + "Failed to place bounty. Check your balance.");
                }

            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid amount! Please enter a number.");
            }
            return true;
        }

        player.sendMessage(ChatColor.RED + "Usage: /bounty OR /bounty <player> <amount>");
        return true;
    }
}