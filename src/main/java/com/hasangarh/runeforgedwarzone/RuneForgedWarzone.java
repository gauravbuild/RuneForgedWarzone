package com.hasangarh.runeforgedwarzone;

import com.hasangarh.runeforgedwarzone.commands.BountyCommand;
import com.hasangarh.runeforgedwarzone.commands.WarzoneTabCompleter;
import com.hasangarh.runeforgedwarzone.listeners.BossListener;
import com.hasangarh.runeforgedwarzone.listeners.BountyListener;
import com.hasangarh.runeforgedwarzone.listeners.SupplyListener;
import com.hasangarh.runeforgedwarzone.managers.BountyManager;
import com.hasangarh.runeforgedwarzone.managers.MobManager;
import com.hasangarh.runeforgedwarzone.managers.SupplyManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.UUID;

public class RuneForgedWarzone extends JavaPlugin {

    private MobManager mobManager;
    private SupplyManager supplyManager;
    private BountyManager bountyManager;

    private FileConfiguration bountyConfig;
    private FileConfiguration bossRewardsConfig;
    private FileConfiguration supplyRewardsConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        createCustomConfigs();

        // Initialize Managers
        this.mobManager = new MobManager(this);
        this.supplyManager = new SupplyManager(this);
        this.bountyManager = new BountyManager(this);

        // Register Listeners
        getServer().getPluginManager().registerEvents(new WarzoneListener(this), this);
        getServer().getPluginManager().registerEvents(new BossListener(mobManager), this);
        getServer().getPluginManager().registerEvents(new SupplyListener(this, supplyManager), this);
        getServer().getPluginManager().registerEvents(new BountyListener(this), this);

        // Commands & Tab Completers
        WarzoneTabCompleter tabCompleter = new WarzoneTabCompleter();
        getCommand("warzone").setExecutor(this::onCommand);
        getCommand("warzone").setTabCompleter(tabCompleter);
        getCommand("bounty").setExecutor(new BountyCommand(this));
        getCommand("bounty").setTabCompleter(tabCompleter);

        // Schedulers
        if (getConfig().getBoolean("supply-drop.enabled")) {
            supplyManager.startScheduler();
        }
        mobManager.startScheduler();

        startBountyBroadcastTasks();

        getLogger().info("RuneForgedWarzone active and fixed!");
    }

    private void createCustomConfigs() {
        saveResource("bounty.yml", false);
        saveResource("boss-rewards.yml", false);
        saveResource("supply-drop-rewards.yml", false);

        bountyConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "bounty.yml"));
        bossRewardsConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "boss-rewards.yml"));
        supplyRewardsConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "supply-drop-rewards.yml"));
    }

    private void startBountyBroadcastTasks() {
        // Location Broadcast Task (Every 60s)
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID id : bountyManager.getActiveBounties().keySet()) {
                    Player p = Bukkit.getPlayer(id);
                    String worldName = getConfig().getString("warzone-rules.world-name", "warzone");
                    if (p != null && p.getWorld().getName().equals(worldName)) {
                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                                "&c&lWANTED &8» &e" + p.getName() + " &7is at &fX: " + p.getLocation().getBlockX() + " Z: " + p.getLocation().getBlockZ()));
                    }
                }
            }
        }.runTaskTimer(this, 1200L, 1200L);

        // Action Bar Tracking Task (Fixed the "api" error here)
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player hunter : Bukkit.getOnlinePlayers()) {
                    UUID targetId = bountyManager.getTrackingTarget(hunter.getUniqueId());
                    if (targetId == null) continue;
                    Player target = Bukkit.getPlayer(targetId);
                    if (target == null || !target.isOnline()) {
                        bountyManager.stopTracking(hunter.getUniqueId());
                        continue;
                    }
                    double dist = hunter.getLocation().distance(target.getLocation());
                    // FIXED: Uses correct Bungee API imports
                    hunter.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new TextComponent(ChatColor.GOLD + "Tracking: " + target.getName() + " | " + (int)dist + "m"));
                }
            }
        }.runTaskTimer(this, 0L, 10L);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if (!player.hasPermission("warzone.admin")) return true; //
        if (args.length == 0) return false;

        if (args[0].equalsIgnoreCase("setaltar")) {
            Location loc = player.getLocation();
            getConfig().set("boss.location.world", loc.getWorld().getName());
            getConfig().set("boss.location.x", loc.getX());
            getConfig().set("boss.location.y", loc.getY());
            getConfig().set("boss.location.z", loc.getZ());
            saveConfig();
            player.sendMessage(ChatColor.GREEN + "Boss Altar set!");
            return true;
        }

        if (args[0].equalsIgnoreCase("spawnboss")) {
            mobManager.spawnGuardianAtAltar();
            return true;
        }

        if (args[0].equalsIgnoreCase("drop")) {
            supplyManager.spawnSupplyDrop();
            return true;
        }
        return true;
    }

    public BountyManager getBountyManager() { return bountyManager; }
    public FileConfiguration getBountyConfig() { return bountyConfig; }
    public FileConfiguration getSupplyRewardsConfig() { return supplyRewardsConfig; }
}