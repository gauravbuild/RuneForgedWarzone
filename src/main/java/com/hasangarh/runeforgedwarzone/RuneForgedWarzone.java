package com.hasangarh.runeforgedwarzone;

import com.hasangarh.runeforgedwarzone.listeners.BossListener;
import com.hasangarh.runeforgedwarzone.listeners.SupplyListener;
import com.hasangarh.runeforgedwarzone.managers.MobManager;
import com.hasangarh.runeforgedwarzone.managers.SupplyManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RuneForgedWarzone extends JavaPlugin {

    private MobManager mobManager;
    private SupplyManager supplyManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Initialize Managers
        // Pass 'this' to MobManager now
        this.mobManager = new MobManager(this);
        this.supplyManager = new SupplyManager(this);

        // Listeners
        getServer().getPluginManager().registerEvents(new WarzoneListener(this), this);
        getServer().getPluginManager().registerEvents(new BossListener(mobManager), this);
        getServer().getPluginManager().registerEvents(new SupplyListener(this, supplyManager), this);

        // Start Schedulers
        if (getConfig().getBoolean("supply-drop.enabled")) {
            supplyManager.startScheduler();
        }
        mobManager.startScheduler(); // Auto-spawn boss

        // Command
        getCommand("warzone").setExecutor(this::onCommand);

        getLogger().info("RuneForgedWarzone active!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (!player.hasPermission("warzone.admin")) {
            player.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        if (args.length == 0) return false;

        // 1. SET ALTAR COMMAND
        if (args[0].equalsIgnoreCase("setaltar")) {
            Location loc = player.getLocation();
            getConfig().set("boss.location.world", loc.getWorld().getName());
            getConfig().set("boss.location.x", loc.getX());
            getConfig().set("boss.location.y", loc.getY());
            getConfig().set("boss.location.z", loc.getZ());
            saveConfig();
            player.sendMessage(ChatColor.GREEN + "Boss Altar set to your location!");
            return true;
        }

        // 2. SPAWN BOSS MANUALLY
        if (args[0].equalsIgnoreCase("spawnboss")) {
            mobManager.spawnGuardianAtAltar(); // Use new method
            player.sendMessage(ChatColor.GREEN + "Forced boss spawn event.");
            return true;
        }

        // 3. SUPPLY DROP
        if (args[0].equalsIgnoreCase("drop")) {
            supplyManager.spawnSupplyDrop();
            return true;
        }

        return true;
    }
}