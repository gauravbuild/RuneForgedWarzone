package com.hasangarh.runeforgedwarzone;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class RuneForgedWarzone extends JavaPlugin implements CommandExecutor {

    private static RuneForgedWarzone instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Register the Event Listener
        getServer().getPluginManager().registerEvents(new WarzoneListener(this), this);

        // Register Command
        getCommand("rfwarzone").setExecutor(this);

        getLogger().info("RuneForgedWarzone has been enabled!");
    }

    public static RuneForgedWarzone getInstance() {
        return instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("runeforgedwarzone.admin")) {
                sender.sendMessage("§cNo permission.");
                return true;
            }
            reloadConfig();
            sender.sendMessage("§a[RuneForgedWarzone] Configuration reloaded.");
            return true;
        }
        return false;
    }
}