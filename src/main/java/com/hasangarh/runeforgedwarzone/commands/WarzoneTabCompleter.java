package com.hasangarh.runeforgedwarzone.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WarzoneTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // --- /warzone Completion ---
        if (command.getName().equalsIgnoreCase("warzone")) {
            if (!sender.hasPermission("warzone.admin")) return Collections.emptyList();

            if (args.length == 1) {
                List<String> subCommands = Arrays.asList("setaltar", "spawnboss", "drop");
                StringUtil.copyPartialMatches(args[0], subCommands, completions);
            }
        }

        // --- /bounty Completion ---
        if (command.getName().equalsIgnoreCase("bounty")) {
            if (args.length == 1) {
                List<String> playerNames = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    playerNames.add(p.getName());
                }
                StringUtil.copyPartialMatches(args[0], playerNames, completions);
            } else if (args.length == 2) {
                List<String> amounts = Arrays.asList("500", "1000", "5000", "10000");
                StringUtil.copyPartialMatches(args[1], amounts, completions);
            }
        }

        Collections.sort(completions);
        return completions;
    }
}